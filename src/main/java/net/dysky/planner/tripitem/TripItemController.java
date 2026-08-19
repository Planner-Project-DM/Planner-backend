package net.dysky.planner.tripitem;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.cityVisited.CityVisitedService;
import net.dysky.planner.hotel.OverpassApiDTO;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trip-items")
class TripItemController {

    private final TripItemService tripItemService;

    private final CityVisitedService cityVisitedService;

    @GetMapping("/city/{city}")
    public ResponseEntity<ResponseDTO> getTripItemsByCity(@PathVariable String city) {
        List<TripItem> tripItems;

        if(cityVisitedService.isCityVisited(city)) {
            tripItems = tripItemService.findAllByCity(city);
        } else {
            OverpassApiDTO overpassApiDTO = tripItemService.getDataFromOverpassApi(city);
            tripItems = tripItemService.saveDataFromOverpass(overpassApiDTO, city);
            cityVisitedService.markCityASVisited(city);
        }

        ResponseDTO responseDTO = new ResponseDTO(
                LocalDateTime.now(),
                200,
                "Trip items retrieved successfully",
                "/api/trip-items/city/" + city,
                tripItems
        );

        return ResponseEntity.ok(responseDTO);
    }

}
