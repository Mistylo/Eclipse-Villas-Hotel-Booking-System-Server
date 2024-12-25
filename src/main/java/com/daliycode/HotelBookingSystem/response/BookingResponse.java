package com.daliycode.HotelBookingSystem.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private long bookingId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private String guestFullName;

    private String guestEmail;

    private int numOfAdults;

    private int numOfChildren;

    private int totalGuest;

    private String bookingNumber;

    private RoomResponse event;

    public BookingResponse(long bookingId, LocalDate checkInDate, LocalDate checkOutDate, String bookingNumber) {
        this.bookingId = bookingId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingNumber = bookingNumber;
    }

}
