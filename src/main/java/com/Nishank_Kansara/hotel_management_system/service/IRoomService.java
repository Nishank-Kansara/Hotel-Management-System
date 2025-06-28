package com.Nishank_Kansara.hotel_management_system.service;

import com.Nishank_Kansara.hotel_management_system.model.Room;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IRoomService {
    Room addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice) throws IOException;

    List<String> getAllRoomTypes();

    List<Room> getAllRooms();

    byte[] getRoomPhotoByRoomId(Long roomId);

    void deleteRoom(Long roomId);

    Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes);

    Optional<Room> getRoomById(Long roomId);

    boolean isOverlapping(LocalDate reqCheckIn, LocalDate reqCheckOut,
                          LocalDate bookingCheckIn, LocalDate bookingCheckOut);

    List<Room> findAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate);
}
