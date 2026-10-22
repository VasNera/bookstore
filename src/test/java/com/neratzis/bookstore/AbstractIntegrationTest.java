package com.neratzis.bookstore;

import com.jayway.jsonpath.JsonPath;
import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.Role;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.RoleRepository;
import com.neratzis.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mysql.MySQLContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final String PASSWORD = "Test1234!";

    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("bookstore")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_0900_ai_ci");

    static {
        MYSQL.start(); // one container for the whole test run
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UserRepository userRepository;
    @Autowired protected RoleRepository roleRepository;
    @Autowired protected CartRepository cartRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JdbcTemplate jdbcTemplate;

    // ---------- helpers ----------

    protected String uniq() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    /** Creates a user directly in the database (with a cart). Returns the username. */
    protected String createUser(String roleName) {
        String username = "user_" + uniq();
        Role role = roleRepository.findByName(roleName).orElseThrow();

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setFirstname("Test");
        user.setLastname("User");
        user.setEmail(username + "@test.com");
        user.setPhone("6900000000");
        user.setRole(role);
        User saved = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(saved);
        cartRepository.save(cart);

        return username;
    }

    /** Logs in through the real endpoint and returns the raw JWT token. */
    protected String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected String adminToken() throws Exception {
        return login(createUser("ADMIN"));
    }

    protected String customerToken() throws Exception {
        return login(createUser("CUSTOMER"));
    }

    /** Creates a category through the admin API and returns its id. */
    protected long createCategory(String adminToken, String name, Long parentId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/admin/categories")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","parentCategoryId":%s}
                                """.formatted(name, parentId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    /** Creates a root category and a leaf under it through the admin API. Returns the leaf id. */
    protected long createLeafCategory(String adminToken) throws Exception {
        long root = createCategory(adminToken, "Root " + uniq(), null);
        return createCategory(adminToken, "Leaf " + uniq(), root);
    }

    /** Creates a book in a brand new leaf category. Returns the product id. */
    protected long createProduct(String adminToken, String price, String discountPrice, int stock) throws Exception {
        return createBook(adminToken, createLeafCategory(adminToken), price, discountPrice, stock);
    }

    /** Creates a book through the admin API and returns its id. discountPrice may be null. */
    protected long createBook(String adminToken, long categoryId, String price,
                              String discountPrice, int stock) throws Exception {
        String id = uniq();
        String body = mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Book %s","sku":"SKU-%s","isbn":"ISBN-%s","pages":100,
                                 "price":%s,"discountPrice":%s,"stock":%d,
                                 "author":"Author","publisher":"Publisher","categoryId":%d,"featured":false}
                                """.formatted(id, id, id, price, discountPrice, stock, categoryId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }
}
