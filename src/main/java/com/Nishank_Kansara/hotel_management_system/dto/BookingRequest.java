package com.Nishank_Kansara.hotel_management_system.dto;


import com.Nishank_Kansara.hotel_management_system.model.BookedRoom;
import lombok.Data;

@Data
public class BookingRequest {
    private double totalAmount;
    private BookedRoom bookedRoom;
}
