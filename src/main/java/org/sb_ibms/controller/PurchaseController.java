package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.PurchaseView;
import org.sb_ibms.repositories.MasterDataRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class PurchaseController {

    private final MasterDataRepository masterDataRepository;

    @GetMapping("/purchases")
    public List<PurchaseView> purchases() {
        return masterDataRepository.getPurchases();
    }

}

