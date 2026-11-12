package com.vishal.picontrolcentre.monitor;

import com.vishal.picontrolcentre.client.GlancesClient;
import com.vishal.picontrolcentre.dto.GlancesStatusResponse;
import com.vishal.picontrolcentre.status.GlancesStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GlancesStatusMonitor {

    private static final Logger logger =
            LoggerFactory.getLogger(GlancesStatusMonitor.class);

    private final GlancesClient client;

    private volatile GlancesStatus currentStatus = GlancesStatus.DOWN;

    public GlancesStatusMonitor(GlancesClient glancesClient) {
        this.client = glancesClient;
    }

    public void checkStatus() {
        GlancesStatusResponse response = client.getStatus();

        currentStatus = response != null && response.version() != null
                ? GlancesStatus.UP
                : GlancesStatus.DOWN;
    }

    public GlancesStatus getStatus() {
        return currentStatus;
    }

    @Scheduled(fixedRate = 2000)
    public void monitorGlances() {
        try {
            checkStatus();

            if (currentStatus == GlancesStatus.UP) {
                logger.debug("Glances is UP.");
            } else {
                logger.warn("Glances returned an invalid status response.");
            }

        } catch (RuntimeException ex) {
            currentStatus = GlancesStatus.DOWN;
            logger.warn("Glances is unavailable. Monitoring will retry.", ex);
        }
    }

    public void startupChecker() {
        // Perform an initial check, but don't prevent application startup.
        monitorGlances();
    }
}