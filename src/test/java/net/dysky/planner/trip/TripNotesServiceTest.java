package net.dysky.planner.trip;

import net.dysky.planner.exception.InvalidNotesException;
import net.dysky.planner.exception.NotesConflictException;
import net.dysky.planner.exception.NotesTooLargeException;
import net.dysky.planner.exception.TripNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripNotesServiceTest {

    private static final long MAX_SIZE = 100L;
    private static final String EMAIL = "user@example.com";

    @Mock
    private TripNotesRepository tripNotesRepository;

    @Mock
    private TripRepository tripRepository;

    private TripNotesService service;

    private final UUID tripId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new TripNotesService(tripNotesRepository, tripRepository, JsonMapper.builder().build(), MAX_SIZE);
    }

    private TripNotes existingNotes(String content, long version) {
        TripNotes notes = new TripNotes();
        notes.setTripId(tripId);
        notes.setContent(content);
        notes.setVersion(version);
        notes.setUpdatedAt(LocalDateTime.now());
        return notes;
    }

    @Test
    void getNotes_shouldReturnNullContentAndVersionZero_whenNoRecord() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.empty());

        TripNotesDTO result = service.getNotes(tripId);

        assertThat(result.content()).isNull();
        assertThat(result.version()).isZero();
    }

    @Test
    void getNotes_shouldReturnStoredContentAndVersion() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.of(existingNotes("{\"a\":1}", 3L)));

        TripNotesDTO result = service.getNotes(tripId);

        assertThat(result.content()).isEqualTo("{\"a\":1}");
        assertThat(result.version()).isEqualTo(3L);
    }

    @Test
    void getNotes_shouldThrowTripNotFound_whenTripDoesNotExist() {
        when(tripRepository.existsById(tripId)).thenReturn(false);

        assertThatThrownBy(() -> service.getNotes(tripId)).isInstanceOf(TripNotFoundException.class);

        verifyNoInteractions(tripNotesRepository);
    }

    @Test
    void saveNotes_shouldCreateNewRecordWithNullVersion_whenNoneExists() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.empty());
        java.util.concurrent.atomic.AtomicReference<Long> versionAtSave = new java.util.concurrent.atomic.AtomicReference<>(-1L);
        when(tripNotesRepository.saveAndFlush(any(TripNotes.class))).thenAnswer(inv -> {
            TripNotes n = inv.getArgument(0);
            versionAtSave.set(n.getVersion());
            n.setVersion(0L);
            return n;
        });

        TripNotesDTO result = service.saveNotes(tripId, new SaveTripNotesDTO("{\"a\":1}", null), EMAIL);

        ArgumentCaptor<TripNotes> captor = ArgumentCaptor.forClass(TripNotes.class);
        verify(tripNotesRepository).saveAndFlush(captor.capture());
        TripNotes toSave = captor.getValue();
        assertThat(versionAtSave.get()).isNull();
        assertThat(toSave.getTripId()).isEqualTo(tripId);
        assertThat(toSave.getContent()).isEqualTo("{\"a\":1}");
        assertThat(toSave.getUpdatedBy()).isEqualTo(EMAIL);
        assertThat(toSave.getUpdatedAt()).isNotNull();
        assertThat(result.content()).isEqualTo("{\"a\":1}");
        assertThat(result.version()).isZero();
    }

    @Test
    void saveNotes_shouldUpdateAndReturnNewVersion_whenVersionMatches() {
        TripNotes existing = existingNotes("{}", 2L);
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.of(existing));
        when(tripNotesRepository.saveAndFlush(existing)).thenAnswer(inv -> {
            existing.setVersion(3L);
            return existing;
        });

        TripNotesDTO result = service.saveNotes(tripId, new SaveTripNotesDTO("{\"b\":2}", 2L), EMAIL);

        assertThat(result.content()).isEqualTo("{\"b\":2}");
        assertThat(result.version()).isEqualTo(3L);
        assertThat(existing.getUpdatedBy()).isEqualTo(EMAIL);
    }

    @Test
    void saveNotes_shouldThrowConflict_whenVersionDiffers() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.of(existingNotes("{}", 5L)));

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO("{}", 4L), EMAIL))
                .isInstanceOf(NotesConflictException.class);

        verify(tripNotesRepository, never()).saveAndFlush(any());
    }

    @Test
    void saveNotes_shouldOverwrite_whenVersionIsNullAndRecordExists() {
        TripNotes existing = existingNotes("{\"old\":true}", 5L);
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.of(existing));
        when(tripNotesRepository.saveAndFlush(existing)).thenReturn(existing);

        TripNotesDTO result = service.saveNotes(tripId, new SaveTripNotesDTO("{\"new\":true}", null), EMAIL);

        verify(tripNotesRepository).saveAndFlush(existing);
        assertThat(result.content()).isEqualTo("{\"new\":true}");
        assertThat(result.version()).isEqualTo(5L);
    }

    @Test
    void saveNotes_shouldThrowInvalidNotes_whenContentIsNotJson() {
        when(tripRepository.existsById(tripId)).thenReturn(true);

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO("{not json", null), EMAIL))
                .isInstanceOf(InvalidNotesException.class);

        verify(tripNotesRepository, never()).saveAndFlush(any());
    }

    @Test
    void saveNotes_shouldThrowTooLarge_whenContentExceedsLimit() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        String content = "\"" + "x".repeat((int) MAX_SIZE) + "\"";

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO(content, null), EMAIL))
                .isInstanceOf(NotesTooLargeException.class);

        verify(tripNotesRepository, never()).saveAndFlush(any());
    }

    @Test
    void saveNotes_shouldAccept_whenContentSizeEqualsLimit() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.empty());
        when(tripNotesRepository.saveAndFlush(any(TripNotes.class))).thenAnswer(inv -> inv.getArgument(0));
        String content = "\"" + "x".repeat((int) MAX_SIZE - 2) + "\"";
        assertThat(content.length()).isEqualTo((int) MAX_SIZE);

        TripNotesDTO result = service.saveNotes(tripId, new SaveTripNotesDTO(content, null), EMAIL);

        assertThat(result.content()).isEqualTo(content);
    }

    @Test
    void saveNotes_shouldCountBytesNotChars_whenContentIsMultiByteUtf8() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        // 60 chars but 120 bytes (2 bytes each) + 2 quotes -> over the 100 byte limit although < 100 chars
        String content = "\"" + "ą".repeat(60) + "\"";
        assertThat(content.length()).isLessThan((int) MAX_SIZE);

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO(content, null), EMAIL))
                .isInstanceOf(NotesTooLargeException.class);
    }

    @Test
    void saveNotes_shouldThrowConflict_whenOptimisticLockingFails() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.of(existingNotes("{}", 1L)));
        when(tripNotesRepository.saveAndFlush(any(TripNotes.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(TripNotes.class, tripId));

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO("{}", 1L), EMAIL))
                .isInstanceOf(NotesConflictException.class);
    }

    @Test
    void saveNotes_shouldThrowConflict_whenConcurrentFirstInsertViolatesPrimaryKey() {
        when(tripRepository.existsById(tripId)).thenReturn(true);
        when(tripNotesRepository.findById(tripId)).thenReturn(Optional.empty());
        when(tripNotesRepository.saveAndFlush(any(TripNotes.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO("{}", null), EMAIL))
                .isInstanceOf(NotesConflictException.class);
    }

    @Test
    void saveNotes_shouldThrowTripNotFound_whenTripDoesNotExist() {
        when(tripRepository.existsById(tripId)).thenReturn(false);

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO("{}", null), EMAIL))
                .isInstanceOf(TripNotFoundException.class);

        verifyNoInteractions(tripNotesRepository);
    }

    @Test
    void saveNotes_shouldThrowInvalidNotes_whenGarbageFollowsValidJson() {
        when(tripRepository.existsById(tripId)).thenReturn(true);

        assertThatThrownBy(() -> service.saveNotes(tripId, new SaveTripNotesDTO("{} xyz", null), EMAIL))
                .isInstanceOf(InvalidNotesException.class);

        verify(tripNotesRepository, never()).saveAndFlush(any());
    }
}
