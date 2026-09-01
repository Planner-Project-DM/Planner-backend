package net.dysky.planner.weather;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

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

        String formattedLatitude = String.format(Locale.US, "%.6f", latitude);
        String formattedLongitude = String.format(Locale.US, "%.6f", longitude);

        return weatherRestClient.get().uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", formattedLatitude)
                        .queryParam("longitude", formattedLongitude)
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .queryParam("hourly", "temperature_2m,apparent_temperature,precipitation,precipitation_probability,weather_code,cloud_cover,is_day,wind_speed_10m")
                        .build()
                )
                .retrieve()
                .body(WeatherDTO.class);
    }

    public ArchiveWeatherDTO calculateAverageParams(ArchiveWeatherDTO archiveWeatherDTO, LocalDate startDate, LocalDate endDate) {
        Daily daily = archiveWeatherDTO.daily();
        List<String> rawTimes = daily.time();

        List<LocalDate> targetDates = startDate.datesUntil(endDate.plusDays(1)).toList();

        List<String> finalTimes = new ArrayList<>();
        List<Double> finalTempMax = new ArrayList<>();
        List<Double> finalTempMin = new ArrayList<>();
        List<Double> finalRainSum = new ArrayList<>();

        for (LocalDate targetDate : targetDates) {
            MonthDay targetMD = MonthDay.from(targetDate);

            List<Integer> matchingIndices = IntStream.range(0, rawTimes.size())
                    .filter(i -> MonthDay.from(LocalDate.parse(rawTimes.get(i))).equals(targetMD))
                    .boxed()
                    .toList();

            if (!matchingIndices.isEmpty()) {
                double avgMax = matchingIndices.stream()
                        .mapToDouble(daily.temperature_2m_max()::get)
                        .average()
                        .orElse(0.0);

                double avgMin = matchingIndices.stream()
                        .mapToDouble(daily.temperature_2m_min()::get)
                        .average()
                        .orElse(0.0);

                double avgRain = matchingIndices.stream()
                        .mapToDouble(daily.rain_sum()::get)
                        .average()
                        .orElse(0.0);

                avgMax = Math.round(avgMax * 10.0) / 10.0;
                avgMin = Math.round(avgMin * 10.0) / 10.0;
                avgRain = Math.round(avgRain * 10.0) / 10.0;

                finalTimes.add(targetDate.toString());
                finalTempMax.add(avgMax);
                finalTempMin.add(avgMin);
                finalRainSum.add(avgRain);
            }
        }

        Daily averagedDaily = new Daily(
                finalTimes,
                finalTempMax,
                finalTempMin,
                finalRainSum
        );

        return new ArchiveWeatherDTO(
                archiveWeatherDTO.latitude(),
                archiveWeatherDTO.longitude(),
                archiveWeatherDTO.generationtime_ms(),
                archiveWeatherDTO.utc_offset_seconds(),
                archiveWeatherDTO.timezone(),
                archiveWeatherDTO.timezone_abbreviation(),
                archiveWeatherDTO.elevation(),
                archiveWeatherDTO.daily_units(),
                averagedDaily
        );


    }

}
