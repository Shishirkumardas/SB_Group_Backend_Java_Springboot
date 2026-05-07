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
            if (userRepo.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
            }

            User user = mapToEntity(dto);

            if (isHierarchicalRole(user.getRole())) {
                validateHierarchy(user);
            }

            validateShoppingMallRole(user);

            user.setPassword(passwordEncoder.encode(dto.getPassword()));

            return userRepo.save(user);
        }

        @Transactional
        public User updateUser(User user) {
            if (!userRepo.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email doesn't exists: " + user.getEmail());
            }

            if (isHierarchicalRole(user.getRole())) {
                validateHierarchy(user);
            }

            validateShoppingMallRole(user);

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            return userRepo.save(user);
        }

        /**
         * Full Hierarchy Validation - Updated for new roles
         */
        private void validateHierarchy(User user) {
            User manager = user.getManager();

            if (manager == null) {
                if (user.getRole() != Role.DMD) {
                    throw new IllegalArgumentException("Only DMD can have no manager. "
                            + user.getRole() + " must have a manager.");
                }
                return;
            }

            // if manager exists
            if (manager.getId() == null || !userRepo.existsById(manager.getId())) {
                throw new IllegalArgumentException("Manager with ID " + manager.getId() + " does not exist");
            }

            Role userRole = user.getRole();
            Role managerRole = manager.getRole();

            switch (userRole) {
                case PD:
                    if (managerRole != Role.DMD)
                        throw new IllegalArgumentException("PD must report to DMD only");
                    break;

                case GM:
                    if (managerRole != Role.PD && managerRole != Role.DMD)
                        throw new IllegalArgumentException("GM must report to PD or DMD");
                    break;

                case AGM:
                    if (managerRole != Role.GM)
                        throw new IllegalArgumentException("AGM must report to GM only");
                    break;

                case MM:
                    if (managerRole != Role.AGM)
                        throw new IllegalArgumentException("MM must report to AGM only");
                    break;

                case RSM:
                    if (managerRole != Role.MM && managerRole != Role.AGM)
                        throw new IllegalArgumentException("RSM must report to MM or AGM");
                    break;

                case ME:
                    if (managerRole != Role.RSM && managerRole != Role.MM)
                        throw new IllegalArgumentException("ME must report to RSM or MM");
                    break;

                case DEALER:
                    if (managerRole != Role.GM && managerRole != Role.AGM &&
                            managerRole != Role.MM && managerRole != Role.RSM)
                        throw new IllegalArgumentException("Dealer must report to GM, AGM, MM or RSM");
                    break;

                default:
                    // Other roles - no strict validation
                    break;
            }
        }

        private boolean isHierarchicalRole(Role r) {
            return r == Role.DMD || r == Role.PD || r == Role.GM || r == Role.AGM ||
                    r == Role.MM || r == Role.RSM || r == Role.ME || r == Role.DEALER;
        }

        private void validateShoppingMallRole(User user) {
            if (user.getRole() == Role.SHOPPING_MALL_CUSTOMER ||
                    user.getRole() == Role.SHOPPING_MALL_MANAGER ||
                    user.getRole() == Role.SHOPPING_MALL_ASSISTANT) {

                if (user.getShoppingMallId() == null) {
                    throw new IllegalArgumentException("Shopping Mall roles must have shoppingMallId");
                }
            } else {
                if (user.getShoppingMallId() != null) {
                    throw new IllegalArgumentException("Only Shopping Mall roles can have shoppingMallId");
                }
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
                Optional<User> managerOpt = userRepo.findById(dto.getManagerId());
                managerOpt.ifPresent(user::setManager);
                if (managerOpt.isEmpty()) {
                    throw new IllegalArgumentException("Manager not found with ID: " + dto.getManagerId());
                }
            }

            return user;
        }

        // ==================== Query Methods ====================
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
