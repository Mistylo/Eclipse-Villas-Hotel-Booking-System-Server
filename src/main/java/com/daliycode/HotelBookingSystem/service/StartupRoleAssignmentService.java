package com.daliycode.HotelBookingSystem.service;

import com.daliycode.HotelBookingSystem.model.Role;
import com.daliycode.HotelBookingSystem.model.User;
import com.daliycode.HotelBookingSystem.repository.RoleRepository;
import com.daliycode.HotelBookingSystem.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class StartupRoleAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(StartupRoleAssignmentService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public StartupRoleAssignmentService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public boolean isRoleAssignedToUser(Long userId, Long roleId) {
        return userRepository.existsByUserIdAndRoleId(userId, roleId);
    }

    @PostConstruct
    public void assignRoleToUser() {
        Long userId = 5L; 
        Long roleId = 2L;  

        if (!isRoleAssignedToUser(userId, roleId)) {

            Optional<User> userOptional = userRepository.findById(userId);
            Optional<Role> roleOptional = roleRepository.findById(roleId);

            if (userOptional.isPresent() && roleOptional.isPresent()) {
                User user = userOptional.get();
                Role role = roleOptional.get();
                user.getRoles().add(role);
                userRepository.save(user);

                logger.info("Role {} assigned to User {}", role.getName(), user.getFirstName());
            } else {
                throw new RuntimeException("User or Role not found");
            }
        } else {
            logger.info("Role {} already assigned to User {}", roleId, userId);
        }
    }
}
