package org.sb_ibms.services;

import jakarta.transaction.Transactional;

import lombok.AllArgsConstructor;
import org.sb_ibms.ResourceNotFoundException;

import org.sb_ibms.dto.CustomerFormDTO;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.models.Area;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.repositories.MasterDataRepository;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class MasterDataService {

    private MasterDataRepository masterDataRepository;
    private AreaService areaService;


    public MasterData create(MasterData data) {
        if (data.getPurchaseAmount() != null && data.getPaidAmount() != null) {
            data.setDueAmount(data.getAmountBackFromPurchase());
        }

        MasterData saved = masterDataRepository.save(data);
        areaService.recalculateArea(data.getArea().getId());

        return saved;
    }

    public MasterData updateMasterData(Long id, MasterData masterDataDetails) {
        MasterData masterData = masterDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterData not found"));
        masterData.setName(masterDataDetails.getName());
        masterData.setArea(masterDataDetails.getArea());
        masterData.setPhone(masterDataDetails.getPhone());
        masterData.setNid(masterDataDetails.getNid());

        masterData.setPaymentMethod(masterDataDetails.getPaymentMethod());
        masterData.setDate(masterDataDetails.getDate());
        masterData.setPurchaseAmount(masterDataDetails.getPurchaseAmount());
        masterData.setCashBackAmount(masterDataDetails.getCashBackAmount());
        MasterData saved  = masterDataRepository.save(masterData);
        areaService.recalculateArea(masterData.getArea().getId());
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

        areaService.recalculateArea(areaId);
    }
    @Transactional
    public MasterData saveCustomerForm(CustomerFormDTO dto) {
        MasterData md =  new MasterData();
        Area area = areaService.getAreaById(dto.getAreaID());
        md.setArea(area);
        md.setName(dto.getCustomerName());
        md.setArea(area);
        md.setPhone(dto.getPhoneNumber());
        md.setNid(dto.getNid());
        md.setQuantity(dto.getQuantity());
        md.setDate(dto.getPaymentDate());
        md.setPaymentMethod(dto.getPaymentMethod());
        md.setPurchaseAmount(dto.getAmount());
        md.setPaidAmount(dto.getAmount());
        md.setAmountBackFromPurchase(dto.getAmount());


        if (dto.getPhoneNumber() != null && dto.getPaymentMethod()==PaymentMethod.BKASH) {
            md.setBkashNumber(dto.getPhoneNumber());
        } else if (dto.getPhoneNumber() != null && dto.getPaymentMethod()==PaymentMethod.ROCKET) {
            md.setRocketNumber(dto.getPhoneNumber());
        } else if (dto.getPhoneNumber() != null && dto.getPaymentMethod()==PaymentMethod.NAGAD) {
            md.setNogodNumber(dto.getPhoneNumber());
        }

        MasterData saved = masterDataRepository.save(md);
        areaService.recalculateArea(area.getId());
        return saved;

    }
}
