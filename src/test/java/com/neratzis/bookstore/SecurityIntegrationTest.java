package com.neratzis.bookstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/orders", "/api/v1/cart", "/api/v1/favourites",
            "/api/v1/users/me", "/api/v1/admin/orders"})
    void protectedEndpoints_withoutToken_return401(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/products", "/api/v1/categories", "/api/v1/reviews/latest"})
    void publicEndpoints_withoutToken_return200(String url) throws Exception {
        mockMvc.perform(get(url)).andExpect(status().isOk());
    }

    @Test
    void customer_cannotAccessAdminEndpoints() throws Exception {
        String token = customerToken();

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_canAccessAdminEndpoints() throws Exception {
        String token = adminToken();

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat " + uniq() + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void login_wrongPasswordAndUnknownUser_returnTheSame401() throws Exception {
        String existing = createUser("CUSTOMER");

        String wrongPassword = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + existing + "\",\"password\":\"Wrong1234!\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String unknownUser = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody_" + uniq() + "\",\"password\":\"Wrong1234!\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        // no user enumeration: the two answers must be identical
        assertThat(wrongPassword).isEqualTo(unknownUser);
    }

    @Test
    void garbageToken_returns401_not500() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removingACapabilityFromTheRole_takesEffectImmediately() throws Exception {
        String token = customerToken();

        jdbcTemplate.update("""
                DELETE rc FROM roles_capabilities rc
                JOIN roles r ON r.id = rc.role_id
                JOIN capabilities c ON c.id = rc.capability_id
                WHERE r.name = 'CUSTOMER' AND c.name = 'CREATE_REVIEW'
                """);
        try {
            mockMvc.perform(post("/api/v1/products/999999/reviews")
                            .header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"rating\":5,\"comment\":\"x\"}"))
                    .andExpect(status().isForbidden());
        } finally {
            jdbcTemplate.update("""
                    INSERT INTO roles_capabilities (role_id, capability_id)
                    SELECT r.id, c.id FROM roles r, capabilities c
                    WHERE r.name = 'CUSTOMER' AND c.name = 'CREATE_REVIEW'
                    """);
        }

        // capability is back: the request now passes authorization and fails only because the product does not exist
        mockMvc.perform(post("/api/v1/products/999999/reviews")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
}
