package com.vishal.picontrolcentre.client;

import com.vishal.picontrolcentre.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class GlancesClient {

    private final RestClient client;

    public GlancesClient(
            @Value("${glances.base-url}") String baseUrl) {

        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public GlancesStatusResponse getStatus(){
        return client.get()
                .uri("api/4/status")
                .retrieve()
                .body(GlancesStatusResponse.class);
    }

    public GlancesCPUResponse getCpu(){
        return client.get()
                .uri("api/4/cpu")
                .retrieve()
                .body(GlancesCPUResponse.class);
    }

    public GlancesMemoryResponse getMemory() {
        return client
                .get()
                .uri("/api/4/mem")
                .retrieve()
                .body(GlancesMemoryResponse.class);
    }

    public GlancesLoadResponse getLoad() {
        return client
                .get()
                .uri("/api/4/load")
                .retrieve()
                .body(GlancesLoadResponse.class);
    }

    public String getUptime() {
        return client
                .get()
                .uri("/api/4/uptime")
                .retrieve()
                .body(String.class);
    }

    public List<GlancesSensorResponse> getSensors() {
        return client
                .get()
                .uri("/api/4/sensors")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<GlancesFileSystemResponse> getFileSystems() {
        return client
                .get()
                .uri("/api/4/fs")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<GlancesNetworkResponse> getNetwork() {
        return client
                .get()
                .uri("/api/4/network")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }


}
