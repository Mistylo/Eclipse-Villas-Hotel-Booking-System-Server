package com.daliycode.HotelBookingSystem.cache;

import com.daliycode.HotelBookingSystem.model.Room;
import com.daliycode.HotelBookingSystem.repository.RoomRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisCacheService {

    private final RedisTemplate<String, Room> redisTemplate;
    private final RoomRepository roomRepository;

    public RedisCacheService(RedisTemplate<String, Room> redisTemplate, RoomRepository roomRepository) {
        this.redisTemplate = redisTemplate;
        this.roomRepository = roomRepository;
    }

    @Cacheable(value = "rooms", key = "#roomId")
    public Room getRoomFromCache(Long roomId) {
        return findRoomById(roomId);
    }


    @CacheEvict(value = "rooms", key = "#roomId")
    public void evictRoomFromCache(Long roomId) {
    }

    @CachePut(value = "rooms", key = "#roomId")
    public Room updateRoomCache(Long roomId, Room updatedRoom) {
        return updateRoomInDatabase(roomId, updatedRoom);
    }

    private Room findRoomById(Long roomId) {
        Room room = redisTemplate.opsForValue().get("room:" + roomId);

        if (room == null) {
            room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

            redisTemplate.opsForValue().set("room:" + roomId, room);
        }

        return room;
    }

    private Room updateRoomInDatabase(Long roomId, Room updatedRoom) {
        return updatedRoom;
    }
}
