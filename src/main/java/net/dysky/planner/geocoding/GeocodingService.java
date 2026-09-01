package net.dysky.planner.geocoding;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeocodingService {

    private final RestClient geocodingRestClient;

    public GeocodingService(@Qualifier("geocodingRestClient") RestClient geocodingRestClient) {
        this.geocodingRestClient = geocodingRestClient;
    }

    public CoordinatesDTO getCoordinates(String cityName) {
        GeocodingResponse response = geocodingRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("name", cityName)
                        .queryParam("count", 1)
                        .queryParam("language", "pl")
                        .queryParam("format", "json")
                        .build()
                )
                .retrieve()
                .body(GeocodingResponse.class);

        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new IllegalArgumentException("Cannot find coordinates for city: " + cityName);
        }

        var firstResult = response.results().get(0);

        return new CoordinatesDTO(firstResult.latitude(), firstResult.longitude());
    }
}
