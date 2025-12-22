package reservation_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import reservation_service.dto.ReservationDto;
import reservation_service.repository.ReservationRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    void createReservation_Success() throws Exception {
        ReservationDto reservation = new ReservationDto(0, "user@test.com", 1, 3, LocalDateTime.now().plusDays(1));

        when(restTemplate.getForEntity(contains("user-service"), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(restTemplate.getForEntity(contains("club-service"), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        doNothing().when(rabbitTemplate).convertAndSend(any(String.class), any(Object.class));

        mockMvc.perform(post("/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courtNumber").value(3));

        mockMvc.perform(get("/reservation/user/user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clubId").value(1));
    }
}