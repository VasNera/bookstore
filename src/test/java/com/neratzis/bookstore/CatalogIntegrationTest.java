package com.neratzis.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CatalogIntegrationTest extends AbstractIntegrationTest {

    private String bookJson(String sku, long categoryId) {
        String id = uniq();
        return """
                {"title":"Book %s","sku":"%s","isbn":"ISBN-%s","pages":100,"price":10.00,
                 "stock":3,"author":"A","publisher":"P","categoryId":%d,"featured":false}
                """.formatted(id, sku, id, categoryId);
    }

    // ---------- products ----------

    @Test
    void createdBook_isReturnedWithTypeSpecificFieldsAndStock() throws Exception {
        String admin = adminToken();
        long id = createProduct(admin, "20.00", null, 25);

        mockMvc.perform(get("/api/v1/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.isbn").exists())
                .andExpect(jsonPath("$.pages").value(100))
                .andExpect(jsonPath("$.stock").value(25))
                .andExpect(jsonPath("$.availability").value("IN_STOCK"));
    }

    @Test
    void createBook_withExistingSku_returns409() throws Exception {
        String admin = adminToken();
        long leaf = createLeafCategory(admin);
        String sku = "SKU-DUP-" + uniq();

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(sku, leaf)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson(sku, leaf)))
                .andExpect(status().isConflict());
    }

    @Test
    void createBook_inANonLeafCategory_returns400() throws Exception {
        String admin = adminToken();
        long root = createCategory(admin, "Root " + uniq(), null);
        createCategory(admin, "Leaf " + uniq(), root); // root now has a child

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson("SKU-" + uniq(), root)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBook_withInvalidData_returns400() throws Exception {
        String admin = adminToken();

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"price\":-1,\"stock\":1,\"featured\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void stockBoundaries_areReflectedInAvailability() throws Exception {
        String admin = adminToken();
        long out = createProduct(admin, "10.00", null, 0);
        long low = createProduct(admin, "10.00", null, 5);

        mockMvc.perform(get("/api/v1/products/" + out))
                .andExpect(jsonPath("$.availability").value("OUT_OF_STOCK"));
        mockMvc.perform(get("/api/v1/products/" + low))
                .andExpect(jsonPath("$.availability").value("LOW_STOCK"));
    }

    @Test
    void getProduct_thatDoesNotExist_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void filters_workAloneAndCombined() throws Exception {
        String admin = adminToken();
        long leaf = createLeafCategory(admin);
        createBook(admin, leaf, "10.00", null, 10);     // cheap, no discount
        createBook(admin, leaf, "30.00", "25.00", 10);  // expensive, on sale
        String cat = String.valueOf(leaf);

        // category
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat))
                .andExpect(jsonPath("$.totalElements").value(2));
        // search is case-insensitive and partial
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat).param("search", "bOoK"))
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat).param("search", "zzzzzz"))
                .andExpect(jsonPath("$.totalElements").value(0));
        // price range
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat).param("minPrice", "20"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat).param("maxPrice", "20"))
                .andExpect(jsonPath("$.totalElements").value(1));
        // on sale
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat).param("onSale", "true"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].discountPrice").value(25.0));
        // product type
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat).param("productType", "BOOK"))
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat).param("productType", "MUSIC"))
                .andExpect(jsonPath("$.totalElements").value(0));
        // combined
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat)
                        .param("productType", "BOOK").param("onSale", "true").param("minPrice", "20"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deletedProduct_disappearsFromTheCatalogue() throws Exception {
        String admin = adminToken();
        long leaf = createLeafCategory(admin);
        long id = createBook(admin, leaf, "10.00", null, 10);
        String cat = String.valueOf(leaf);

        mockMvc.perform(get("/api/v1/products").param("categoryId", cat))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(delete("/api/v1/admin/products/" + id)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/products").param("categoryId", cat))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ---------- categories ----------

    @Test
    void categoryTree_containsTheSubcategories() throws Exception {
        String admin = adminToken();
        String rootName = "TreeRoot " + uniq();
        long root = createCategory(admin, rootName, null);
        createCategory(admin, "TreeLeaf " + uniq(), root);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='" + rootName + "')].subCategories.length()").value(1));
    }

    @Test
    void deleteCategory_withSubcategories_returns400() throws Exception {
        String admin = adminToken();
        long root = createCategory(admin, "Root " + uniq(), null);
        createCategory(admin, "Leaf " + uniq(), root);

        mockMvc.perform(delete("/api/v1/admin/categories/" + root)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_withProducts_returns400() throws Exception {
        String admin = adminToken();
        long leaf = createLeafCategory(admin);
        createBook(admin, leaf, "10.00", null, 10);

        mockMvc.perform(delete("/api/v1/admin/categories/" + leaf)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_thatIsEmpty_returns204() throws Exception {
        String admin = adminToken();
        long leaf = createLeafCategory(admin);

        mockMvc.perform(delete("/api/v1/admin/categories/" + leaf)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void movingACategoryUnderItsOwnDescendant_returns400() throws Exception {
        String admin = adminToken();
        long root = createCategory(admin, "Root " + uniq(), null);
        long child = createCategory(admin, "Child " + uniq(), root);

        mockMvc.perform(put("/api/v1/admin/categories/" + root)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Moved " + uniq() + "\",\"parentCategoryId\":" + child + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_withExistingName_returns409() throws Exception {
        String admin = adminToken();
        String name = "Dup " + uniq();
        createCategory(admin, name, null);

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isConflict());
    }
}
