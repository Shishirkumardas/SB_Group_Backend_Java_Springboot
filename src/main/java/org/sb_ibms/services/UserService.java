package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.dto.userDTO;
import org.sb_ibms.enums.Role;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(userDTO dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        User user = mapToEntity(dto);

        // Hierarchy & Role Validation
        if (isHierarchicalRole(user.getRole())) {
            validateHierarchy(user);
        }
        validateShoppingMallRole(user);

        // Encode password
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepo.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        User existing = userRepo.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update fields
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setPhoneNumber(user.getPhoneNumber());
        existing.setAddress(user.getAddress());
        existing.setRole(user.getRole());
        existing.setEmployeeCode(user.getEmployeeCode());
        existing.setShoppingMallId(user.getShoppingMallId());

        if (user.getManager() != null) {
            existing.setManager(user.getManager());
        }

        // Re-validate hierarchy if role changed
        if (isHierarchicalRole(existing.getRole())) {
            validateHierarchy(existing);
        }
        validateShoppingMallRole(existing);

        // Password update (only if provided)
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepo.save(existing);
    }

    // ====================== HIERARCHY VALIDATION ======================
    private void validateHierarchy(User user) {
        User manager = user.getManager();

        if (manager == null) {
            if (user.getRole() != Role.DMD) {
                throw new IllegalArgumentException("Only DMD can have no manager.");
            }
            return;
        }

        if (!userRepo.existsById(manager.getId())) {
            throw new IllegalArgumentException("Manager with ID " + manager.getId() + " does not exist");
        }

        Role userRole = user.getRole();
        Role managerRole = manager.getRole();

        switch (userRole) {
            case PD -> { if (managerRole != Role.DMD) throw new IllegalArgumentException("PD must report to DMD"); }
            case GM -> { if (managerRole != Role.PD && managerRole != Role.DMD) throw new IllegalArgumentException("GM must report to PD or DMD"); }
            case AGM -> { if (managerRole != Role.GM) throw new IllegalArgumentException("AGM must report to GM"); }
            case MM -> { if (managerRole != Role.AGM) throw new IllegalArgumentException("MM must report to AGM"); }
            case RSM -> { if (managerRole != Role.MM && managerRole != Role.AGM) throw new IllegalArgumentException("RSM must report to MM or AGM"); }
            case ME -> { if (managerRole != Role.RSM && managerRole != Role.MM) throw new IllegalArgumentException("ME must report to RSM or MM"); }
            case DEALER -> {
                if (managerRole != Role.GM && managerRole != Role.AGM &&
                        managerRole != Role.MM && managerRole != Role.RSM)
                    throw new IllegalArgumentException("Dealer must report to GM, AGM, MM or RSM");
            }
        }
    }

    private boolean isHierarchicalRole(Role role) {
        return role == Role.DMD || role == Role.PD || role == Role.GM ||
                role == Role.AGM || role == Role.MM || role == Role.RSM ||
                role == Role.ME || role == Role.DEALER;
    }

    private void validateShoppingMallRole(User user) {
        boolean isShoppingMallRole = user.getRole() == Role.SHOPPING_MALL_CUSTOMER ||
                user.getRole() == Role.SHOPPING_MALL_MANAGER ||
                user.getRole() == Role.SHOPPING_MALL_ASSISTANT;

        if (isShoppingMallRole && user.getShoppingMallId() == null) {
            throw new IllegalArgumentException("Shopping Mall roles must have shoppingMallId");
        }
        if (!isShoppingMallRole && user.getShoppingMallId() != null) {
            throw new IllegalArgumentException("Only Shopping Mall roles can have shoppingMallId");
        }
    }

    private User mapToEntity(userDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(dto.getRole());
        user.setEmployeeCode(dto.getEmployeeCode());
        user.setShoppingMallId(dto.getShoppingMallId());

        if (dto.getManagerId() != null) {
            userRepo.findById(dto.getManagerId()).ifPresent(user::setManager);
        }

        return user;
    }

    // ====================== QUERY METHODS ======================
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<User> findById(String id) {
        return userRepo.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public List<User> getSubordinates(String managerId) {
        return userRepo.findByManagerId(managerId);
    }

    // Recursive Subordinates (For Tree View)
    public User getUserWithSubordinates(String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> subs = getSubordinates(id);
        user.setSubordinates(subs);   // Make sure User entity has this field

        // Recursively load deeper levels (optional - can be limited)
        for (User sub : subs) {
            sub.setSubordinates(getSubordinates(sub.getId()));
        }

        return user;
    }

    // Performance Data (You can enhance this with real calculations later)
    public void updatePerformanceMetrics(String userId, BigDecimal netSale, BigDecimal profit, BigDecimal commission) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setNetSale(netSale);
        user.setProfit(profit);
        user.setCommission(commission);

        userRepo.save(user);
    }
}