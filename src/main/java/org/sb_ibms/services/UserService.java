package org.sb_ibms.services;

import org.sb_ibms.dto.userDTO;
import org.sb_ibms.enums.Role;
import org.sb_ibms.models.User;
import org.sb_ibms.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(userDTO dto) {
        // Email already exists
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        User user = mapToEntity(dto);

        // Hierarchy validation
        if (isHierarchicalRole(user.getRole())) {
            validateHierarchy(user);
        }

        // Shopping Mall role validation
        validateShoppingMallRole(user);

        // Password encode
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        return userRepo.save(user);
    }

    /**
     * Hierarchy Validation (GM → AGM → ME)
     */
    private void validateHierarchy(User user) {
        User manager = user.getManager();

        if (manager == null) {
            if (user.getRole() != Role.GM) {
                throw new IllegalArgumentException("Only GM can have no manager. "
                        + user.getRole() + " must have a manager.");
            }
            return;
        }

        // Manager must exist in database
        if (manager.getId() == null || !userRepo.existsById(manager.getId())) {
            throw new IllegalArgumentException("Manager with ID " + manager.getId() + " does not exist");
        }

        Role managerRole = manager.getRole();

        if (user.getRole() == Role.AGM && managerRole != Role.GM) {
            throw new IllegalArgumentException("AGM must report to GM only");
        }

        if (user.getRole() == Role.ME && managerRole != Role.AGM) {
            throw new IllegalArgumentException("ME must report to AGM only");
        }

        if (user.getRole() == Role.DEALER && managerRole != Role.GM && managerRole != Role.AGM) {
            throw new IllegalArgumentException("Dealer must report to GM or AGM");
        }
    }

    /**
     * Shopping Mall Role Validation
     */
    private void validateShoppingMallRole(User user) {
        if (user.getRole() == Role.SHOPPING_MALL_CUSTOMER ||
                user.getRole() == Role.SHOPPING_MALL_MANAGER ||
                user.getRole() == Role.SHOPPING_MALL_ASSISTANT) {

            if (user.getShoppingMallId() == null) {
                throw new IllegalArgumentException("Shopping Mall roles must have shoppingMallId");
            }
        } else {
            // অন্য রোলের জন্য shoppingMallId না থাকাই ভালো
            if (user.getShoppingMallId() != null) {
                throw new IllegalArgumentException("Only Shopping Mall roles can have shoppingMallId");
            }
        }
    }

    private boolean isHierarchicalRole(Role r) {
        return r == Role.GM || r == Role.AGM || r == Role.ME || r == Role.DEALER;
    }

    /**
     * DTO থেকে Entity তে ম্যাপ করা
     */
    private User mapToEntity(userDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setRole(dto.getRole());
        user.setEmployeeCode(dto.getEmployeeCode());
        user.setShoppingMallId(dto.getShoppingMallId());

        // Manager set করা (managerId থেকে)
        if (dto.getManagerId() != null) {
            Optional<User> managerOpt = userRepo.findById(dto.getManagerId());
            if (managerOpt.isPresent()) {
                user.setManager(managerOpt.get());
            } else {
                throw new IllegalArgumentException("Manager not found with ID: " + dto.getManagerId());
            }
        }

        return user;
    }
    public List<User> getSubordinates(String managerId) {
        return userRepo.findByManagerId(managerId);
    }
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Optional<User> findById(String id) {
        return userRepo.findById(id);
    }
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}