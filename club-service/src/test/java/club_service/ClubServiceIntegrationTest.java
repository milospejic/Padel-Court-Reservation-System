package club_service;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import club_service.dto.ClubDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClubServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetClub_Flow() throws Exception {
        ClubDto newClub = new ClubDto("Integration Club", "Test City", "12345");

        mockMvc.perform(post("/club")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newClub)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/club")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Integration Club')]").exists());
    }

    @Test
    void getClub_NotFound() throws Exception {
        mockMvc.perform(get("/club/9999"))
                .andExpect(status().isNotFound());
    }
}