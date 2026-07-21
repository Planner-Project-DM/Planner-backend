package net.dysky.planner.hotel;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.exception.HotelNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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

    public OverpassApiDTO getHotelsFromOverpassApi(String city) {
        String query = String.format("""
                [out:json][timeout:25];
                (
                  node["tourism"="hotel"]["addr:city"="%s"];
                  way["tourism"="hotel"]["addr:city"="%s"];
                );
                out center body;
                """,city, city);

        return restClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("data=" + query)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new RuntimeException("Error from Overpass API: " + response.getStatusCode());
                })
                .body(OverpassApiDTO.class);
    }

}
