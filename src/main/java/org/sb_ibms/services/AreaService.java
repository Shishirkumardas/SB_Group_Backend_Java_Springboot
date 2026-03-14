package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sb_ibms.ResourceNotFoundException;
import org.sb_ibms.models.Area;
import org.sb_ibms.repositories.AreaRepository;
import org.sb_ibms.repositories.CashbackPaymentRepository;
import org.sb_ibms.repositories.MasterDataRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AreaService {

    private AreaRepository areaRepository;
    private MasterDataRepository masterRepo;
    private CashbackPaymentRepository cashbackRepo;


    public Area updateArea(Long id, Area updatedArea) {
        Area area = areaRepository.findById(id)
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
    public Area recalculateArea(Long areaId) {

        Area area = areaRepository.findById(areaId)
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

    @Transactional
    public Area getAreaSummary(Long areaId) {
        return recalculateArea(areaId);
    }

    public List<AreaSummaryDTO> getDailyAreaSummary(LocalDate date) {

        List<Area> areas = areaRepository.findAll();

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
    public List<Area> getAllAreas() {
        List<Area> areas = areaRepository.findAll();

        return areas.stream().map(area -> recalculateArea(area.getId())).collect(Collectors.toList());
    }

    public Area getAreaById(Long id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found"));
    }

    public Area getAreaByName(String name) {
        return areaRepository.findByName(name);
    }

    public Area createArea(Area area) {
        return areaRepository.save(area);
    }

    @Transactional
    public Area getOrCreateArea(String areaName) {
        Area area = areaRepository.findByName(areaName);

        if (area == null) {
            area = new Area();
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
        Area area = getAreaById(id);
        areaRepository.delete(area);
    }

}
