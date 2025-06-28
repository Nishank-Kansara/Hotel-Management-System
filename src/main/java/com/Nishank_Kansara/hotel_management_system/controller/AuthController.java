package com.Nishank_Kansara.hotel_management_system.controller;

import com.Nishank_Kansara.hotel_management_system.exception.UserAlreadyExistException;
import com.Nishank_Kansara.hotel_management_system.model.Role;
import com.Nishank_Kansara.hotel_management_system.model.User;
import com.Nishank_Kansara.hotel_management_system.repository.RoleRepository;
import com.Nishank_Kansara.hotel_management_system.repository.UserRepository;
import com.Nishank_Kansara.hotel_management_system.request.LoginRequest;
import com.Nishank_Kansara.hotel_management_system.response.JwtResponse;
import com.Nishank_Kansara.hotel_management_system.security.jwt.JwtUtils;
import com.Nishank_Kansara.hotel_management_system.security.user.HotelUserDetails;
import com.Nishank_Kansara.hotel_management_system.service.IUserService;
import com.Nishank_Kansara.hotel_management_system.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Map to store OTP + creation timestamp
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    private record OtpData(String otp, long timestamp) {}

    // ─── REGISTER ────────────────────────────────────────────────────────────────


    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            String rawPassword = user.getPassword();
            userService.registerUser(user);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), rawPassword)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HotelUserDetails userDetails = (HotelUserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtTokenForUser(authentication);
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            JwtResponse jwtResponse = new JwtResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    jwt,
                    roles
            );

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "user",    jwtResponse
            ));
        } catch (UserAlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HotelUserDetails userDetails = (HotelUserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtTokenForUser(authentication);
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            JwtResponse jwtResponse = new JwtResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    jwt,
                    roles
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "user",    jwtResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    // ─── FORGOT‑PASSWORD (SEND OTP) ───────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStore.put(email, new OtpData(otp, System.currentTimeMillis()));
        emailService.sendSimpleMessage(email, "Password Reset OTP", otp);

        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    // ─── VERIFY‑OTP ───────────────────────────────────────────────────────────────

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp   = request.get("otp");

        OtpData data = otpStore.get(email);
        if (data == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No OTP requested"));
        }

        if (System.currentTimeMillis() - data.timestamp() > 15 * 60_000) {
            otpStore.remove(email);
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("error", "OTP expired"));
        }
        if (!data.otp().equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid OTP"));
        }

        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }

    // ─── RESET‑PASSWORD (AFTER OTP VERIFIED) ────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email       = request.get("email");
        String newPassword = request.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
        User user = userOpt.get();
        user.setPassword(newPassword);
        userService.updateUserPassword(user);   // encodes & saves
        otpStore.remove(email);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // ─── CHANGE‑PASSWORD (LOGGED‑IN USER) ────────────────────────────────────────

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload) {
        String email       = payload.get("email");
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        try {
            userService.changePassword(email, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
