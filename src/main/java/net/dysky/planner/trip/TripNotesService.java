package net.dysky.planner.trip;

import net.dysky.planner.exception.InvalidNotesException;
import net.dysky.planner.exception.NotesConflictException;
import net.dysky.planner.exception.NotesTooLargeException;
import net.dysky.planner.exception.TripNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TripNotesService {

    private static final String CONFLICT_MESSAGE =
            "Notes were modified by someone else. Reload the latest version and try again.";

    private final TripNotesRepository tripNotesRepository;

    private final TripRepository tripRepository;

    private final ObjectMapper objectMapper;

    private final long maxSizeBytes;

    public TripNotesService(TripNotesRepository tripNotesRepository,
                            TripRepository tripRepository,
                            ObjectMapper objectMapper,
                            @Value("${app.trip-notes.max-size-bytes:5242880}") long maxSizeBytes) {
        this.tripNotesRepository = tripNotesRepository;
        this.tripRepository = tripRepository;
        this.objectMapper = objectMapper;
        this.maxSizeBytes = maxSizeBytes;
    }

    public TripNotesDTO getNotes(UUID tripId) {
        assertTripExists(tripId);

        return tripNotesRepository.findById(tripId)
                .map(notes -> new TripNotesDTO(notes.getContent(), notes.getVersion()))
                .orElseGet(() -> new TripNotesDTO(null, 0L));
    }

    public TripNotesDTO saveNotes(UUID tripId, SaveTripNotesDTO dto, String email) {
        assertTripExists(tripId);
        validateContent(dto.content());

        TripNotes notes = tripNotesRepository.findById(tripId).orElse(null);

        if (notes == null) {
            notes = new TripNotes();
            notes.setTripId(tripId);
        } else if (dto.version() != null && !dto.version().equals(notes.getVersion())) {
            throw new NotesConflictException(CONFLICT_MESSAGE);
        }

        notes.setContent(dto.content());
        notes.setUpdatedAt(LocalDateTime.now());
        notes.setUpdatedBy(email);

        try {
            TripNotes saved = tripNotesRepository.saveAndFlush(notes);
            return new TripNotesDTO(saved.getContent(), saved.getVersion());
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            throw new NotesConflictException(CONFLICT_MESSAGE);
        }
    }

    private void assertTripExists(UUID tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new TripNotFoundException("Trip not found");
        }
    }

    private void validateContent(String content) {
        if (content.getBytes(StandardCharsets.UTF_8).length > maxSizeBytes) {
            throw new NotesTooLargeException("Notes exceed maximum size of " + maxSizeBytes + " bytes");
        }

        try {
            objectMapper.readTree(content);
        } catch (JacksonException e) {
            throw new InvalidNotesException("Notes content is not valid JSON");
        }
    }
}
