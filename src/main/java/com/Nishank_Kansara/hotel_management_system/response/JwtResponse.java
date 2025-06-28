package com.Nishank_Kansara.hotel_management_system.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class JwtResponse {
    private Long id;
    private String email;
    private String token;
    private List<String> roles;
    private String type = "Bearer";

    public JwtResponse(Long id, String email, String token, List<String> roles) {
        this.id = id;
        this.email = email;
        this.token = token;
        this.roles = roles;
    }
}
