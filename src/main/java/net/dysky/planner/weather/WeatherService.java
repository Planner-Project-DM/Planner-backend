package net.dysky.planner.weather;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Service
public class WeatherService {

    private final RestClient archiveRestClient;
    private final RestClient weatherRestClient;

    public WeatherService(
            @Qualifier("archiveRestClient") RestClient archiveRestClient,
            @Qualifier("weatherRestClient") RestClient weatherRestClient) {
        this.archiveRestClient = archiveRestClient;
        this.weatherRestClient = weatherRestClient;
    }

    public ArchiveWeatherDTO getArchiveData(double latitude, double longitude, LocalDate startDate, LocalDate endDate) {

        return archiveRestClient.get().uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .queryParam("daily", "temperature_2m_max,temperature_2m_min,rain_sum")
                        .build()
                )
                .retrieve()
                .body(ArchiveWeatherDTO.class);
    }

    public WeatherDTO getWeatherData(double latitude, double longitude, LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }

        if (startDate.isAfter(LocalDate.now().plusDays(14))) {
            throw new IllegalArgumentException("Start date must be within 14 days from now.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }

        return weatherRestClient.get().uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .queryParam("hourly", "temperature_2m,apparent_temperature,precipitation,precipitation_probability,weather_code,cloud_cover,is_day,wind_speed_10m")
                        .build()
                )
                .retrieve()
                .body(WeatherDTO.class);
    }

}
