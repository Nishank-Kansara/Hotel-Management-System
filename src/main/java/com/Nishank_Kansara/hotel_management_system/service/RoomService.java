package com.Nishank_Kansara.hotel_management_system.service;

import com.Nishank_Kansara.hotel_management_system.exception.InternalServerException;
import com.Nishank_Kansara.hotel_management_system.exception.ResourceNotFoundException;
import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import com.Nishank_Kansara.hotel_management_system.model.Room;
import com.Nishank_Kansara.hotel_management_system.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService {

    private final RoomRepository roomRepository;

    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws IOException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);

        if (!file.isEmpty()) {
            room.setPhoto(file.getBytes());
        }

        return roomRepository.save(room);
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return room.getPhoto() != null ? room.getPhoto() : new byte[0];
    }

    @Override
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        roomRepository.delete(room);
    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (roomType != null) room.setRoomType(roomType);
        if (roomPrice != null) room.setRoomPrice(roomPrice);

        if (photoBytes != null) {
            room.setPhoto(photoBytes.length > 0 ? photoBytes : null); // delete if empty
        }

        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    @Override
    public boolean isOverlapping(LocalDate reqCheckIn, LocalDate reqCheckOut,
                                 LocalDate bookingCheckIn, LocalDate bookingCheckOut) {
        return !(reqCheckOut.isBefore(bookingCheckIn) || reqCheckIn.isAfter(bookingCheckOut));
    }

    @Override
    public List<Room> findAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> allRooms = roomRepository.findAll();
        List<Room> availableRooms = new ArrayList<>();

        for (Room room : allRooms) {
            boolean isAvailable = true;

            for (BookedRoom booking : room.getBookings()) {
                if (isOverlapping(checkInDate, checkOutDate,
                        booking.getCheckInDate(), booking.getCheckOutDate())) {
                    isAvailable = false;
                    break;
                }
            }

            if (isAvailable) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }
}
