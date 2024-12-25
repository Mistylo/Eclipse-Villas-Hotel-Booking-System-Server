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

    // 假设你已经知道了 userId 和 adminRoleId
    @PostMapping("/assign-admin/{userId}")
    public ResponseEntity<User> assignAdminRole(@PathVariable Long userId) {
        // 假设 admin 角色的 ID 是 1
        Long adminRoleId = 1L;

        try {
            // 调用 UserService 的方法来分配 admin 角色
            User updatedUser = userServiceImpl.assignRoleToUser(userId, adminRoleId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
