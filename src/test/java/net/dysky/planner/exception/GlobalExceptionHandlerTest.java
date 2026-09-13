package net.dysky.planner.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleFriendshipExistsException_shouldReturn409() throws Exception {
        mockMvc.perform(get("/test/exceptions/friendship-exists")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Friendship already exists")));
    }

    @Test
    void handleUserExistException_shouldReturn409() throws Exception {
        mockMvc.perform(get("/test/exceptions/user-exist")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("User already exists")));
    }

    @Test
    void handleBalanceOverBudgetException_shouldReturn409() throws Exception {
        mockMvc.perform(get("/test/exceptions/balance-over-budget")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Balance exceeds budget")));
    }

    @Test
    void handleHasNoPermissionException_shouldReturn401() throws Exception {
        mockMvc.perform(get("/test/exceptions/has-no-permission")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("No permission")));
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() throws Exception {
        mockMvc.perform(get("/test/exceptions/access-denied")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", is("Access Denied: Forbidden access")));
    }

    @Test
    void handleDataIntegrityViolation_withTripConstraint_shouldReturn409() throws Exception {
        mockMvc.perform(get("/test/exceptions/data-integrity-trip")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("You already have a trip with this name. Please choose a different name.")));
    }

    @Test
    void handleDataIntegrityViolation_generic_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/exceptions/data-integrity-generic")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Data integrity violation occurred.")));
    }

    @Test
    void handleUnexpectedError_withBlankMessage_shouldReturn500WithDefaultMessage() throws Exception {
        mockMvc.perform(get("/test/exceptions/unexpected-blank")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("Internal Server Error")));
    }

    @Test
    void handleUnexpectedError_withProvidedMessage_shouldReturn500WithMessage() throws Exception {
        mockMvc.perform(get("/test/exceptions/unexpected-custom")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", is("Unexpected runtime error")));
    }

    @RestController
    static class ExceptionTestController {

        @GetMapping("/test/exceptions/friendship-exists")
        public void throwFriendshipExists() {
            throw new FriendshipExistsException("Friendship already exists");
        }

        @GetMapping("/test/exceptions/user-exist")
        public void throwUserExist() {
            throw new UserExistException("User already exists");
        }

        @GetMapping("/test/exceptions/balance-over-budget")
        public void throwBalanceOverBudget() {
            throw new BalanceOverBudgetException("Balance exceeds budget");
        }

        @GetMapping("/test/exceptions/has-no-permission")
        public void throwHasNoPermission() {
            throw new HasNoPermissionException("No permission");
        }

        @GetMapping("/test/exceptions/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Forbidden access");
        }

        @GetMapping("/test/exceptions/data-integrity-trip")
        public void throwDataIntegrityTrip() {
            throw new DataIntegrityViolationException("Violation: uk_trip_name_and_creator constraint failed");
        }

        @GetMapping("/test/exceptions/data-integrity-generic")
        public void throwDataIntegrityGeneric() {
            throw new DataIntegrityViolationException("Generic constraint violation");
        }

        @GetMapping("/test/exceptions/unexpected-blank")
        public void throwUnexpectedBlank() {
            throw new RuntimeException("   ");
        }

        @GetMapping("/test/exceptions/unexpected-custom")
        public void throwUnexpectedCustom() {
            throw new RuntimeException("Unexpected runtime error");
        }
    }
}