package net.dysky.planner.hotel;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dysky.planner.address.Address;
import net.dysky.planner.exception.HotelNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

@Service
@RequiredArgsConstructor
public class HotelService {

    private final RestClient restClient;

    private final HotelRepository hotelRepository;

    public Page<Hotel> getAllHotels(Pageable pageable) {
        return hotelRepository.findAll(pageable);
    }

    public Hotel getHotelById(UUID id) {
        return hotelRepository.findById(id).orElseThrow(
                () -> new HotelNotFoundException("Hotel not found"));
    }

    public List<Hotel> getHotelsByCity(String city) {
        return hotelRepository.findByAddress_City(city);
    }

    public Hotel getHotelByName(String name) {
        return hotelRepository.findByName(name);
    }

    @Retryable(
            retryFor = {RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public OverpassApiDTO getHotelsFromOverpassApi(String city) {
        String query = String.format("""
                [out:json][timeout:25];
                (
                  node["tourism"="hotel"]["addr:city"="%s"];
                  way["tourism"="hotel"]["addr:city"="%s"];
                );
                out center body;
                """,city, city);

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
    public List<Hotel> saveHotelFromOverpass(OverpassApiDTO overpassApiDTO, String city) {

        for(ElementDTO elementDTO : overpassApiDTO.elements()) {
            Hotel hotel = new Hotel();

            hotel.setName(elementDTO.tags().name());
            Address address = Address.builder()
                    .country(elementDTO.tags().country())
                    .city(elementDTO.tags().city())
                    .street(elementDTO.tags().street())
                    .postalCode(elementDTO.tags().postalCode())
                    .houseNumber(elementDTO.tags().houseNumber())
                    .build();

            hotel.setAddress(address);


            hotel.setPhoneNumber(elementDTO.tags().phone());
            hotel.setEmail(elementDTO.tags().email());
            hotel.setWebsite(elementDTO.tags().website());
            hotel.setDescription(elementDTO.tags().description());

            Location location = Location.builder()
                    .latitude(elementDTO.lat())
                    .longitude(elementDTO.lon())
                    .build();

            hotel.setLocation(location);

            hotel.setStars(elementDTO.tags().stars());

            hotelRepository.save(hotel);
        }

        return getHotelsByCity(city);
    }
}
