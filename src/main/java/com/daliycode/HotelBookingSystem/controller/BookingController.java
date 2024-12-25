package com.daliycode.HotelBookingSystem.controller;

import com.daliycode.HotelBookingSystem.exception.InvalidBookingRequestException;
import com.daliycode.HotelBookingSystem.exception.ResourceNotFoundException;
import com.daliycode.HotelBookingSystem.model.BookedRoom;
import com.daliycode.HotelBookingSystem.model.Room;
import com.daliycode.HotelBookingSystem.response.BookingResponse;
import com.daliycode.HotelBookingSystem.response.RoomResponse;
import com.daliycode.HotelBookingSystem.security.jwt.JwtUtils;
import com.daliycode.HotelBookingSystem.service.IBookingService;
import com.daliycode.HotelBookingSystem.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/bookings")

public class BookingController {
    private final IBookingService bookingService;
    private final IRoomService roomService;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;


    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }
        return ResponseEntity.ok(bookingResponses);
    }

    @GetMapping("/confirmation/{bookingNumber}")
    public ResponseEntity<?> getBookingByBookingNumber(@PathVariable String bookingNumber) {
        try {
            BookedRoom booking = bookingService.findByBookingNumber(bookingNumber);
            BookingResponse bookingResponse = getBookingResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        } catch (ResourceNotFoundException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

@PostMapping("/room/{roomId}/booking")
public ResponseEntity<?> saveBooking(@PathVariable Long roomId, @RequestBody BookedRoom bookingRequest) {
    try {
        String bookingNumber = bookingService.saveBooking(roomId, bookingRequest);
        return ResponseEntity.ok("Room booked successfully! Your booking number is : " + bookingNumber);
    } catch (InvalidBookingRequestException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


    @DeleteMapping("/booking/{bookingId}/cancel")
    public void cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);

    }

    @GetMapping("/user/{email}/bookings")
    public ResponseEntity<?> getBookingsByEmail(@PathVariable("email") String email,
                                                @RequestHeader("Authorization") String authorization) {
        try {

            String token = authorization.replace("Bearer ", "");

            //Check if token is valid or expired
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("message", "Invalid or expired token"));
            }

            // Get username from token
            String username = jwtUtils.getUsernameFromToken(token);

            // Get bookings by guest email
            List<BookedRoom> bookings = bookingService.findByGuestEmail(email);

            // If no bookings found, return empty list
            if (bookings.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // If bookings found, create BookingResponse objects and return as list
            List<BookingResponse> bookingResponses = bookings.stream()
                    .map(this::getBookingResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(bookingResponses);
        } catch (Exception ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse room = new RoomResponse(
                theRoom.getId(),
                theRoom.getRoomType(),
                theRoom.getRoomPrice());

        return new BookingResponse(
                booking.getBookingId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getGuestFullName(),
                booking.getGuestEmail(), booking.getNumOfAdults(),
                booking.getNumOfChildren(), booking.getTotalGuest(),
                booking.getBookingNumber(),room);
    }

}
