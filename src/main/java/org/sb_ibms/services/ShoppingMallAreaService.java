package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sb_ibms.ResourceNotFoundException;
import org.sb_ibms.models.Area;
import org.sb_ibms.models.ShoppingMallArea;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShoppingMallAreaService {
    private ShoppingMallAreaRepository areaRepository;
    private ShoppingMallCustomerRepository masterRepo;
    private ShoppingMallCahbackPaymentRepository cashbackRepo;


    public ShoppingMallArea updateArea(Long id, ShoppingMallArea updatedArea) {
        ShoppingMallArea area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found"));

        area.setName(updatedArea.getName());
        area.setPurchaseAmount(updatedArea.getPurchaseAmount());
        area.setPaidAmount(updatedArea.getPaidAmount());
        area.setDueAmount(updatedArea.getDueAmount());
        area.setCashbackAmount(updatedArea.getCashbackAmount());
        area.setPackageQuantity(updatedArea.getPackageQuantity());

        return areaRepository.save(area);
    }

    @Transactional
    public ShoppingMallArea recalculateArea(Long areaId) {

        ShoppingMallArea area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found"));

        BigDecimal totalPurchase = masterRepo.sumPurchaseByArea(areaId);
        BigDecimal totalPaid = masterRepo.sumPaidByArea(areaId);
        BigDecimal totalQuantity = masterRepo.sumQuantityByArea(areaId);
        BigDecimal totalCashback = cashbackRepo.sumCashbackByArea(areaId);

        totalPurchase = totalPurchase != null ? totalPurchase : BigDecimal.ZERO;
        totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;

        area.setPurchaseAmount(totalPurchase);
        area.setPaidAmount(totalPaid);
        area.setDueAmount(totalPurchase.subtract(totalCashback));
        area.setCashbackAmount(totalCashback);
        area.setPackageQuantity(totalQuantity);

        return areaRepository.save(area);
    }

    public ShoppingMallArea getAreaByIdWithMallCheck(Long areaId, Long mallId) {
        if (areaId == null) {
            throw new RuntimeException("Area ID is required");
        }

        ShoppingMallArea area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found with ID: " + areaId));

        // Security check for managers
        if (mallId != null && !mallId.equals(area.getShoppingMallId())) {
            throw new RuntimeException("Access denied: This area belongs to another shopping mall");
        }

        return area;
    }

    @Transactional
    public ShoppingMallArea getAreaSummary(Long areaId) {
        return recalculateArea(areaId);
    }

    public List<AreaSummaryDTO> getDailyAreaSummary(LocalDate date) {

        List<ShoppingMallArea> areas = areaRepository.findAll();

        return areas.stream().map(area -> {
            Long areaId = area.getId();

            BigDecimal totalPurchase = masterRepo.sumPurchaseByAreaAndDate(areaId, date);
            BigDecimal totalQuantity = masterRepo.sumQuantityByAreaAndDate(areaId, date);
            BigDecimal totalCashback = cashbackRepo.sumCashbackByAreaAndDate(areaId, date);
            Long cashbackQuantity = cashbackRepo.countCashbackByAreaAndDate(areaId, date);

            totalPurchase = totalPurchase != null ? totalPurchase : BigDecimal.ZERO;
            totalQuantity = totalQuantity != null ? totalQuantity : BigDecimal.ZERO;
            totalCashback = totalCashback != null ? totalCashback : BigDecimal.ZERO;
            cashbackQuantity = cashbackQuantity != null ? cashbackQuantity : 0L;

            return new AreaSummaryDTO(
                    areaId,
                    area.getName(),
                    totalPurchase,
                    totalQuantity,
                    totalCashback,
                    cashbackQuantity
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<ShoppingMallArea> getAllAreas() {
        List<ShoppingMallArea> areas = areaRepository.findAll();

        return areas.stream().map(area -> recalculateArea(area.getId())).collect(Collectors.toList());
    }

    public ShoppingMallArea getAreaById(Long id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found"));
    }

    public ShoppingMallArea getAreaByName(String name) {
        return areaRepository.findByName(name);
    }

    public ShoppingMallArea createArea(ShoppingMallArea area) {
        return areaRepository.save(area);
    }

    @Transactional
    public ShoppingMallArea getOrCreateArea(String areaName) {
        ShoppingMallArea area = areaRepository.findByName(areaName);

        if (area == null) {
            area = new ShoppingMallArea();
            area.setName(areaName);
            area.setPurchaseAmount(BigDecimal.ZERO);
            area.setPaidAmount(BigDecimal.ZERO);
            area.setDueAmount(BigDecimal.ZERO);
            area.setCashbackAmount(BigDecimal.ZERO);
            area.setPackageQuantity(BigDecimal.ZERO);

            area = areaRepository.save(area);
        }

        return area;
    }



    public void deleteArea(Long id) {
        ShoppingMallArea area = getAreaById(id);
        areaRepository.delete(area);
    }

}
