package org.example.sbgroup2.services;

import jakarta.transaction.Transactional;

import org.example.sbgroup2.ResourceNotFoundException;

import org.example.sbgroup2.dto.CustomerFormDTO;
import org.example.sbgroup2.enums.OrderStatus;
import org.example.sbgroup2.enums.PaymentMethod;
import org.example.sbgroup2.models.Area;
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
            data.setDueAmount(data.getAmountBackFromPurchase());
        }

        MasterData saved = masterDataRepository.save(data);

        // 🔥 auto-update area
        areaService.recalculateArea(data.getArea().getId());

        return saved;
    }

//    public List<CashbackDetailsDTO> getMasterDataByNextDueDate(LocalDate nextDueDate) {
//        // 1. fetch all master data
//        List<MasterData> allMasters = masterDataRepository.findAll();
//
//        // 2. map each to CashbackDetailsDTO
//        List<CashbackDetailsDTO> allWithCashback = allMasters.stream()
//                .map(cashbackService::calculateCashback2)
//                .collect(Collectors.toList());
//
//        // 3. filter by nextDueDate if provided
//        if (nextDueDate != null) {
//            return allWithCashback.stream()
//                    .filter(dto -> dto.getNextDueDate() != null && dto.getNextDueDate().equals(nextDueDate))
//                    .collect(Collectors.toList());
//        }
//
//        return allWithCashback; // default: return all
//    }
//public List<MasterData> getMasterDataByNextDueDate(LocalDate nextDueDate) {
//    // 1. Fetch all master data
//    List<MasterData> allMasters = masterDataRepository.findAll();
//
//    // 2. Filter by nextDueDate if provided
//    if (nextDueDate != null) {
//        return allMasters.stream()
//                .filter(m -> m.getNextDueDate() != null && m.getNextDueDate().equals(nextDueDate))
//                .collect(Collectors.toList());
//    }
//
//    // Default: return all
//    return allMasters;
//}

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
//        areaService.recalculateArea(saved.getArea().getId());
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
