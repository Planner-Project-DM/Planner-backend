package net.dysky.planner.hotel;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.cityVisited.CityVisitedService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels")
class HotelController {

    private final HotelService hotelService;

    private final CityVisitedService cityVisitedService;

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

    // TODO zlaczyc to w jeden enpoint jestli dane miasto jest w bazie to zwracamy z bazy jest nie to z overpass
    @GetMapping("city/{city}")
    public ResponseEntity<ResponseDTO> getHotelByCity(@PathVariable String city) {
        List<Hotel> hotels;

        if (cityVisitedService.isCityVisited(city)) {
             hotels = hotelService.getHotelsByCity(city);

        } else {
            OverpassApiDTO overpassApiDTO = hotelService.getHotelsFromOverpassApi(city);
            hotels = hotelService.saveHotelFromOverpass(overpassApiDTO, city);
            cityVisitedService.markCityASVisited(city);
        }

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
