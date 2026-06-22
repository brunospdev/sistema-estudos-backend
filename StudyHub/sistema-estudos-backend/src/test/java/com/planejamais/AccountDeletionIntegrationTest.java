package com.planejamais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planejamais.dto.DisciplinaRequest;
import com.planejamais.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void excluirContaRemoveDisciplinasAssociadas() throws Exception {
        String token = register("delete-me@test.com");

        DisciplinaRequest disciplina = new DisciplinaRequest();
        disciplina.setNome("Física");

        mockMvc.perform(post("/api/disciplinas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disciplina)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/usuarios/perfil")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginPayload("delete-me@test.com", "senha123"))))
                .andExpect(status().isUnauthorized());
    }

    private String register(String email) throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setNome("Delete User");
        register.setEmail(email);
        register.setSenha("senha123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private record LoginPayload(String email, String senha) {}
}
