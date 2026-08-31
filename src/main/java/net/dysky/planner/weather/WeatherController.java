package net.dysky.planner.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
class WeatherController {

    private final WeatherService weatherService;



}
