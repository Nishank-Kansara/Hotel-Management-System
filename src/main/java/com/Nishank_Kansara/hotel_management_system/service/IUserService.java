package com.Nishank_Kansara.hotel_management_system.service;

import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import com.Nishank_Kansara.hotel_management_system.model.Room;
import com.Nishank_Kansara.hotel_management_system.model.User;

import java.util.List;

public interface IUserService {
    User registerUser(User user);
    List<User> getUsers();
    void deleteUser(String email);
    User getUserByEmail(String email);
    List<BookedRoom> getBookingByEmail(String email);

    void updateUserPassword(User user);

    void changePassword(String email, String oldPassword, String newPassword) ;
}
