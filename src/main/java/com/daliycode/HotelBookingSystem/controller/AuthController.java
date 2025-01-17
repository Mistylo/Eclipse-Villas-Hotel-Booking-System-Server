package com.daliycode.HotelBookingSystem.controller;

import com.daliycode.HotelBookingSystem.exception.UserAlreadyExistException;
import com.daliycode.HotelBookingSystem.model.User;
import com.daliycode.HotelBookingSystem.request.LoginRequest;
import com.daliycode.HotelBookingSystem.request.RefreshTokenRequest;
import com.daliycode.HotelBookingSystem.security.jwt.JwtUtils;
import com.daliycode.HotelBookingSystem.security.user.HotelUserDetails;
import com.daliycode.HotelBookingSystem.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("Registration Successful");
        } catch (UserAlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate Access Token and Refresh Token
        String jwt = jwtUtils.generateJwtTokenForUser(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);
        Date refreshTokenExpiration = jwtUtils.getExpirationDateFromToken(refreshToken);


        // 处理过期的 refreshToken
        User user = userService.getUserOrElseThrow(((HotelUserDetails) authentication.getPrincipal()).getUsername());
        if (user.getRefreshToken() != null && isTokenExpired(user.getRefreshToken())) {
            user.setRefreshToken(null); // Clear old refresh token
        }

        user.setRefreshToken(refreshToken); // save new refresh token
        userService.save(user);

        redisTemplate.opsForValue().set("refreshToken:" + user.getUsername(), refreshToken,
                refreshTokenExpiration.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        return ResponseEntity.ok(Map.of(
                "accessToken", jwt,
                "refreshToken", refreshToken,
                "refreshTokenExpiration", refreshTokenExpiration
        ));
    }


    public boolean isTokenExpired(String refreshToken) {
        Date expirationDate = jwtUtils.getExpirationDateFromToken(refreshToken);
        return expirationDate.before(new Date());
    }

@PostMapping("/refresh-token")
public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();

    String storedRefreshToken = redisTemplate.opsForValue().get("refreshToken:" + refreshToken);
    if (storedRefreshToken == null) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or expired refresh token");
    }

    if (!jwtUtils.validateRefreshToken(refreshToken, storedRefreshToken)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid refresh token");
    }

    // Check if refresh token is expired
    String username = jwtUtils.getUsernameFromToken(refreshToken);
    User user = userService.getUserOrElseThrow(username);

    if (isTokenExpired(refreshToken)) {
        // If refresh token is expired, clear it and return 401
        user.setRefreshToken(null);
        userService.save(user);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
    }

    // Generate new Access Token and Refresh Token
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities());
    String newAccessToken = jwtUtils.generateJwtTokenForUser(authentication);
    String newRefreshToken = jwtUtils.generateRefreshToken(authentication);

    // Update refresh token in Redis
    redisTemplate.opsForValue().set("refreshToken:" + user.getUsername(), newRefreshToken, jwtUtils.getExpirationDateFromToken(newRefreshToken).getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

    // Update refresh token in database
    user.setRefreshToken(newRefreshToken);
    userService.save(user);

    return ResponseEntity.ok(Map.of(
            "accessToken", newAccessToken,
            "refreshToken", newRefreshToken
    ));
}
}
