
package com.daliycode.HotelBookingSystem.service;

import com.daliycode.HotelBookingSystem.model.User;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User registerUser(User user);
    List<User> getUsers();


    void deleteUser(String email);
    Optional<User> getUser(String email);
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    boolean existsByEmail(String mail);

    User save(User user);
    User getUserOrElseThrow(String email);

    Optional<User> findByRefreshToken(@Param("refreshToken") String refreshToken);

    Optional<String> getStoredRefreshToken(String refreshToken);

    User assignRoleToUser(Long userId, Long roleId);
}
