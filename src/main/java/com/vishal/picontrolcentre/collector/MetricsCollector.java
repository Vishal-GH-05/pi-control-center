package com.vishal.picontrolcentre.collector;

import com.vishal.picontrolcentre.monitor.GlancesStatusMonitor;
import com.vishal.picontrolcentre.service.PersistenceService;
import com.vishal.picontrolcentre.service.SystemMetricsService;
import com.vishal.picontrolcentre.status.GlancesStatus;
import com.vishal.picontrolcentre.store.RedisMetricsStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetricsCollector {

    private final SystemMetricsService metricsService;

    private final RedisMetricsStore metricsStore;

    private final GlancesStatusMonitor glancesStatusMonitor;

    private final PersistenceService persistenceService;

    public MetricsCollector(SystemMetricsService metricsService, RedisMetricsStore metricsStore, GlancesStatusMonitor glancesStatusMonitor, PersistenceService persistenceService) {
        this.metricsService = metricsService;
        this.metricsStore = metricsStore;
        this.glancesStatusMonitor = glancesStatusMonitor;
        this.persistenceService = persistenceService;
    }

    @Scheduled(fixedDelayString = "${fast.metrics.collector.interval}")
    public void collectFastMetrics(){
        if(glancesStatusMonitor.getStatus() == GlancesStatus.UP){
            metricsStore.saveCpuStats(metricsService.getCpuStats());
            metricsStore.saveMemoryStats(metricsService.getMemoryStats());
            metricsStore.saveLoadStats(metricsService.getLoadStats());
            metricsStore.saveNetworkStats(metricsService.getNetworkStats());

        }
    }

    @Scheduled(fixedDelayString = "${sensor.metrics.collector.interval}")
    public void collectSensorMetrics(){
        if(glancesStatusMonitor.getStatus() == GlancesStatus.UP){
            metricsStore.saveSensorStats(metricsService.getSensorStats());
        }
    }

    @Scheduled(fixedDelayString = "${slow.metrics.collector.interval}")
    public void collectSlowMetrics(){
        if(glancesStatusMonitor.getStatus() == GlancesStatus.UP){
            metricsStore.saveFileSystemStats(metricsService.getFileSystemStats());
            metricsStore.saveUptime(metricsService.getUptime());
        }
    }

    @Scheduled(fixedDelayString = "${db.metrics.collector.updater.interval}")
    public void updateMetricsToDB(){
        if(glancesStatusMonitor.getStatus() == GlancesStatus.UP){
            persistenceService.persistCpuStats(metricsStore.getCpuStats());
            persistenceService.persistMemoryStats(metricsStore.getMemoryStats());
            persistenceService.persistLoadStats(metricsStore.getLoadStats());
            persistenceService.persistNetworkStats(metricsStore.getNetworkStats());
            persistenceService.persistSensorStats(metricsStore.getSensorStats());
        }


    }




}
