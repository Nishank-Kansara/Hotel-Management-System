package com.Nishank_Kansara.hotel_management_system.service;

import com.Nishank_Kansara.hotel_management_system.exception.InvalidBookingRequestException;
import com.Nishank_Kansara.hotel_management_system.exception.ResourceNotFoundException;
import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import com.Nishank_Kansara.hotel_management_system.model.Room;
import com.Nishank_Kansara.hotel_management_system.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {

    private final BookingRepository bookingRepository;
    private final IRoomService roomService;

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode).orElseThrow(() -> new ResourceNotFoundException("Booking not found with confirmation code: " + confirmationCode));
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
            throw new InvalidBookingRequestException("Check-out date must come after check-in date");
        }

        Optional<Room> optionalRoom = roomService.getRoomById(roomId);
        if (optionalRoom.isEmpty()) {
            throw new InvalidBookingRequestException("Room not found with ID: " + roomId);
        }

        Room room = optionalRoom.get();
        List<BookedRoom> existingBookings = room.getBookings();

        if (roomIsAvailable(bookingRequest, existingBookings)) {
            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
            return bookingRequest.getBookingConfirmationCode();
        } else {
            throw new InvalidBookingRequestException("Room is not available for the requested dates");
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public List<BookedRoom> getBookingsByUserEmail(String email) {
        return bookingRepository.findByGuestEmail(email);
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream().noneMatch(existingBooking ->
                bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()) &&
                        bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckInDate())
        );
    }
}
