package com.vishal.picontrolcentre.service;

import com.vishal.picontrolcentre.client.GlancesClient;
import com.vishal.picontrolcentre.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SystemMetricsService {

    private final GlancesClient glancesClient;

    public SystemMetricsService(GlancesClient glancesClient) {
        this.glancesClient = glancesClient;
    }

    private <T> T requireResponse(T response, String operation) {
        if (response == null) {
            throw new IllegalStateException(
                    "Missing Glances response for: " + operation
            );
        }

        return response;
    }

    public CpuStatsResponse getCpuStats(){

        GlancesCPUResponse cpuResponse = requireResponse(
                glancesClient.getCpu(),
                "CPU metrics"
        );

        int nCores = cpuResponse.cpucore();
        double totalUsage = cpuResponse.total();
        double userUsagePercent = cpuResponse.user();
        double iowait = cpuResponse.iowait();

        return new CpuStatsResponse(nCores,totalUsage,userUsagePercent,iowait);
    }

    public MemoryStatsResponse getMemoryStats(){

        GlancesMemoryResponse memoryResponse = requireResponse(
                glancesClient.getMemory(),
                "memory metrics"
        );

        long total = memoryResponse.total();
        long free = memoryResponse.free();
        double percent = memoryResponse.percent();
        long used = memoryResponse.used();

        return new MemoryStatsResponse(total,free,percent,used);

    }

    public LoadStatsResponse getLoadStats(){

        GlancesLoadResponse loadResponse = requireResponse(
                glancesClient.getLoad(),
                "load metrics"
        );

        double min1 = loadResponse.min1();
        double min5 = loadResponse.min5();
        double min15 = loadResponse.min15();

        return new LoadStatsResponse(min1, min5, min15);

    }

    public NetworkStatsResponse getNetworkStats() {

        List<GlancesNetworkResponse> networkResponse =
                glancesClient.getNetwork();

        if (networkResponse == null || networkResponse.isEmpty()) {
            throw new IllegalStateException(
                    "Glances returned no network interfaces."
            );
        }

        GlancesNetworkResponse result = networkResponse.stream()
                .filter(x -> x != null)
                .filter(x -> "eth0".equalsIgnoreCase(x.interface_name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Network interface eth0 is not available."
                ));

        return new NetworkStatsResponse(
                result.interface_name(),
                result.bytes_recv_rate_per_sec(),
                result.bytes_sent_rate_per_sec()
        );
    }

    public List<SensorStatsResponse> getSensorStats(){

        List<GlancesSensorResponse> sensorResponses =
                requireResponse(
                        glancesClient.getSensors(),
                        "sensor metrics"
                );

        return sensorResponses.stream()
                .filter(x -> x != null)
                .filter(x -> x.label() != null)
                .filter(x -> x.label().equalsIgnoreCase("cpu_thermal 0"))
                .map(x -> new SensorStatsResponse(
                        x.label(),
                        x.unit(),
                        x.value(),
                        x.type(),
                        x.key()
                ))
                .toList();
    }

    public List<FileSystemStatsResponse> getFileSystemStats(){

        List<GlancesFileSystemResponse> fileSystemResponses =
                glancesClient.getFileSystems();

        return fileSystemResponses.stream()
                .map(x -> new FileSystemStatsResponse(
                        x.device_name(),
                        x.mnt_point(),
                        x.size(),
                        x.used(),
                        x.free(),
                        x.percent()
                ))
                .toList();
    }

    public String getUptime(){

        return glancesClient.getUptime();
    }

}
