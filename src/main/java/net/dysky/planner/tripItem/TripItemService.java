package net.dysky.planner.tripItem;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.address.Address;
import net.dysky.planner.exception.TripNotFoundException;
import net.dysky.planner.hotel.ElementDTO;
import net.dysky.planner.hotel.Hotel;
import net.dysky.planner.hotel.Location;
import net.dysky.planner.hotel.OverpassApiDTO;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TripItemService {

    private final RestClient restClient;

    private final TripItemRepository tripItemRepository;

    public TripItem findById(UUID id) {
        return tripItemRepository.findById(id).orElseThrow(
                () -> new TripNotFoundException("Trip item not found with id: " + id));
    }

    public List<TripItem> findAllByCity(String city) {
        return tripItemRepository.findAllByAddress_City(city);
    }

    public List<TripItem> findAllByCityAndCategory(String city, TripItemCategory category) {
        return tripItemRepository.findAllByAddress_CityAndCategory(city, category);
    }

    @Retryable(
            retryFor = {RuntimeException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000)
    )
    public OverpassApiDTO getDataFromOverpassApi(String city) {
        String query = String.format("""
                [out:json][timeout:25];
                    area["name"="%s"]["admin_level"="8"]->.searchArea;
                    (
                      node["tourism"~"hotel|hostel|guest_house|apartment|museum|viewpoint|attraction"](area.searchArea);
                      node["historic"~"castle|monument|ruins"](area.searchArea);
                      way["tourism"~"hotel|hostel|guest_house|apartment|museum|viewpoint|attraction"](area.searchArea);
                      way["historic"~"castle|monument|ruins"](area.searchArea);
                    );
                    out center body;
            """, city);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("data", query);

        return restClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new RuntimeException("Error from Overpass API: " + response.getStatusCode());
                })
                .body(OverpassApiDTO.class);
    }

    @Transactional
    public List<TripItem> saveDataFromOverpass(OverpassApiDTO overpassApiDTO, String city) {

        for(ElementDTO elementDTO : overpassApiDTO.elements()) {
            TripItem tripItem = new TripItem();

            tripItem.setName(elementDTO.tags().name());

            Address address = Address.builder()
                    .country(elementDTO.tags().country())
                    .city(elementDTO.tags().city())
                    .street(elementDTO.tags().street())
                    .postalCode(elementDTO.tags().postalCode())
                    .houseNumber(elementDTO.tags().houseNumber())
                    .build();

            tripItem.setAddress(address);

            tripItem.setPhoneNumber(elementDTO.tags().phone());
            tripItem.setEmail(elementDTO.tags().email());
            tripItem.setWebsite(elementDTO.tags().website());
            tripItem.setDescription(elementDTO.tags().description());

            Location location = Location.builder()
                    .latitude(elementDTO.lat())
                    .longitude(elementDTO.lon())
                    .build();

            tripItem.setLocation(location);

            tripItem.setStars(elementDTO.tags().stars());

            String resolvedCategory = (elementDTO.tags().tourism() != null)
                    ? elementDTO.tags().tourism()
                    : elementDTO.tags().historic();

            tripItem.setTourism(resolvedCategory);

            TripItemCategory category = switch (resolvedCategory) {
                case "hotel", "hostel", "guest_house", "apartment" -> TripItemCategory.HOTEL;
                case "museum", "viewpoint", "attraction", "castle", "monument", "ruins" -> TripItemCategory.ATTRACTION;
                default -> TripItemCategory.OTHER;
            };

            tripItem.setCategory(category);

            tripItemRepository.save(tripItem);
        }

        return findAllByCity(city);
    }

}
