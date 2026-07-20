package net.dysky.planner.hotel;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.HotelNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    public Page<Hotel> getAllHotels(Pageable pageable) {
        return hotelRepository.findAll(pageable);
    }

    public Hotel getHotelById(UUID id) {
        return hotelRepository.findById(id).orElseThrow(
                () -> new HotelNotFoundException("Hotel not found"));
    }

    public List<Hotel> getHotelsByCity(String city) {
        return hotelRepository.findByAddress_City(city);
    }

    public Hotel getHotelByName(String name) {
        return hotelRepository.findByName(name);
    }

}
