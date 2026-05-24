package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.sb_ibms.dto.ShoppingMallCustomerFormDTO;
import org.sb_ibms.models.Area;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.models.ShoppingMallSale;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.sb_ibms.repositories.ShoppingMallSaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Transactional
public class ShoppingMallService {
    private final ShoppingMallCustomerRepository customerRepo;
    private final ShoppingMallSaleRepository saleRepo;
    private final RewardService rewardService;
    private final AreaService areaService;
    private final ShoppingMallCustomerRepository shoppingMallCustomerRepository;

    public ShoppingMallCustomer createCustomer(ShoppingMallCustomer customer) {
        return customerRepo.save(customer);
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

        ShoppingMallCustomer saved = shoppingMallCustomerRepository.save(md);
        areaService.recalculateArea(area.getId());
        return saved;
    }

    public ShoppingMallSale recordSale(ShoppingMallSale sale) {
        ShoppingMallSale saved = saleRepo.save(sale);

        // Auto issue reward card if not exists
        if (saved.getCustomer().getRewardCard() == null) {
            rewardService.issueRewardCard(saved.getCustomer().getId().toString());
        }

        // Add points
        int points = calculatePoints(saved.getNetAmount());
        rewardService.addPoints(String.valueOf(saved.getCustomer().getRewardCard().getId()), points, "Purchase #" + saved.getId());

        return saved;
    }

    private int calculatePoints(BigDecimal amount) {
        return amount.intValue() / 100;   // Example: 100 TK = 1 point
    }
}
