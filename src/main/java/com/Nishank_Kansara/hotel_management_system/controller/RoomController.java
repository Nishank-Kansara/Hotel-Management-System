package com.Nishank_Kansara.hotel_management_system.controller;

import com.Nishank_Kansara.hotel_management_system.exception.PhotoRetrievalException;
import com.Nishank_Kansara.hotel_management_system.exception.ResourceNotFoundException;
import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import com.Nishank_Kansara.hotel_management_system.model.Room;
import com.Nishank_Kansara.hotel_management_system.response.RoomResponse;
import com.Nishank_Kansara.hotel_management_system.service.BookingService;
import com.Nishank_Kansara.hotel_management_system.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final IRoomService roomService;
    private final BookingService bookingService;

    // ─── Add New Room ─────────────────────────────────────────────────────────────

    @PostMapping("/add/new-room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = toRoomResponse(savedRoom);
        return ResponseEntity.ok(response);
    }

    // ─── All Room Types ───────────────────────────────────────────────────────────

    @GetMapping("/types")
    public ResponseEntity<List<String>> getRoomTypes() {
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }

    // ─── All Rooms ────────────────────────────────────────────────────────────────

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : rooms) {
            RoomResponse response = toRoomResponse(room);
            byte[] photoBytes = room.getPhoto();
            if (photoBytes != null && photoBytes.length > 0) {
                response.setPhoto(Base64.encodeBase64String(photoBytes));
            }
            roomResponses.add(response);
        }

        return ResponseEntity.ok(roomResponses);
    }

    // ─── Delete Room ──────────────────────────────────────────────────────────────

    @DeleteMapping("/delete/room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ─── Update Room ──────────────────────────────────────────────────────────────

    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) String roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo,
                                                   @RequestParam(required = false) String removePhoto)
            throws IOException {

        BigDecimal price = null;
        if (roomPrice != null && !roomPrice.isEmpty()) {
            try {
                price = new BigDecimal(roomPrice);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        byte[] photoBytes = null;
        if ("true".equalsIgnoreCase(removePhoto)) {
            photoBytes = new byte[0]; // trigger photo removal
        } else if (photo != null && !photo.isEmpty()) {
            photoBytes = photo.getBytes();
        }

        Room updatedRoom = roomService.updateRoom(roomId, roomType, price, photoBytes);
        RoomResponse response = toRoomResponse(updatedRoom);
        if (updatedRoom.getPhoto() != null && updatedRoom.getPhoto().length > 0) {
            response.setPhoto(Base64.encodeBase64String(updatedRoom.getPhoto()));
        }

        return ResponseEntity.ok(response);
    }

    // ─── Get Room By ID ───────────────────────────────────────────────────────────

    @GetMapping("/room/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
        return roomService.getRoomById(roomId)
                .map(room -> ResponseEntity.ok(toRoomResponse(room)))
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    // ─── Available Rooms ──────────────────────────────────────────────────────────

    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut)
    {
        if (checkIn.isAfter(checkOut)) return ResponseEntity.badRequest().build();

        List<Room> availableRooms = roomService.findAvailableRooms(checkIn, checkOut);
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : availableRooms) {
            RoomResponse response = toRoomResponse(room);
            byte[] photoBytes = room.getPhoto();
            if (photoBytes != null && photoBytes.length > 0) {
                response.setPhoto(Base64.encodeBase64String(photoBytes));
            }
            roomResponses.add(response);
        }

        return ResponseEntity.ok(roomResponses);
    }

    // ─── Utility Method ───────────────────────────────────────────────────────────

    private RoomResponse toRoomResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                room.getPhoto()
        );
    }
}
