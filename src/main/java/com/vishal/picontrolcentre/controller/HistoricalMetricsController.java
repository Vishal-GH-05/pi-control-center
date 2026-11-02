package com.vishal.picontrolcentre.controller;

import com.vishal.picontrolcentre.entity.*;
import com.vishal.picontrolcentre.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("app/historical/system")
public class HistoricalMetricsController {

    private final CpuRepository cpuRepository;
    private final LoadRepository loadRepository;
    private final MemoryRepository memoryRepository;
    private final NetworkRepository networkRepository;
    private final SensorRepository sensorRepository;


    public HistoricalMetricsController(CpuRepository cpuRepository, LoadRepository loadRepository, MemoryRepository memoryRepository, NetworkRepository networkRepository, SensorRepository sensorRepository) {
        this.cpuRepository = cpuRepository;
        this.loadRepository = loadRepository;
        this.memoryRepository = memoryRepository;
        this.networkRepository = networkRepository;
        this.sensorRepository = sensorRepository;
    }

    @GetMapping("/cpu")
    public ResponseEntity<List<CpuHistory>> getHCpu() {
        Page<CpuHistory> page =
                cpuRepository.findAll(PageRequest.of(0, 600));

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/load")
    public ResponseEntity<List<LoadHistory>> getHLoad() {
        Page<LoadHistory> page =
                loadRepository.findAll(PageRequest.of(0, 600));

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/memory")
    public ResponseEntity<List<MemoryHistory>> getHMemory() {
        Page<MemoryHistory> page =
                memoryRepository.findAll(PageRequest.of(0, 600));

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/network")
    public ResponseEntity<List<NetworkHistory>> getHNetwork() {
        Page<NetworkHistory> page =
                networkRepository.findAll(PageRequest.of(0, 600));

        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/sensor")
    public ResponseEntity<List<SensorHistory>> getHSensor() {
        Page<SensorHistory> page =
                sensorRepository.findAll(PageRequest.of(0, 600));

        return ResponseEntity.ok(page.getContent());
    }

}
