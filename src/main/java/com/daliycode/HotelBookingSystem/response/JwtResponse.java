package com.daliycode.HotelBookingSystem.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JwtResponse {
    private Long id;
    private String email;
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private List<String> roles;
    private Long remainingTime;

    public JwtResponse(Long id, String email, String token, String refreshToken,List<String> roles,
                       Long remainingTime) {
        this.id = id;
        this.email = email;
        this.token = token;
        this.refreshToken = refreshToken;
        this.roles = roles;
        this.remainingTime = remainingTime;
    }

}
