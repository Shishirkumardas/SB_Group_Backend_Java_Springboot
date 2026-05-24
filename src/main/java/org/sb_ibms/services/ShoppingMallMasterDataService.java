package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sb_ibms.ResourceNotFoundException;
import org.sb_ibms.dto.CustomerFormDTO;
import org.sb_ibms.dto.ShoppingMallCustomerFormDTO;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.models.Area;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ShoppingMallMasterDataService {
    private ShoppingMallCustomerRepository shoppingMallmasterDataRepository;
    private AreaService areaService;


    public ShoppingMallCustomer create(ShoppingMallCustomer data) {
        if (data.getPurchaseAmount() != null && data.getPaidAmount() != null) {
            data.setDueAmount(data.getAmountBackFromPurchase());
        }

        ShoppingMallCustomer saved = shoppingMallmasterDataRepository.save(data);
        areaService.recalculateArea(data.getArea().getId());

        return saved;
    }

    public ShoppingMallCustomer updateMasterData(Long id, ShoppingMallCustomer masterDataDetails) {
        ShoppingMallCustomer masterData = shoppingMallmasterDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterData not found"));
        masterData.setName(masterDataDetails.getName());
        masterData.setArea(masterDataDetails.getArea());
        masterData.setPhone(masterDataDetails.getPhone());
        masterData.setNid(masterDataDetails.getNid());

        masterData.setPaymentMethod(masterDataDetails.getPaymentMethod());
        masterData.setDate(masterDataDetails.getDate());
        masterData.setPurchaseAmount(masterDataDetails.getPurchaseAmount());
        masterData.setCashBackAmount(masterDataDetails.getCashBackAmount());
        ShoppingMallCustomer saved  = shoppingMallmasterDataRepository.save(masterData);
        areaService.recalculateArea(masterData.getArea().getId());
        areaService.recalculateArea(saved.getArea().getId());
        return saved;
    }

    public ShoppingMallCustomer getMasterDataById(Long id) {
        return shoppingMallmasterDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found"));
    }

    public ShoppingMallCustomer getMasterDataByName(String name) {
        return shoppingMallmasterDataRepository.findByName(name);
    }
    public void delete(Long id) {
        ShoppingMallCustomer data = shoppingMallmasterDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MasterData not found"));

        Long areaId = data.getArea().getId();
        shoppingMallmasterDataRepository.deleteById(id);

        areaService.recalculateArea(areaId);
    }
    @Transactional
    public ShoppingMallCustomer saveCustomerForm(ShoppingMallCustomerFormDTO dto) {
        ShoppingMallCustomer md =  new ShoppingMallCustomer();
        Area area = areaService.getAreaById(dto.getAreaID());
        md.setArea(area);
        md.setName(dto.getCustomerName());
        md.setArea(area);
        md.setPhone(dto.getPhoneNumber());
        md.setNid(dto.getNid());

        ShoppingMallCustomer saved = shoppingMallmasterDataRepository.save(md);
        areaService.recalculateArea(area.getId());
        return saved;

    }
}
