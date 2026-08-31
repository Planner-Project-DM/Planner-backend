package net.dysky.planner.weather;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.geocoding.CoordinatesDTO;
import net.dysky.planner.geocoding.GeocodingService;
import net.dysky.planner.response.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
class WeatherController {

    private final WeatherService weatherService;

    private final GeocodingService geocodingService;

    @GetMapping("/{city}")
    public ResponseEntity<ResponseDTO> getWeatherForCity(@PathVariable String city, @RequestBody WeatherSearchDTO weatherSearchDTO) {

        CoordinatesDTO coordinates = geocodingService.getCoordinates(city);

        if(LocalDate.now().plusDays(14).isBefore(weatherSearchDTO.startDate())) {
            LocalDate startArchiveDate = weatherSearchDTO.startDate().minusYears(7);
            LocalDate endArchiveDate = weatherSearchDTO.endDate().minusYears(1);

            ArchiveWeatherDTO response = weatherService.getArchiveData(coordinates.latitude(), coordinates.longitude(), startArchiveDate, endArchiveDate);

            return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Weather data retrieved successfully", "api/weather/" + city, response));
        } else {
            WeatherDTO response = weatherService.getWeatherData(coordinates.latitude(), coordinates.longitude(), weatherSearchDTO.startDate(), weatherSearchDTO.endDate());

            return ResponseEntity.ok(new ResponseDTO(LocalDateTime.now(), 200, "Weather data retrieved successfully", "api/weather/" + city, response));
        }

    }

}
