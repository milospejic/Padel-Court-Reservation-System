package review_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import review_service.dto.ReviewDto;
import review_service.repository.ReviewRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    void addReview_Success() throws Exception {
        ReviewDto review = new ReviewDto(0, "user@test.com", 1, 5, "Great court!", LocalDate.now());

        when(restTemplate.getForEntity(contains("user-service"), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(restTemplate.getForEntity(contains("club-service"), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Great court!"));

        mockMvc.perform(get("/review/club/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(5));
    }
}