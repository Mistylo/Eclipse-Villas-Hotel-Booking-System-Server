package com.daliycode.HotelBookingSystem.service;

import com.daliycode.HotelBookingSystem.exception.InvalidBookingRequestException;
import com.daliycode.HotelBookingSystem.exception.ResourceNotFoundException;
import com.daliycode.HotelBookingSystem.model.BookedRoom;
import com.daliycode.HotelBookingSystem.model.Room;
import com.daliycode.HotelBookingSystem.repository.BookedRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements IBookingService {

    private final BookedRoomRepository bookedRoomRepository;
    private final IRoomService roomService;

    @Override
    public List<BookedRoom> getAllBookings() {
        return bookedRoomRepository.findAll();
    }


    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        List<BookedRoom> bookings = bookedRoomRepository.findByRoomId(roomId);
        return bookings != null ? bookings : new ArrayList<>();
    }

    @Override
    public void cancelBooking(Long bookingId) {
        bookedRoomRepository.deleteById(bookingId);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        try {
            if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
                throw new InvalidBookingRequestException("Check-in date should be earlier than check-out date.");
            }

            Room room = roomService.getRoomById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
            List<BookedRoom> existingBookings = room.getBookings();
            boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);

            if (roomIsAvailable) {
                bookingRequest.setRoom(room);
                bookingRequest.setTotalGuest(bookingRequest.getNumOfAdults() + bookingRequest.getNumOfChildren());
                room.addBooking(bookingRequest);
                bookedRoomRepository.save(bookingRequest);
                System.out.println("Booking saved successfully with booking number: " + bookingRequest.getBookingNumber());
            } else {
                throw new InvalidBookingRequestException("This room is not available for the given dates");
            }

            return bookingRequest.getBookingNumber();
        } catch (Exception e) {
            System.err.println("Error while saving booking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public BookedRoom findByBookingNumber(String bookingNumber) {
        return bookedRoomRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No booking found with this booking number :" + bookingNumber));
    }

    public List<BookedRoom> findByGuestEmail(String email) {
        return bookedRoomRepository.findByGuestEmail(email);
    }


    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream().noneMatch(existingBooking ->
                bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                        || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                        || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                        && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
                        || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                        && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))

                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                        && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))

                        || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                        && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
                );
    }



}
