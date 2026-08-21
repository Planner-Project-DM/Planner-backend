package net.dysky.planner.tripitem;

import net.dysky.planner.address.Address;
import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.hotel.ElementDTO;
import net.dysky.planner.hotel.Location;
import net.dysky.planner.hotel.OverpassApiDTO;
import net.dysky.planner.hotel.TagsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripItemServiceTest {

    @Mock
    private TripItemRepository tripItemRepository;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private TripItemService tripItemService;

    @Test
    void findById_shouldReturnTripItemWhenExists() {
        UUID id = UUID.randomUUID();
        TripItem tripItem = createTestTripItem("Eiffel Tower", "Paris");
        tripItem.setId(id);

        when(tripItemRepository.findById(id)).thenReturn(Optional.of(tripItem));

        TripItem result = tripItemService.findById(id);

        assertThat(result)
                .isNotNull()
                .extracting(TripItem::getName)
                .isEqualTo("Eiffel Tower");
        verify(tripItemRepository).findById(id);
    }

    @Test
    void findById_shouldThrowTripNotFoundExceptionWhenNotExists() {
        UUID id = UUID.randomUUID();
        when(tripItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripItemService.findById(id))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("Trip item not found with id: " + id);
    }

    @Test
    void findByName_shouldReturnTripItemWhenExists() {
        TripItem tripItem = createTestTripItem("Eiffel Tower", "Paris");

        when(tripItemRepository.findByName("Eiffel Tower")).thenReturn(Optional.of(tripItem));

        TripItem result = tripItemService.findByName("Eiffel Tower");

        assertThat(result)
                .isNotNull()
                .extracting(TripItem::getName)
                .isEqualTo("Eiffel Tower");
        verify(tripItemRepository).findByName("Eiffel Tower");
    }

    @Test
    void findByName_shouldThrowTripNotFoundExceptionWhenNotExists() {
        when(tripItemRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripItemService.findByName("NonExistent"))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("Trip item not found with name: NonExistent");
    }

    @Test
    void findAllByCity_shouldReturnTripItemsByCity() {
        List<TripItem> tripItems = List.of(
                createTestTripItem("Eiffel Tower", "Paris"),
                createTestTripItem("Louvre Museum", "Paris")
        );

        when(tripItemRepository.findAllByAddress_City("Paris")).thenReturn(tripItems);

        List<TripItem> result = tripItemService.findAllByCity("Paris");

        assertThat(result)
                .hasSize(2)
                .extracting(TripItem::getName)
                .containsExactlyInAnyOrder("Eiffel Tower", "Louvre Museum");
        verify(tripItemRepository).findAllByAddress_City("Paris");
    }

    @Test
    void findAllByCity_shouldReturnEmptyListWhenNoCityMatches() {
        when(tripItemRepository.findAllByAddress_City("NonExistent")).thenReturn(List.of());

        List<TripItem> result = tripItemService.findAllByCity("NonExistent");

        assertThat(result).isEmpty();
        verify(tripItemRepository).findAllByAddress_City("NonExistent");
    }

    @Test
    void findAllByCityAndCategory_shouldReturnFilteredTripItems() {
        List<TripItem> tripItems = List.of(
                createTestTripItem("Eiffel Tower", "Paris")
        );

        when(tripItemRepository.findAllByAddress_CityAndCategory("Paris", TripItemCategory.ATTRACTION))
                .thenReturn(tripItems);

        List<TripItem> result = tripItemService.findAllByCityAndCategory("Paris", TripItemCategory.ATTRACTION);

        assertThat(result)
                .hasSize(1)
                .extracting(TripItem::getName)
                .containsExactly("Eiffel Tower");
        verify(tripItemRepository).findAllByAddress_CityAndCategory("Paris", TripItemCategory.ATTRACTION);
    }

    @Test
    void findAllByCityAndCategory_shouldReturnEmptyListWhenNoCategoryMatches() {
        when(tripItemRepository.findAllByAddress_CityAndCategory("Paris", TripItemCategory.HOTEL))
                .thenReturn(List.of());

        List<TripItem> result = tripItemService.findAllByCityAndCategory("Paris", TripItemCategory.HOTEL);

        assertThat(result).isEmpty();
    }

    @Test
    void saveDataFromOverpass_shouldMapHotelCategory() {
        TagsDTO hotelTags = createTestTagsDTO("hotel", null);
        ElementDTO hotelElement = new ElementDTO(
                "node",
                1L,
                new BigDecimal("48.86605"),
                new BigDecimal("2.33353"),
                null,
                hotelTags
        );
        OverpassApiDTO overpassApiDTO = new OverpassApiDTO(List.of(hotelElement));

        when(tripItemRepository.save(any(TripItem.class))).thenAnswer(invocation -> {
            TripItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });
        when(tripItemRepository.findAllByAddress_City("Paris")).thenReturn(List.of());

        List<TripItem> result = tripItemService.saveDataFromOverpass(overpassApiDTO, "Paris");

        assertThat(result).isEmpty();
        verify(tripItemRepository).save(any(TripItem.class));
    }

    @Test
    void saveDataFromOverpass_shouldMapAttractionCategory() {
        TagsDTO attractionTags = createTestTagsDTO(null, "museum");
        ElementDTO attractionElement = new ElementDTO(
                "node",
                2L,
                new BigDecimal("48.86605"),
                new BigDecimal("2.33353"),
                null,
                attractionTags
        );
        OverpassApiDTO overpassApiDTO = new OverpassApiDTO(List.of(attractionElement));

        when(tripItemRepository.save(any(TripItem.class))).thenAnswer(invocation -> {
            TripItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });
        when(tripItemRepository.findAllByAddress_City("Paris")).thenReturn(List.of());

        List<TripItem> result = tripItemService.saveDataFromOverpass(overpassApiDTO, "Paris");

        assertThat(result).isEmpty();
        verify(tripItemRepository).save(any(TripItem.class));
    }

    @Test
    void saveDataFromOverpass_shouldSkipItemsWithoutName() {
        TagsDTO tagsWithoutName = new TagsDTO(
                null,
                "Test",
                "1",
                "12345",
                "Paris",
                "France",
                "test@example.com",
                "+33123456789",
                "5",
                "http://test.com",
                "Test Description",
                "hotel",
                null
        );
        ElementDTO element = new ElementDTO(
                "node",
                1L,
                new BigDecimal("48.86605"),
                new BigDecimal("2.33353"),
                null,
                tagsWithoutName
        );
        OverpassApiDTO overpassApiDTO = new OverpassApiDTO(List.of(element));

        when(tripItemRepository.findAllByAddress_City("Paris")).thenReturn(List.of());

        List<TripItem> result = tripItemService.saveDataFromOverpass(overpassApiDTO, "Paris");

        assertThat(result).isEmpty();
        verify(tripItemRepository, times(0)).save(any(TripItem.class));
    }

    @Test
    void saveDataFromOverpass_shouldUseProvidedCityWhenNotInElement() {
        TagsDTO tags = new TagsDTO(
                "Hotel Name",      // name
                "Street",          // street
                "1",               // houseNumber
                "12345",           // postalCode
                null,              // city
                "France",          // country
                "test@example.com",// email
                "+33123456789",    // phone
                "5",               // stars
                "http://test.com", // website
                "Test Description",// description
                "hotel",           // tourism
                null               // historic
        );
        ElementDTO element = new ElementDTO(
                "node",
                1L,
                new BigDecimal("48.86605"),
                new BigDecimal("2.33353"),
                null,
                tags
        );
        OverpassApiDTO overpassApiDTO = new OverpassApiDTO(List.of(element));

        when(tripItemRepository.save(any(TripItem.class))).thenAnswer(invocation -> {
            TripItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });
        when(tripItemRepository.findAllByAddress_City("Paris")).thenReturn(List.of());

        List<TripItem> result = tripItemService.saveDataFromOverpass(overpassApiDTO, "Paris");

        assertThat(result).isEmpty();
        verify(tripItemRepository).save(any(TripItem.class));
    }

    @Test
    void saveDataFromOverpass_shouldMapOtherCategoryForUnknownTourism() {
        TagsDTO tags = createTestTagsDTO("unknown_type", null);
        ElementDTO element = new ElementDTO(
                "node",
                1L,
                new BigDecimal("48.86605"),
                new BigDecimal("2.33353"),
                null,
                tags
        );
        OverpassApiDTO overpassApiDTO = new OverpassApiDTO(List.of(element));

        when(tripItemRepository.save(any(TripItem.class))).thenAnswer(invocation -> {
            TripItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });
        when(tripItemRepository.findAllByAddress_City("Paris")).thenReturn(List.of());

        List<TripItem> result = tripItemService.saveDataFromOverpass(overpassApiDTO, "Paris");

        assertThat(result).isEmpty();
        verify(tripItemRepository).save(any(TripItem.class));
    }

    @Test
    void saveDataFromOverpass_shouldMapAllAddressFields() {
        TagsDTO tags = new TagsDTO(
                "Hotel Name",
                "Rue de la Paix",
                "42",
                "75008",
                "Paris",
                "France",
                "test@example.com",
                "+33123456789",
                "5",
                "http://test.com",
                "Test Description",
                "hotel",
                null
        );
        ElementDTO element = new ElementDTO(
                "node",
                1L,
                new BigDecimal("48.86605"),
                new BigDecimal("2.33353"),
                null,
                tags
        );
        OverpassApiDTO overpassApiDTO = new OverpassApiDTO(List.of(element));

        when(tripItemRepository.save(any(TripItem.class))).thenAnswer(invocation -> {
            TripItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });
        when(tripItemRepository.findAllByAddress_City("Paris")).thenReturn(List.of());

        List<TripItem> result = tripItemService.saveDataFromOverpass(overpassApiDTO, "Paris");

        assertThat(result).isEmpty();
        verify(tripItemRepository).save(any(TripItem.class));
    }

    private TripItem createTestTripItem(String name, String city) {
        TripItem tripItem = new TripItem();
        tripItem.setId(UUID.randomUUID());
        tripItem.setName(name);
        tripItem.setCategory(TripItemCategory.ATTRACTION);

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

    private OverpassApiDTO createTestOverpassApiDTO() {
        TagsDTO tags = createTestTagsDTO("hotel", null);
        ElementDTO element = new ElementDTO(
                "node",
                1L,
                new BigDecimal("48.86605"),
                new BigDecimal("2.33353"),
                null,
                tags
        );
        return new OverpassApiDTO(List.of(element));
    }

    private TagsDTO createTestTagsDTO(String tourism, String historic) {
        return new TagsDTO(
                "Test Name",
                "TestStreet",
                "1",
                "12345",
                "TestCity",
                "TestCountry",
                "test@example.com",
                "+33123456789",
                "5",
                "http://test.com",
                "Test Description",
                tourism,
                historic
        );
    }
}