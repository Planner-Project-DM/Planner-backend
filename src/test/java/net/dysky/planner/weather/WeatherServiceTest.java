package net.dysky.planner.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestClient archiveRestClient;

    @Mock
    private RestClient weatherRestClient;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(archiveRestClient, weatherRestClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getArchiveData_ShouldReturnArchiveWeatherDTO_OnSuccess() throws Exception {
        double lat = 52.2298;
        double lon = 21.0118;
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(5);
        ArchiveWeatherDTO mockResponse = mock(ArchiveWeatherDTO.class);

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        org.springframework.web.util.UriBuilder uriBuilder = mock(org.springframework.web.util.UriBuilder.class);
        when(uriBuilder.queryParam(anyString(), any(Object[].class))).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(new java.net.URI("http://mock-uri"));

        when(archiveRestClient.get()).thenReturn(uriSpec);

        when(uriSpec.uri(any(Function.class))).thenAnswer(invocation -> {
            Function<org.springframework.web.util.UriBuilder, java.net.URI> lambda = invocation.getArgument(0);
            lambda.apply(uriBuilder);
            return headersSpec;
        });

        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ArchiveWeatherDTO.class)).thenReturn(mockResponse);

        ArchiveWeatherDTO result = weatherService.getArchiveData(lat, lon, start, end);

        assertNotNull(result);
        assertEquals(mockResponse, result);
    }


    @Test
    void getWeatherData_ShouldThrowException_WhenStartDateInPast() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                weatherService.getWeatherData(52.2298, 21.0118, pastDate, endDate)
        );
        assertEquals("Start date cannot be in the past.", exception.getMessage());
    }

    @Test
    void getWeatherData_ShouldThrowException_WhenStartDateMoreThan14DaysInFuture() {
        LocalDate farFutureDate = LocalDate.now().plusDays(15);
        LocalDate endDate = LocalDate.now().plusDays(20);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                weatherService.getWeatherData(52.2298, 21.0118, farFutureDate, endDate)
        );
        assertEquals("Start date must be within 14 days from now.", exception.getMessage());
    }

    @Test
    void getWeatherData_ShouldThrowException_WhenStartDateAfterEndDate() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(3);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                weatherService.getWeatherData(52.2298, 21.0118, startDate, endDate)
        );
        assertEquals("Start date must be before end date.", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getWeatherData_ShouldReturnWeatherDTO_OnSuccess() throws Exception {
        double lat = 52.2298;
        double lon = 21.0118;
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(5);
        WeatherDTO mockResponse = mock(WeatherDTO.class);

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        org.springframework.web.util.UriBuilder uriBuilder = mock(org.springframework.web.util.UriBuilder.class);
        when(uriBuilder.queryParam(anyString(), any(Object[].class))).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(new java.net.URI("http://mock-uri"));

        when(weatherRestClient.get()).thenReturn(uriSpec);

        when(uriSpec.uri(any(Function.class))).thenAnswer(invocation -> {
            Function<org.springframework.web.util.UriBuilder, java.net.URI> lambda = invocation.getArgument(0);
            lambda.apply(uriBuilder);
            return headersSpec;
        });

        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherDTO.class)).thenReturn(mockResponse);

        WeatherDTO result = weatherService.getWeatherData(lat, lon, start, end);

        assertNotNull(result);
        assertEquals(mockResponse, result);
    }

    @Test
    void calculateAverageParams_ShouldCalculateAveragesCorrectly() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 2);

        List<String> rawTimes = List.of("2021-10-01", "2021-10-02", "2022-10-01", "2022-10-02");
        List<Double> maxTemps = List.of(20.0, 15.0, 10.0, 5.0);
        List<Double> minTemps = List.of(10.0, 8.0, 6.0, 4.0);
        List<Double> rainSums = List.of(2.0, 4.0, 0.0, 0.0);

        Daily daily = new Daily(rawTimes, maxTemps, minTemps, rainSums);
        DailyUnits dailyUnits = mock(DailyUnits.class);

        ArchiveWeatherDTO inputDTO = new ArchiveWeatherDTO(
                "52.2298", "21.0118", "1.2", "3600", "UTC", "UTC", "115",
                dailyUnits, daily
        );

        ArchiveWeatherDTO result = weatherService.calculateAverageParams(inputDTO, startDate, endDate);

        assertNotNull(result);
        Daily resultDaily = result.daily();
        assertNotNull(resultDaily);

        assertEquals(List.of("2026-10-01", "2026-10-02"), resultDaily.time());
        assertEquals(List.of(15.0, 10.0), resultDaily.temperature_2m_max());
        assertEquals(List.of(8.0, 6.0), resultDaily.temperature_2m_min());
        assertEquals(List.of(1.0, 2.0), resultDaily.rain_sum());
    }

    @Test
    void calculateAverageParams_ShouldReturnEmptyDaily_WhenNoMatchingHistoricalData() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 1);

        List<String> rawTimes = List.of("2021-10-05");
        List<Double> maxTemps = List.of(20.0);
        List<Double> minTemps = List.of(10.0);
        List<Double> rainSums = List.of(2.0);

        Daily daily = new Daily(rawTimes, maxTemps, minTemps, rainSums);
        DailyUnits dailyUnits = mock(DailyUnits.class);

        ArchiveWeatherDTO inputDTO = new ArchiveWeatherDTO(
                "52.2298", "21.0118", "1.2", "3600", "UTC", "UTC", "115",
                dailyUnits, daily
        );

        ArchiveWeatherDTO result = weatherService.calculateAverageParams(inputDTO, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.daily().time().isEmpty());
        assertTrue(result.daily().temperature_2m_max().isEmpty());
        assertTrue(result.daily().temperature_2m_min().isEmpty());
        assertTrue(result.daily().rain_sum().isEmpty());
    }
}