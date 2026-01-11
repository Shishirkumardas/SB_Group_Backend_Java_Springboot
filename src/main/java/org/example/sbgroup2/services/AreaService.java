package org.example.sbgroup2.services;

import jakarta.transaction.Transactional;
import org.example.sbgroup2.ResourceNotFoundException;
import org.example.sbgroup2.models.Area;
import org.example.sbgroup2.models.Area;
import org.example.sbgroup2.repositories.AreaRepository;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AreaService {
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private MasterDataRepository masterRepo;


    public Area updateArea(Long id, Area updatedArea) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found"));

        area.setName(updatedArea.getName());
        area.setPurchaseAmount(updatedArea.getPurchaseAmount());
        area.setPaidAmount(updatedArea.getPaidAmount());
        area.setDueAmount(updatedArea.getDueAmount());

        return areaRepository.save(area);
    }

//    public Area getAreaSummary(long id) {
//        return areaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Area not found"));
//    }
    @Transactional
    public Area recalculateArea(Long areaId) {

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found"));

        BigDecimal totalPurchase = masterRepo.sumPurchaseByArea(areaId);
        BigDecimal totalPaid = masterRepo.sumPaidByArea(areaId);

        totalPurchase = totalPurchase != null ? totalPurchase : BigDecimal.ZERO;
        totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;

        area.setPurchaseAmount(totalPurchase);
        area.setPaidAmount(totalPaid);
        area.setDueAmount(totalPurchase.subtract(totalPaid));

        return areaRepository.save(area);
    }

    public Area getAreaSummary(Long areaId) {
        return recalculateArea(areaId);
    }

    public List<Area> getAllAreas() {
        return areaRepository.findAll();
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

            area = areaRepository.save(area);
        }

        return area;
    }



    public void deleteArea(Long id) {
        Area area = getAreaById(id);
        areaRepository.delete(area);
    }



}
