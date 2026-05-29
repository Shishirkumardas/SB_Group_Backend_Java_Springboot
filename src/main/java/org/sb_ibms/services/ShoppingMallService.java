package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.MallListDTO;
import org.sb_ibms.dto.ShoppingMallCustomerFormDTO;
import org.sb_ibms.dto.ShoppingMallRequest;
import org.sb_ibms.models.*;
import org.sb_ibms.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingMallService {

    private final ShoppingMallCustomerRepository customerRepo;
    private final ShoppingMallSaleRepository saleRepo;
    private final RewardService rewardService;
    private final ShoppingMallAreaService areaService;
    private final ShoppingMallCustomerRepository shoppingMallCustomerRepository;
    private final ShoppingMallRepository shoppingMallRepository;
    private final UserShoppingMallMappingRepository mappingRepository;
    private final ShoppingMallContext shoppingMallContext;
    private final UserRepository userRepository;

    // ==================== Customer Related ====================

    public ShoppingMallCustomer createCustomer(ShoppingMallCustomer customer) {
        return customerRepo.save(customer);
    }

    @Transactional
    public ShoppingMallCustomer saveCustomerForm(ShoppingMallCustomerFormDTO dto) {
        ShoppingMallCustomer md = new ShoppingMallCustomer();
        ShoppingMallArea area = areaService.getAreaById(dto.getAreaID());

        md.setArea(area);
        md.setName(dto.getCustomerName());
        md.setPhone(dto.getPhoneNumber());
        md.setNid(dto.getNid());

        ShoppingMallCustomer saved = shoppingMallCustomerRepository.save(md);
        areaService.recalculateArea(area.getId());
        return saved;
    }

    // ==================== Shopping Mall CRUD ====================

    @Transactional
    public ShoppingMall createShoppingMall(ShoppingMallRequest request, String adminId) {
        ShoppingMall mall = new ShoppingMall();
        updateMallFromRequest(mall, request);
        return shoppingMallRepository.save(mall);
    }

    public List<ShoppingMall> getAllShoppingMalls() {
        return shoppingMallRepository.findAll();
    }

    public ShoppingMall getShoppingMallById(Long id) {
        return shoppingMallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shopping Mall not found with id: " + id));
    }

    @Transactional
    public ShoppingMall updateShoppingMall(Long id, ShoppingMallRequest request) {
        ShoppingMall mall = getShoppingMallById(id);
        updateMallFromRequest(mall, request);
        return shoppingMallRepository.save(mall);
    }

    @Transactional
    public void deleteShoppingMall(Long id) {
        shoppingMallRepository.deleteById(id);
    }

    // ==================== Manager Assignment ====================

    @Transactional
    public void assignManagerToMall(String userEmail, Long mallId, String assignedBy) {
        if (userEmail == null || mallId == null) {
            throw new RuntimeException("User Email and Mall ID are required");
        }

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        String userId = user.getId();   // UUID as String

        // Prevent duplicate assignment
        if (mappingRepository.existsByUserIdAndShoppingMallId(userId, mallId)) {
            throw new RuntimeException("This manager is already assigned to the mall");
        }

        UserShoppingMallMapping mapping = new UserShoppingMallMapping();
        mapping.setUserId(userId);
        mapping.setShoppingMallId(mallId);
        mapping.setAssignedBy(assignedBy);
        mappingRepository.save(mapping);
    }

    // ==================== Mall Selection ====================

    public List<MallListDTO> getMallsForCurrentUser() {
        String role = getCurrentUserRole();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return shoppingMallRepository.findAllMallsAsDTO();
        }

        if (isShoppingMallManager(role)) {
            String userId = getCurrentUserId();
            if (userId != null) {
                return shoppingMallRepository.findMallsByManagerId(userId);
            }
        }

        return List.of();
    }

    // ==================== Permission Check ====================

    public boolean isManagerAllowedForMall(String userId, Long mallId) {
        if (userId == null || mallId == null) return false;
        return mappingRepository.existsByUserIdAndShoppingMallId(userId, mallId);
    }

    // ==================== Helper Methods ====================

    private void updateMallFromRequest(ShoppingMall mall, ShoppingMallRequest req) {
        mall.setName(req.getName());
        mall.setAreaName(req.getAreaName());
        mall.setAddress(req.getAddress());
        mall.setPhone(req.getPhone());
        mall.setEmail(req.getEmail());
        mall.setLocation(req.getLocation());
        mall.setTotalShops(req.getTotalShops());
        mall.setOpeningDate(req.getOpeningDate());
        mall.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        mall.setOwnerId(req.getOwnerId());
    }

    private String getCurrentUserRole() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getRole();
        }
        return null;
    }

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            String idStr = user.getId();
            if (idStr == null || idStr.trim().isEmpty() || "null".equals(idStr)) {
                return null;
            }
            return idStr;
        }
        return null;
    }

    private boolean isShoppingMallManager(String role) {
        if (role == null) return false;
        return "SHOPPING_MALL_MANAGER".equals(role) ||
                "SHOPPING_MALL_ASSISTANT".equals(role);
    }
}