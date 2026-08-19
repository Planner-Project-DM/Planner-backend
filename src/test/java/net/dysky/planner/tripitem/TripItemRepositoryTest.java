package net.dysky.planner.tripitem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import net.dysky.planner.address.Address;
import net.dysky.planner.hotel.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TripItemRepositoryTest {

    @Autowired
    private TripItemRepository tripItemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findAllByAddress_City_shouldReturnOnlyTripsItemsInGivenCity() {
        persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);
        persistTripItem("Louvre Museum", "Paris", TripItemCategory.ATTRACTION);
        persistTripItem("Colosseum", "Rome", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        List<TripItem> parisItems = tripItemRepository.findAllByAddress_City("Paris");

        assertThat(parisItems)
                .hasSize(2)
                .extracting(TripItem::getName)
                .containsExactlyInAnyOrder("Eiffel Tower", "Louvre Museum");
    }

    @Test
    void findAllByAddress_City_shouldReturnEmptyListWhenCityNotFound() {
        persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        List<TripItem> items = tripItemRepository.findAllByAddress_City("NonExistentCity");

        assertThat(items).isEmpty();
    }

    @Test
    void findAllByAddress_CityAndCategory_shouldReturnOnlyItemsWithMatchingCityAndCategory() {
        persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);
        persistTripItem("Le Cordon Bleu", "Paris", TripItemCategory.FOOD);
        persistTripItem("Hilton Paris", "Paris", TripItemCategory.HOTEL);

        entityManager.flush();
        entityManager.clear();

        List<TripItem> attractions = tripItemRepository.findAllByAddress_CityAndCategory("Paris", TripItemCategory.ATTRACTION);

        assertThat(attractions)
                .hasSize(1)
                .extracting(TripItem::getName)
                .containsExactly("Eiffel Tower");
    }

    @Test
    void findAllByAddress_CityAndCategory_shouldReturnEmptyListWhenNoCategoryMatches() {
        persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        List<TripItem> hotels = tripItemRepository.findAllByAddress_CityAndCategory("Paris", TripItemCategory.HOTEL);

        assertThat(hotels).isEmpty();
    }

    @Test
    void findByName_shouldReturnOptionalWithTripItemWhenExists() {
        persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        Optional<TripItem> item = tripItemRepository.findByName("Eiffel Tower");

        assertThat(item).isPresent()
                .get()
                .extracting(TripItem::getName)
                .isEqualTo("Eiffel Tower");
    }

    @Test
    void findByName_shouldReturnEmptyOptionalWhenNotExists() {
        persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        Optional<TripItem> item = tripItemRepository.findByName("NonExistent");

        assertThat(item).isEmpty();
    }

    @Test
    void findById_shouldReturnOptionalWithTripItemWhenExists() {
        TripItem saved = persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        Optional<TripItem> item = tripItemRepository.findById(saved.getId());

        assertThat(item).isPresent()
                .get()
                .extracting(TripItem::getName)
                .isEqualTo("Eiffel Tower");
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenNotExists() {
        entityManager.flush();
        entityManager.clear();

        Optional<TripItem> item = tripItemRepository.findById(UUID.randomUUID());

        assertThat(item).isEmpty();
    }

    @Test
    void save_shouldPersistTripItemWithAllFields() {
        TripItem tripItem = new TripItem();
        tripItem.setName("Hotel Paris");
        tripItem.setCategory(TripItemCategory.HOTEL);
        tripItem.setPhoneNumber("+33123456789");
        tripItem.setEmail("hotel@paris.com");
        tripItem.setWebsite("http://hotelpariscomplex.com");
        tripItem.setDescription("Luxury hotel in the heart of Paris");
        tripItem.setStars("5");
        tripItem.setPrice(250.0);
        tripItem.setTourism("hotel");

        Address address = Address.builder()
                .country("France")
                .city("Paris")
                .street("Rue de la Paix")
                .postalCode("75008")
                .houseNumber("42")
                .build();
        tripItem.setAddress(address);

        Location location = Location.builder()
                .latitude(new BigDecimal("48.86605"))
                .longitude(new BigDecimal("2.33353"))
                .build();
        tripItem.setLocation(location);

        TripItem saved = tripItemRepository.save(tripItem);

        entityManager.flush();
        entityManager.clear();

        Optional<TripItem> retrieved = tripItemRepository.findById(saved.getId());

        assertThat(retrieved).isPresent()
                .get()
                .satisfies(item -> {
                    assertThat(item.getName()).isEqualTo("Hotel Paris");
                    assertThat(item.getCategory()).isEqualTo(TripItemCategory.HOTEL);
                    assertThat(item.getPhoneNumber()).isEqualTo("+33123456789");
                    assertThat(item.getEmail()).isEqualTo("hotel@paris.com");
                    assertThat(item.getWebsite()).isEqualTo("http://hotelpariscomplex.com");
                    assertThat(item.getDescription()).isEqualTo("Luxury hotel in the heart of Paris");
                    assertThat(item.getStars()).isEqualTo("5");
                    assertThat(item.getPrice()).isEqualTo(250.0);
                    assertThat(item.getTourism()).isEqualTo("hotel");
                    assertThat(item.getAddress().getCountry()).isEqualTo("France");
                    assertThat(item.getAddress().getCity()).isEqualTo("Paris");
                    assertThat(item.getAddress().getStreet()).isEqualTo("Rue de la Paix");
                    assertThat(item.getAddress().getPostalCode()).isEqualTo("75008");
                    assertThat(item.getAddress().getHouseNumber()).isEqualTo("42");
                    assertThat(item.getLocation().getLatitude()).isEqualByComparingTo(new BigDecimal("48.86605"));
                    assertThat(item.getLocation().getLongitude()).isEqualByComparingTo(new BigDecimal("2.33353"));
                });
    }

    @Test
    void delete_shouldRemoveTripItemFromDatabase() {
        TripItem saved = persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        tripItemRepository.delete(saved);

        entityManager.flush();
        entityManager.clear();

        Optional<TripItem> item = tripItemRepository.findById(saved.getId());

        assertThat(item).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllTripItems() {
        persistTripItem("Eiffel Tower", "Paris", TripItemCategory.ATTRACTION);
        persistTripItem("Louvre Museum", "Paris", TripItemCategory.ATTRACTION);
        persistTripItem("Colosseum", "Rome", TripItemCategory.ATTRACTION);

        entityManager.flush();
        entityManager.clear();

        List<TripItem> allItems = tripItemRepository.findAll();

        assertThat(allItems)
                .hasSize(3)
                .extracting(TripItem::getName)
                .containsExactlyInAnyOrder("Eiffel Tower", "Louvre Museum", "Colosseum");
    }

    private TripItem persistTripItem(String name, String city, TripItemCategory category) {
        TripItem tripItem = new TripItem();
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

        entityManager.persist(tripItem);
        return tripItem;
    }
}
