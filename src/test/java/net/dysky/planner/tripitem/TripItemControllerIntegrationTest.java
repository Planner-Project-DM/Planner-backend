package net.dysky.planner.tripitem;

import jakarta.transaction.Transactional;
import net.dysky.planner.AbstractIntegrationTest;
import net.dysky.planner.address.Address;
import net.dysky.planner.auth.JwtService;
import net.dysky.planner.cityVisited.CityVisitedService;
import net.dysky.planner.hotel.Location;
import net.dysky.planner.hotel.OverpassApiDTO;
import net.dysky.planner.tripitem.TripItemService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class TripItemControllerIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private TripItemService tripItemService;

    @MockitoBean
    private CityVisitedService cityVisitedService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getTripItemsByCity_shouldReturnCachedItemsWhenCityIsVisited() throws Exception {
        when(cityVisitedService.isCityVisited("Paris")).thenReturn(true);

        List<TripItem> tripItems = List.of(
                createTestTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION),
                createTestTripItem("Louvre Museum", "Paris", TripItemCategory.ATTRACTION)
        );
        when(tripItemService.findAllByCity("Paris")).thenReturn(tripItems);

        mockMvc.perform(get("/api/trip-items/city/Paris")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Trip items retrieved successfully")))
                .andExpect(jsonPath("$.url", is("/api/trip-items/city/Paris")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[1].name").exists());
    }

    @Test
    void getTripItemsByCity_shouldFetchFromApiAndMarkCityAsVisitedWhenNotVisited() throws Exception {
        when(cityVisitedService.isCityVisited("Rome")).thenReturn(false);

        OverpassApiDTO overpassApiDTO = new OverpassApiDTO(List.of());
        when(tripItemService.getDataFromOverpassApi("Rome")).thenReturn(overpassApiDTO);

        List<TripItem> tripItems = List.of();
        when(tripItemService.saveDataFromOverpass(overpassApiDTO, "Rome")).thenReturn(tripItems);

        mockMvc.perform(get("/api/trip-items/city/Rome")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Trip items retrieved successfully")))
                .andExpect(jsonPath("$.url", is("/api/trip-items/city/Rome")))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getTripItemsByCity_shouldReturnCorrectResponseDTO() throws Exception {
        when(cityVisitedService.isCityVisited("Berlin")).thenReturn(true);

        List<TripItem> tripItems = List.of(
                createTestTripItem("Brandenburg Gate", "Berlin", TripItemCategory.ATTRACTION)
        );
        when(tripItemService.findAllByCity("Berlin")).thenReturn(tripItems);

        mockMvc.perform(get("/api/trip-items/city/Berlin")
                        .with(user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.message", is("Trip items retrieved successfully")))
                .andExpect(jsonPath("$.url", is("/api/trip-items/city/Berlin")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Brandenburg Gate")))
                .andExpect(jsonPath("$.data[0].category", is("ATTRACTION")));
    }

    @Test
    void getTripItemsByCity_shouldIncludeAllTripItemFields() throws Exception {
        when(cityVisitedService.isCityVisited("Paris")).thenReturn(true);

        TripItem tripItem = new TripItem();
        tripItem.setId(UUID.randomUUID());
        tripItem.setName("Luxury Hotel");
        tripItem.setCategory(TripItemCategory.HOTEL);
        tripItem.setPhoneNumber("+33123456789");
        tripItem.setEmail("hotel@paris.com");
        tripItem.setWebsite("http://hotel.com");
        tripItem.setDescription("A beautiful hotel");
        tripItem.setStars("5");
        tripItem.setPrice(250.0);
        tripItem.setTourism("hotel");

        Address address = Address.builder()
                .country("France")
                .city("Paris")
                .street("Rue de Test")
                .postalCode("75001")
                .houseNumber("10")
                .build();
        tripItem.setAddress(address);

        Location location = Location.builder()
                .latitude(new BigDecimal("48.86605"))
                .longitude(new BigDecimal("2.33353"))
                .build();
        tripItem.setLocation(location);

        when(tripItemService.findAllByCity("Paris")).thenReturn(List.of(tripItem));

        mockMvc.perform(get("/api/trip-items/city/Paris")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Luxury Hotel")))
                .andExpect(jsonPath("$.data[0].category", is("HOTEL")))
                .andExpect(jsonPath("$.data[0].phoneNumber", is("+33123456789")))
                .andExpect(jsonPath("$.data[0].email", is("hotel@paris.com")))
                .andExpect(jsonPath("$.data[0].website", is("http://hotel.com")))
                .andExpect(jsonPath("$.data[0].description", is("A beautiful hotel")))
                .andExpect(jsonPath("$.data[0].stars", is("5")))
                .andExpect(jsonPath("$.data[0].price", is(250.0)))
                .andExpect(jsonPath("$.data[0].tourism", is("hotel")))
                .andExpect(jsonPath("$.data[0].address.country", is("France")))
                .andExpect(jsonPath("$.data[0].address.city", is("Paris")))
                .andExpect(jsonPath("$.data[0].address.street", is("Rue de Test")))
                .andExpect(jsonPath("$.data[0].address.postalCode", is("75001")))
                .andExpect(jsonPath("$.data[0].address.houseNumber", is("10")))
                .andExpect(jsonPath("$.data[0].location.latitude", is(48.86605)))
                .andExpect(jsonPath("$.data[0].location.longitude", is(2.33353)));
    }

    @Test
    void getTripItemsByCity_shouldHandleEmptyResults() throws Exception {
        when(cityVisitedService.isCityVisited("EmptyCity")).thenReturn(true);
        when(tripItemService.findAllByCity("EmptyCity")).thenReturn(List.of());

        mockMvc.perform(get("/api/trip-items/city/EmptyCity")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(200)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void getTripItemsByCity_shouldHandleMultipleDifferentCategories() throws Exception {
        when(cityVisitedService.isCityVisited("MixedCity")).thenReturn(true);

        List<TripItem> tripItems = List.of(
                createTestTripItem("Hotel A", "MixedCity", TripItemCategory.HOTEL),
                createTestTripItem("Restaurant B", "MixedCity", TripItemCategory.FOOD),
                createTestTripItem("Museum C", "MixedCity", TripItemCategory.ATTRACTION),
                createTestTripItem("Other D", "MixedCity", TripItemCategory.OTHER)
        );
        when(tripItemService.findAllByCity("MixedCity")).thenReturn(tripItems);

        mockMvc.perform(get("/api/trip-items/city/MixedCity")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].category", is("HOTEL")))
                .andExpect(jsonPath("$.data[1].category", is("FOOD")))
                .andExpect(jsonPath("$.data[2].category", is("ATTRACTION")))
                .andExpect(jsonPath("$.data[3].category", is("OTHER")));
    }

    @Test
    void getTripItemsByCity_shouldHaveCorrectContentType() throws Exception {
        when(cityVisitedService.isCityVisited("Paris")).thenReturn(true);
        when(tripItemService.findAllByCity("Paris")).thenReturn(List.of());

        mockMvc.perform(get("/api/trip-items/city/Paris")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk());
    }

    private TripItem createTestTripItem(String name, String city, TripItemCategory category) {
        TripItem tripItem = new TripItem();
        tripItem.setId(UUID.randomUUID());
        tripItem.setName(name);
        tripItem.setCategory(category);

        Address address = Address.builder()
                .country("TestCountry")
                .city(city)
                .street("Test Street")
                .postalCode("12345")
                .houseNumber("1")
                .build();
        tripItem.setAddress(address);

        Location location = Location.builder()
                .latitude(new BigDecimal("0.0"))
                .longitude(new BigDecimal("0.0"))
                .build();
        tripItem.setLocation(location);

        return tripItem;
    }
}
