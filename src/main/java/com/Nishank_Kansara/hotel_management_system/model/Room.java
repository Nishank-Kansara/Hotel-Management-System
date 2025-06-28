package com.Nishank_Kansara.hotel_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomType;

    private BigDecimal roomPrice;

    private boolean isBooked;

    @Lob
    @Column(name = "photo", columnDefinition = "BYTEA") // ✅ PostgreSQL compatible
    private byte[] photo;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookedRoom> bookings = new ArrayList<>();

    public void addBooking(BookedRoom booking) {
        bookings.add(booking);
        booking.setRoom(this);
        isBooked = true;

        String bookingCode = RandomStringUtils.randomNumeric(10);
        booking.setBookingConfirmationCode(bookingCode);
    }
}
