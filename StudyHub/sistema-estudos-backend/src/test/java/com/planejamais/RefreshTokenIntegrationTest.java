package com.planejamais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planejamais.dto.RegisterRequest;
import com.planejamais.security.SecurityConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RefreshTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void refreshIgnoresExpiredBearerOnPublicPath() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setNome("Refresh User");
        register.setEmail("refresh@test.com");
        register.setSenha("senha1234");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn();

        var refreshCookie = registerResult.getResponse().getCookie("refresh_token");

        mockMvc.perform(post("/api/auth/refresh")
                        .header(SecurityConstants.CLIENT_HEADER, SecurityConstants.CLIENT_WEB)
                        .header("Authorization", "Bearer token-expirado-invalido")
                        .cookie(new MockCookie(refreshCookie.getName(), refreshCookie.getValue())))
                .andExpect(status().isOk());
    }

    @Test
    void refreshWithoutClientHeaderReturns403() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isForbidden());
    }
}
