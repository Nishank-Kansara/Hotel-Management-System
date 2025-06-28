package com.Nishank_Kansara.hotel_management_system.repository;


import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface BookingRepository extends JpaRepository<BookedRoom,Long> {

    Optional<BookedRoom> findByBookingConfirmationCode(String bookingConfirmationCode);


    List<BookedRoom>findByRoomId(Long roomId);

    List<BookedRoom> findByGuestEmail(String email);
}
