package com.Nishank_Kansara.hotel_management_system.controller;

import com.Nishank_Kansara.hotel_management_system.dto.BookingRequest;
import com.Nishank_Kansara.hotel_management_system.exception.InvalidBookingRequestException;
import com.Nishank_Kansara.hotel_management_system.exception.ResourceNotFoundException;
import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import com.Nishank_Kansara.hotel_management_system.model.Room;
import com.Nishank_Kansara.hotel_management_system.response.BookingResponse;
import com.Nishank_Kansara.hotel_management_system.response.RoomResponse;
import com.Nishank_Kansara.hotel_management_system.service.BookingService;
import com.Nishank_Kansara.hotel_management_system.service.EmailService;
import com.Nishank_Kansara.hotel_management_system.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final IRoomService roomService;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_DATE;

    // 🔒 ADMIN only
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> responses = new ArrayList<>();
        for (BookedRoom b : bookings) {
            responses.add(toResponse(b));
        }
        return ResponseEntity.ok(responses);
    }

    // ✅ Authenticated users
    @GetMapping("/user/{email}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserEmail(@PathVariable String email) {
        List<BookedRoom> bookings = bookingService.getBookingsByUserEmail(email);
        List<BookingResponse> responses = new ArrayList<>();
        for (BookedRoom b : bookings) {
            responses.add(toResponse(b));
        }
        return ResponseEntity.ok(responses);
    }

    // ✅ Publicly accessible
    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        try {
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            return ResponseEntity.ok(toResponse(booking));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ✅ Authenticated users

    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(
            @PathVariable Long roomId,
            @RequestBody BookingRequest bookingRequest
    ) {
        try {
            BookedRoom bookedRoom = bookingRequest.getBookedRoom();
            double totalAmount = bookingRequest.getTotalAmount();

            String confirmationCode = bookingService.saveBooking(roomId, bookedRoom);

            emailService.sendBookingConfirmationEmail(
                    bookedRoom.getGuestEmail(),
                    bookedRoom.getCheckInDate().format(DATE_FMT),
                    bookedRoom.getCheckOutDate().format(DATE_FMT),
                    bookedRoom.getRoom().getRoomType(),
                    totalAmount,
                    bookedRoom.getBookingConfirmationCode()
            );

            return ResponseEntity.ok(
                    "Room booked successfully! Your confirmation code is: " + confirmationCode
            );
        } catch (InvalidBookingRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🔒 ADMIN only

    @DeleteMapping("/booking/{bookingId}/delete")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    // --- Helper ---
    private BookingResponse toResponse(BookedRoom booking) {
        Room r = roomService.getRoomById(booking.getRoom().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        RoomResponse rr = new RoomResponse(r.getId(), r.getRoomType(), r.getRoomPrice());

        return new BookingResponse(
                booking.getBoookingId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getGuestFullName(),
                booking.getGuestEmail(),
                booking.getNumOfAdults(),
                booking.getNumOfChildren(),
                booking.getTotalNumofGuest(),
                booking.getBookingConfirmationCode(),
                rr
        );
    }
}
