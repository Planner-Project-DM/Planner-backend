package net.dysky.planner.weather;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.geocoding.CoordinatesDTO;
import net.dysky.planner.geocoding.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class WeatherControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        when(jwtService.extractEmail(any(HttpServletRequest.class))).thenReturn("user@example.com");
    }

    @Test
    void getWeatherForCity_shouldReturnArchiveDataAndAverages_whenStartDateIsMoreThan14DaysInFuture() throws Exception {
        String city = "Warszawa";
        CoordinatesDTO coordinates = new CoordinatesDTO(52.2298, 21.0118);

        LocalDate startDate = LocalDate.now().plusDays(15);
        LocalDate endDate = LocalDate.now().plusDays(20);

        WeatherSearchDTO searchDTO = new WeatherSearchDTO(startDate, endDate);

        LocalDate startArchiveDate = startDate.minusYears(5);
        LocalDate endArchiveDate = endDate.minusYears(2);

        ArchiveWeatherDTO mockRawArchiveResponse = mock(ArchiveWeatherDTO.class);
        ArchiveWeatherDTO mockAveragedResponse = mock(ArchiveWeatherDTO.class);

        when(geocodingService.getCoordinates(city)).thenReturn(coordinates);
        when(weatherService.getArchiveData(coordinates.latitude(), coordinates.longitude(), startArchiveDate, endArchiveDate))
                .thenReturn(mockRawArchiveResponse);
        when(weatherService.calculateAverageParams(mockRawArchiveResponse, startDate, endDate))
                .thenReturn(mockAveragedResponse);

        mockMvc.perform(get("/api/weather/{city}", city)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Weather data retrieved successfully"))
                .andExpect(jsonPath("$.url").value("api/weather/" + city))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getWeatherForCity_shouldReturnForecastData_whenStartDateIsWithin14Days() throws Exception {
        String city = "Warszawa";
        CoordinatesDTO coordinates = new CoordinatesDTO(52.2298, 21.0118);

        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(10);

        WeatherSearchDTO searchDTO = new WeatherSearchDTO(startDate, endDate);
        WeatherDTO mockForecastResponse = mock(WeatherDTO.class);

        when(geocodingService.getCoordinates(city)).thenReturn(coordinates);
        when(weatherService.getWeatherData(coordinates.latitude(), coordinates.longitude(), startDate, endDate))
                .thenReturn(mockForecastResponse);

        mockMvc.perform(get("/api/weather/{city}", city)
                        .with(user("user@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Weather data retrieved successfully"))
                .andExpect(jsonPath("$.url").value("api/weather/" + city))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.data").exists());
    }
}