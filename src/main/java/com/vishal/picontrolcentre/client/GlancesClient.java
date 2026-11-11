package com.vishal.picontrolcentre.client;

import com.vishal.picontrolcentre.dto.*;
import com.vishal.picontrolcentre.exception.GlancesUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.function.Supplier;

@Component
public class GlancesClient {

    private final RestClient client;

    public GlancesClient(
            @Value("${glances.base-url}") String baseUrl
    ) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    private <T> T execute(
            String operation,
            Supplier<T> request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Request supplier must not be null."
            );
        }

        try {
            T response = request.get();

            if (response == null) {
                throw new GlancesUnavailableException(
                        "Glances returned an empty response for: " + operation
                );
            }

            return response;

        } catch (RestClientException ex) {
            throw new GlancesUnavailableException(
                    "Unable to retrieve " + operation + " from Glances.",
                    ex
            );
        }
    }

    public GlancesStatusResponse getStatus() {
        return execute(
                "status",
                () -> client.get()
                        .uri("/api/4/status")
                        .retrieve()
                        .body(GlancesStatusResponse.class)
        );
    }

    public GlancesCPUResponse getCpu() {
        return execute(
                "CPU metrics",
                () -> client.get()
                        .uri("/api/4/cpu")
                        .retrieve()
                        .body(GlancesCPUResponse.class)
        );
    }

    public GlancesMemoryResponse getMemory() {
        return execute(
                "memory metrics",
                () -> client.get()
                        .uri("/api/4/mem")
                        .retrieve()
                        .body(GlancesMemoryResponse.class)
        );
    }

    public GlancesLoadResponse getLoad() {
        return execute(
                "load metrics",
                () -> client.get()
                        .uri("/api/4/load")
                        .retrieve()
                        .body(GlancesLoadResponse.class)
        );
    }

    public String getUptime() {
        return execute(
                "uptime",
                () -> client.get()
                        .uri("/api/4/uptime")
                        .retrieve()
                        .body(String.class)
        );
    }

    public List<GlancesSensorResponse> getSensors() {
        return execute(
                "sensor metrics",
                () -> client.get()
                        .uri("/api/4/sensors")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {})
        );
    }

    public List<GlancesFileSystemResponse> getFileSystems() {
        return execute(
                "filesystem metrics",
                () -> client.get()
                        .uri("/api/4/fs")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {})
        );
    }

    public List<GlancesNetworkResponse> getNetwork() {
        return execute(
                "network metrics",
                () -> client.get()
                        .uri("/api/4/network")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {})
        );
    }
}