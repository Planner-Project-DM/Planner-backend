package net.dysky.planner.hotel;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels")
class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<ResponseDTO> getAllHotels(@PageableDefault(size = 10) Pageable pageable) {
        Page<Hotel> hotels = hotelService.getAllHotels(pageable);

        ResponseDTO responseDTO = new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Hotels retrieved successfully",
                "/api/hotels",
                hotels
        );

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO> getHotelById(@PathVariable UUID id) {
        Hotel hotel = hotelService.getHotelById(id);

        ResponseDTO responseDTO = new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Hotels retrieved successfully",
                "/api/hotels/" + id,
                hotel
        );

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{city}")
    public ResponseEntity<ResponseDTO> getHotelByCity(@PathVariable String city) {
        List<Hotel> hotels = hotelService.getHotelsByCity(city);

        ResponseDTO responseDTO = new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Hotels retrieved successfully",
                "/api/hotels/" + city,
                hotels
        );

        return ResponseEntity.ok(responseDTO);
    }

}
