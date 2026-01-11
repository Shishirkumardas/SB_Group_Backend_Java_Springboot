package org.example.sbgroup2.controller;

import lombok.RequiredArgsConstructor;
import org.example.sbgroup2.dto.*;
import org.example.sbgroup2.models.Area;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.example.sbgroup2.services.AreaService;
import org.example.sbgroup2.services.CashbackService;
import org.example.sbgroup2.services.MasterDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
@RequestMapping("/api/master-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class MasterDataController {

    private final MasterDataRepository repo;
    private final MasterDataService masterDataService;
    private final AreaService areaService;
    private final CashbackService  cashbackService;

    @GetMapping
    public List<MasterData> getAll() {

        return repo.findAll();
    }

    @GetMapping("/masterData")
    public MasterData getMasterData(@RequestParam(required = false) Long id,
                                @RequestParam(required = false) String name) {
        if (id != null) {
            return masterDataService.getMasterDataById(id);
        } else if (name != null) {
            return masterDataService.getMasterDataByName(name);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either id or name must be provided");
        }
    }
//    @PostMapping("/customer/submit")
//    public MasterData saveCustomerForm(
//            @PathVariable Long masterId,
//            @RequestBody CustomerFormDTO dto
//    ) {
//        return masterDataService.saveCustomerForm(masterId, dto);
//    }


    @PutMapping("/{id}")
    public MasterData updateMasterData(@PathVariable Long id, @RequestBody MasterData masterData) {
        return masterDataService.updateMasterData(id, masterData);
    }

//    @PostMapping("/master-data/excel-csv")
//    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
//
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().body("CSV file is empty");
//        }
//
//        List<MasterData> rows = masterDataService.readExcel(file);
//        masterDataService.importExcel(rows);
//
//        return ResponseEntity.ok("Master data uploaded successfully");
//    }


    @PostMapping
    public MasterData create(@RequestBody MasterData masterData) {
        return masterDataService.create(masterData);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        masterDataService.delete(id);
    }
    @GetMapping("/consumers")
    public List<ConsumerView> consumers() {
        return repo.getConsumers();
    }


    @GetMapping("/payments")
    public List<PaymentView> payments() {
        return repo.getPayments();
    }

    @GetMapping("/purchases")
    public List<PurchaseView> purchases() {
        return repo.getPurchases();
    }

    @GetMapping("/summary")
    public OverallSummary summary() {
        return repo.getSummary();
    }


}
