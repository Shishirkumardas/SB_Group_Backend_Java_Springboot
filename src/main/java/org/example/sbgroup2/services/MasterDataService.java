package org.example.sbgroup2.services;

import org.example.sbgroup2.ResourceNotFoundException;
import org.example.sbgroup2.dto.CustomerFormDTO;
import org.example.sbgroup2.enums.OrderStatus;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MasterDataService {
    @Autowired
    private MasterDataRepository masterDataRepository;
    @Autowired
    private AreaService areaService;

    public MasterData create(MasterData data) {
        if (data.getPurchaseAmount() != null && data.getPaidAmount() != null) {
            data.setDueAmount(data.getPurchaseAmount().subtract(data.getPaidAmount()));
        }

        MasterData saved = masterDataRepository.save(data);

        // 🔥 auto-update area
        areaService.recalculateArea(data.getArea().getId());

        return saved;
    }

    public MasterData updateMasterData(Long id, MasterData masterDataDetails) {
        MasterData masterData = masterDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterData not found"));
        masterData.setName(masterDataDetails.getName());
        masterData.setArea(masterDataDetails.getArea());
        masterData.setPaymentMethod(masterDataDetails.getPaymentMethod());
        masterData.setBkash(masterDataDetails.getBkash());
        masterData.setDate(masterDataDetails.getDate());
        masterData.setPurchaseAmount(masterDataDetails.getPurchaseAmount());
        masterData.setDueAmount(masterDataDetails.getDueAmount());
        masterData.setCashBackAmount(masterDataDetails.getCashBackAmount());
        MasterData saved  = masterDataRepository.save(masterData);
        areaService.recalculateArea(saved.getArea().getId());
        return saved;
    }

    public MasterData getMasterDataById(Long id) {
        return masterDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found"));
    }

    public MasterData getMasterDataByName(String name) {
        return masterDataRepository.findByName(name);
    }
    public void delete(Long id) {
        MasterData data = masterDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MasterData not found"));

        Long areaId = data.getArea().getId();
        masterDataRepository.deleteById(id);

        // 🔥 update area
        areaService.recalculateArea(areaId);
    }
    public void updatePaymentSummary(MasterData masterData, BigDecimal newPayment) {

        BigDecimal paid = masterData.getPaidAmount().add(newPayment);
        masterData.setPaidAmount(paid);

        BigDecimal due = masterData.getPurchaseAmount().subtract(paid);

        if (due.compareTo(BigDecimal.ZERO) <= 0) {
            masterData.setStatus(OrderStatus.PAID);
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            masterData.setStatus(OrderStatus.PARTIALLY_PAID);
        } else {
            masterData.setStatus(OrderStatus.PENDING);
        }

        masterDataRepository.save(masterData);
    }
    public MasterData saveCustomerForm(Long id, CustomerFormDTO dto) {
        MasterData md = masterDataRepository.findById(id).orElseThrow();

        md.setName(dto.getCustomerName());
        md.setPaymentMethod(dto.getPaymentMethod());
        md.setPaidAmount(dto.getAmount());
        md.setDate(dto.getPaymentDate());

        return masterDataRepository.save(md);
    }

}
