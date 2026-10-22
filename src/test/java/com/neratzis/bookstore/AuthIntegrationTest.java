package com.neratzis.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {

    private String registerBody(String username, String email, String extra) {
        return """
                {"username":"%s","password":"Test1234!","firstname":"Maria","lastname":"Papas",
                 "phone":"6900000001","email":"%s"%s}
                """.formatted(username, email, extra);
    }

    @Test
    void register_createsACustomer_evenIfTheClientSendsRoleAdmin() throws Exception {
        String username = "reg_" + uniq();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, username + "@test.com", ",\"role\":\"ADMIN\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_alsoCreatesACart() throws Exception {
        String username = "reg_" + uniq();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, username + "@test.com", "")))
                .andExpect(status().isCreated());

        assertThat(cartRepository.findByUserUsername(username)).isPresent();
    }

    @Test
    void register_withExistingUsernameOrEmail_returns409() throws Exception {
        String username = "reg_" + uniq();
        String email = username + "@test.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, email, "")))
                .andExpect(status().isCreated());

        // same username, different email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, "other_" + email, "")))
                .andExpect(status().isConflict());

        // different username, same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("other_" + username, email, "")))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withInvalidData_returns400WithAnErrorsMap() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"weak","firstname":"","lastname":"",
                                 "phone":"6900000001","email":"not-an-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void login_withValidCredentials_returnsTokenAndRole() throws Exception {
        String username = createUser("CUSTOMER");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
}
