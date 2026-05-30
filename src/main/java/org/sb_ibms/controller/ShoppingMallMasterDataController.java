package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.OverallSummary;
import org.sb_ibms.dto.PaymentView;
import org.sb_ibms.dto.PurchaseView;
import org.sb_ibms.dto.ShoppingMallPaymentView;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.repositories.ShoppingMallCahbackPaymentRepository;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.sb_ibms.services.ShoppingMallContext;
import org.sb_ibms.services.ShoppingMallMasterDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/shoppingmall-master-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class ShoppingMallMasterDataController {

    private final ShoppingMallCustomerRepository repo;
    private final ShoppingMallMasterDataService masterDataService;
    private final ShoppingMallCahbackPaymentRepository cashbackPaymentRepository;
    private final ShoppingMallContext shoppingMallContext;

//    @GetMapping
//    public List<ShoppingMallCustomer> getAll() {
//        return repo.findAll();
//    }

    @GetMapping
    public List<ShoppingMallCustomer> getAll() {
        Long mallId = shoppingMallContext.getCurrentMallId();
        return repo.findByShoppingMallId(mallId);           // ← Fixed
    }

    @GetMapping("/masterData")
    public ShoppingMallCustomer getMasterData(@RequestParam(required = false) Long id,
                                    @RequestParam(required = false) String name) {
        if (id != null) {
            return masterDataService.getMasterDataById(id);
        } else if (name != null) {
            return masterDataService.getMasterDataByName(name);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either id or name must be provided");
        }
    }

    @PutMapping("/{id}")
    public ShoppingMallCustomer updateMasterData(@PathVariable Long id, @RequestBody ShoppingMallCustomer masterData) {
        return masterDataService.updateMasterData(id, masterData);
    }

//    @PostMapping("/pay")
//    public ResponseEntity<?> pay(
//            @RequestParam Long masterDataId) {
//
//        return ResponseEntity.ok(
//                bkashPaymentService.makePayment(masterDataId)
//        );
//    }


//    @PostMapping
//    public ShoppingMallCustomer create(@RequestBody ShoppingMallCustomer masterData) {
//        return masterDataService.create(masterData);
//    }

    @PostMapping
    public ShoppingMallCustomer create(@RequestBody ShoppingMallCustomer masterData) {
        Long mallId = shoppingMallContext.getCurrentMallId();
        masterData.setShoppingMallId(mallId);          // ← Important
        return masterDataService.create(masterData);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        masterDataService.delete(id);
    }
    @DeleteMapping
    public void deleteAll() {
        cashbackPaymentRepository.deleteAll();
        repo.deleteAll();
    }


//    @GetMapping("/payments")
//    public List<ShoppingMallPaymentView> payments() {
//        return repo.getPayments();
//    }
//
//    @GetMapping("/purchases")
//    public List<PurchaseView> purchases() {
//        return repo.getPurchases();
//    }
//
//    @GetMapping("/summary")
//    public OverallSummary summary() {
//        return repo.getSummary();
//    }
@GetMapping("/payments")
public List<ShoppingMallPaymentView> payments() {
    Long mallId = shoppingMallContext.getCurrentMallId();
    return repo.getPayments(mallId);
}

    @GetMapping("/purchases")
    public List<PurchaseView> purchases() {
        Long mallId = shoppingMallContext.getCurrentMallId();
        return repo.getPurchases(mallId);
    }

    @GetMapping("/summary")
    public OverallSummary summary() {
        Long mallId = shoppingMallContext.getCurrentMallId();
        return repo.getSummary(mallId);
    }
}
