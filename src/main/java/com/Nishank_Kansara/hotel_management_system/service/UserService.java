package com.Nishank_Kansara.hotel_management_system.service;

import com.Nishank_Kansara.hotel_management_system.exception.ResourceNotFoundException;
import com.Nishank_Kansara.hotel_management_system.exception.UserAlreadyExistException;
import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import com.Nishank_Kansara.hotel_management_system.model.Role;
import com.Nishank_Kansara.hotel_management_system.model.Room;
import com.Nishank_Kansara.hotel_management_system.model.User;
import com.Nishank_Kansara.hotel_management_system.repository.BookingRepository;
import com.Nishank_Kansara.hotel_management_system.repository.RoleRepository;
import com.Nishank_Kansara.hotel_management_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public User registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())){
            throw new UserAlreadyExistException(user.getEmail()+" already Exists!!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER").get();
        user.setRoles(Collections.singletonList(userRole));
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public void deleteUser(String email) {
        User theUser=getUserByEmail(email);
        if(theUser!=null) {
            userRepository.deleteByEmail(email);
        }
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found"));
    }

    @Override
    public List<BookedRoom> getBookingByEmail(String email) {
        return bookingRepository.findByGuestEmail(email);
    }

    @Override
    public void updateUserPassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // verify raw oldPassword matches
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // encode & save new
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
