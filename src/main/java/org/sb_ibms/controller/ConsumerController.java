package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.models.Area;
import org.sb_ibms.models.Consumer;
import org.sb_ibms.repositories.ConsumerRepository;
import org.sb_ibms.services.AreaService;
import org.sb_ibms.services.ConsumerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/consumers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class ConsumerController {

    private final ConsumerRepository repo;
    private final ConsumerService consumerService;
    private final AreaService areaService;

    @GetMapping
    public List<Consumer> getAll() {
        return repo.findAll();
    }


    @PutMapping("/{id}")
    public Consumer updateConsumer(@PathVariable Long id, @RequestBody Consumer consumer) {
        return consumerService.updateConsumer(id, consumer);
    }

    @PostMapping
    public Consumer create(@RequestBody Consumer consumer) {
        return repo.save(consumer);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @GetMapping("/summary/area")
    public ResponseEntity<Area> getAreaSummary(long id) {
        Area summary = areaService.getAreaSummary(id);
        return ResponseEntity.ok((Area) summary);
    }

}

