package com.daliycode.HotelBookingSystem.service;

import com.daliycode.HotelBookingSystem.exception.UserNotFoundException;
import com.daliycode.HotelBookingSystem.model.Role;
import com.daliycode.HotelBookingSystem.model.User;
import com.daliycode.HotelBookingSystem.repository.RoleRepository;
import com.daliycode.HotelBookingSystem.repository.UserRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();  // Use BCryptPasswordEncoder for security
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        // Check if user already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException(user.getEmail() + " already registered");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Collections.singleton(userRole));

        return userRepository.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteUser(String email) {
        User user = getUserOrElseThrow(email);
        userRepository.deleteByEmail(email);
    }

    @Override
    public Optional<User> getUser(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getUserOrElseThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByRefreshToken(@Param("refreshToken") String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public Optional<String> getStoredRefreshToken(String refreshToken) {
        Optional<User> userOptional = userRepository.findByRefreshToken(refreshToken);
        return userOptional.map(User::getRefreshToken);
    }

    public User assignRoleToUser(Long userId, Long roleId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Role> role = roleRepository.findById(roleId);

        if (user.isPresent() && role.isPresent()) {
            User existingUser = user.get();
            Role adminRole = role.get();

            if (!existingUser.getRoles().contains(adminRole)) {
                existingUser.getRoles().add(adminRole);
                userRepository.save(existingUser);
            }
            return existingUser;
        } else {
            throw new RuntimeException("User or Role not found");
        }
    }



}

