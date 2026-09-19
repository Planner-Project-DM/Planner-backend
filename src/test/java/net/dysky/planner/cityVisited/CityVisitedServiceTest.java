package net.dysky.planner.cityVisited;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityVisitedServiceTest {

    @Mock
    private CityVisitedRepository cityVisitedRepository;

    @InjectMocks
    private CityVisitedService cityVisitedService;

    @Test
    void isCityVisited_shouldReturnTrue_whenCityExists() {
        when(cityVisitedRepository.existsByCity("Paris")).thenReturn(true);

        boolean result = cityVisitedService.isCityVisited("Paris");

        assertThat(result).isTrue();
        verify(cityVisitedRepository).existsByCity("Paris");
    }

    @Test
    void isCityVisited_shouldReturnFalse_whenCityDoesNotExist() {
        when(cityVisitedRepository.existsByCity("Rome")).thenReturn(false);

        boolean result = cityVisitedService.isCityVisited("Rome");

        assertThat(result).isFalse();
        verify(cityVisitedRepository).existsByCity("Rome");
    }

    @Test
    void markCityASVisited_shouldSaveCity_whenCityNotYetVisited() {
        when(cityVisitedRepository.existsByCity("London")).thenReturn(false);

        cityVisitedService.markCityASVisited("London");

        ArgumentCaptor<CityVisited> captor = ArgumentCaptor.forClass(CityVisited.class);
        verify(cityVisitedRepository).save(captor.capture());

        CityVisited saved = captor.getValue();
        assertThat(saved.getCity()).isEqualTo("London");
    }

    @Test
    void markCityASVisited_shouldNotSaveCity_whenCityAlreadyVisited() {
        when(cityVisitedRepository.existsByCity("Berlin")).thenReturn(true);

        cityVisitedService.markCityASVisited("Berlin");

        verify(cityVisitedRepository, never()).save(any(CityVisited.class));
    }
}