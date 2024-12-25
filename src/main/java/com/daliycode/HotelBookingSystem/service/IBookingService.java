package com.daliycode.HotelBookingSystem.service;

import com.daliycode.HotelBookingSystem.model.BookedRoom;

import java.util.List;

public interface IBookingService {
    void cancelBooking(Long bookingId);

    String saveBooking(Long roomId, BookedRoom bookingRequest);

    BookedRoom findByBookingNumber(String bookingNumber);

    List<BookedRoom> getAllBookings();

    List<BookedRoom> findByGuestEmail(String email);
}
