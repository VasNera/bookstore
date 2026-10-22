package com.neratzis.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartIntegrationTest extends AbstractIntegrationTest {

    private String add(String token, long productId, int quantity, int expectedStatus) throws Exception {
        return mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void newCart_isEmpty() throws Exception {
        String token = customerToken();

        mockMvc.perform(get("/api/v1/cart").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void addingAProduct_calculatesSubtotalAndTotalOnTheServer_usingTheDiscountPrice() throws Exception {
        long product = createProduct(adminToken(), "14.90", "11.90", 5);
        String token = customerToken();

        add(token, product, 2, 200);

        mockMvc.perform(get("/api/v1/cart").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(23.8))
                .andExpect(jsonPath("$.totalAmount").value(23.8));
    }

    @Test
    void addingTheSameProductTwice_increasesTheQuantity_notTheLines() throws Exception {
        long product = createProduct(adminToken(), "10.00", null, 10);
        String token = customerToken();

        add(token, product, 2, 200);
        add(token, product, 1, 200);

        mockMvc.perform(get("/api/v1/cart").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    void addingMoreThanTheStock_returns400() throws Exception {
        long product = createProduct(adminToken(), "10.00", null, 5);
        String token = customerToken();

        add(token, product, 6, 400);

        // also when the sum of two additions exceeds the stock
        add(token, product, 3, 200);
        add(token, product, 3, 400);
    }

    @Test
    void addingAProductThatDoesNotExist_returns404() throws Exception {
        add(customerToken(), 999999999L, 1, 404);
    }

    @Test
    void addingWithInvalidQuantity_returns400() throws Exception {
        long product = createProduct(adminToken(), "10.00", null, 5);

        add(customerToken(), product, 0, 400);
    }

    @Test
    void updatingTheQuantity_worksAndIsLimitedByTheStock() throws Exception {
        long product = createProduct(adminToken(), "10.00", null, 5);
        String token = customerToken();
        add(token, product, 1, 200);

        mockMvc.perform(patch("/api/v1/cart/items/" + product)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(4))
                .andExpect(jsonPath("$.totalAmount").value(40.0));

        mockMvc.perform(patch("/api/v1/cart/items/" + product)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":6}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/cart/items/" + product)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatingAProductThatIsNotInTheCart_returns404() throws Exception {
        long product = createProduct(adminToken(), "10.00", null, 5);

        mockMvc.perform(patch("/api/v1/cart/items/" + product)
                        .header("Authorization", bearer(customerToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removingAnItem_deletesTheRowFromTheDatabase() throws Exception {
        long product = createProduct(adminToken(), "10.00", null, 5);
        String username = createUser("CUSTOMER");
        String token = login(username);
        add(token, product, 1, 200);

        mockMvc.perform(delete("/api/v1/cart/items/" + product)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));

        assertThat(cartItemRows(username)).isZero(); // orphanRemoval really deleted it
    }

    @Test
    void clearingTheCart_removesEveryLine() throws Exception {
        String admin = adminToken();
        long p1 = createProduct(admin, "10.00", null, 5);
        long p2 = createProduct(admin, "12.00", null, 5);
        String username = createUser("CUSTOMER");
        String token = login(username);
        add(token, p1, 1, 200);
        add(token, p2, 1, 200);
        assertThat(cartItemRows(username)).isEqualTo(2);

        mockMvc.perform(delete("/api/v1/cart").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(cartItemRows(username)).isZero();
    }

    @Test
    void eachUserHasAnIndependentCart() throws Exception {
        long product = createProduct(adminToken(), "10.00", null, 5);
        String first = customerToken();
        String second = customerToken();

        add(first, product, 1, 200);

        mockMvc.perform(get("/api/v1/cart").header("Authorization", bearer(second)))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    private int cartItemRows(String username) {
        Integer rows = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM cart_items ci
                JOIN carts c ON c.id = ci.cart_id
                JOIN users u ON u.id = c.user_id
                WHERE u.username = ?
                """, Integer.class, username);
        return rows == null ? 0 : rows;
    }
}
