package net.dysky.planner.cityVisited;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CityVisitedService {

    private final CityVisitedRepository cityVisitedRepository;

    public boolean isCityVisited(String city) {
        return cityVisitedRepository.existsByCity(city);
    }

    public void markCityASVisited(String city) {
        if(!isCityVisited(city)) {
            CityVisited cityVisited = new CityVisited();
            cityVisited.setCity(city);
            cityVisitedRepository.save(cityVisited);
        }
    }

}
