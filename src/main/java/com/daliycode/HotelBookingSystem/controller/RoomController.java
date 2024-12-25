package com.daliycode.HotelBookingSystem.controller;

import com.daliycode.HotelBookingSystem.exception.PhotoRetrievalException;
import com.daliycode.HotelBookingSystem.exception.ResourceNotFoundException;
import com.daliycode.HotelBookingSystem.model.BookedRoom;
import com.daliycode.HotelBookingSystem.model.Room;
import com.daliycode.HotelBookingSystem.response.BookingResponse;
import com.daliycode.HotelBookingSystem.response.RoomResponse;
import com.daliycode.HotelBookingSystem.service.BookingServiceImpl;
import com.daliycode.HotelBookingSystem.service.IRoomService;
import com.daliycode.HotelBookingSystem.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;




@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")

public class RoomController {
    private final IRoomService roomService;
    private final BookingServiceImpl bookingService;
    private final RedisCacheService redisCacheService;
    @PostMapping("/add/new-room")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) {
        try {

            // Call service method to add a new room
            Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);

            // Return success response with room details
            RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(), savedRoom.getRoomPrice());
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            // Handle SQL exception and return a 500 error
            return ResponseEntity.status(500).body(new RoomResponse("Database error: " + e.getMessage()));
        } catch (IOException e) {
            // Handle IO exception and return a 500 error
            return ResponseEntity.status(500).body(new RoomResponse("File upload error: " + e.getMessage()));
        } catch (Exception e) {
            // Handle other exception and return a 500 error
            return ResponseEntity.status(500).body(new RoomResponse("Unexpected error: " + e.getMessage()));
        }
    }

    @GetMapping("/room/types")
    @Cacheable(value = "rooms", key = "'allRooms'")
    public List<String> getRoomTypes(){
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        try {
            List<Room> rooms = roomService.getAllRooms();
            List<RoomResponse> roomResponses = new ArrayList<>();
            for (Room room : rooms) {
                byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
                if (photoBytes != null && photoBytes.length > 0) {
                    String base64Photo = Base64.encodeBase64String(photoBytes);
                    RoomResponse roomResponse = getRoomResponse(room);
                    roomResponse.setPhoto(base64Photo);
                    roomResponses.add(roomResponse);
                }
            }
            return ResponseEntity.ok(roomResponses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/room/{roomId}")
    @Cacheable(value = "rooms", key = "#roomId")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
        try {
            Optional<Room> theRoom = roomService.getRoomById(roomId);
            return theRoom.map(room -> {
                RoomResponse roomResponse = getRoomResponse(room);
                return ResponseEntity.ok(roomResponse);
            }).orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }




    @DeleteMapping("/delete/room/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CacheEvict(value = "rooms", key = "#roomId")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId){
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws IOException, SQLException {
        byte[] photoBytes = photo != null && !photo.isEmpty() ? photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;
        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        theRoom.setPhoto(photoBlob);
        RoomResponse roomResponse = getRoomResponse(theRoom);
        return ResponseEntity.ok(roomResponse);
    }

    @GetMapping("/available-rooms")
    @Cacheable(value = "availableRooms", key = "#checkInDate + '-' + #checkOutDate + '-' + #roomType")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam("roomType") String roomType) throws SQLException {

        List<Room> availableRooms = roomService.getAvailableRooms(checkInDate, checkOutDate, roomType);
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : availableRooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if (photoBytes != null && photoBytes.length > 0) {
                String photoBase64 = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(photoBase64);
                roomResponses.add(roomResponse);
            }
        }

        if (roomResponses.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(roomResponses);
        }
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = bookingService.getAllBookingsByRoomId(room.getId());
        if (bookings == null) {
            bookings = new ArrayList<>(); // Ensure empty list if no bookings
        }
        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(booking -> new BookingResponse(booking.getBookingId(), booking.getCheckInDate(),
                        booking.getCheckOutDate(), booking.getBookingNumber()))
                .toList();

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if (photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalException("Failed to retrieve photo for room " + room.getId());
            }
        }

        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes,
                bookingInfo);
    }


}



