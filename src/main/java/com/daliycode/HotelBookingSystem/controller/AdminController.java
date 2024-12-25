package com.daliycode.HotelBookingSystem.controller;

import com.daliycode.HotelBookingSystem.model.User;
import com.daliycode.HotelBookingSystem.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @PostMapping("/assign-admin/{userId}")
    public ResponseEntity<User> assignAdminRole(@PathVariable Long userId) {

        Long adminRoleId = 1L;

        try {
            User updatedUser = userServiceImpl.assignRoleToUser(userId, adminRoleId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
