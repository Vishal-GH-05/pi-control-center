package com.vishal.picontrolcentre.monitor;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class GlancesStartupChecker implements ApplicationRunner {

    private final GlancesStatusMonitor statusMonitor;

    public GlancesStartupChecker(
            GlancesStatusMonitor statusMonitor
    ) {
        this.statusMonitor = statusMonitor;
    }

    @Override
    public void run(ApplicationArguments args) {
        statusMonitor.startupChecker();
    }
}
