package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.dto.CategoryReadOnlyDTO;
import com.neratzis.bookstore.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Public category tree")
public class CategoryController {

    private final ICategoryService categoryService;


    @Operation(summary = "Get the category tree",
            description = "Public. Returns the nested tree (roots with their subcategories), sorted by name. Products can only belong to leaf categories.")
    @ApiResponse(responseCode = "200", description = "Category tree returned",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CategoryReadOnlyDTO.class))))

    @GetMapping
    public ResponseEntity<List<CategoryReadOnlyDTO>> getCategoryTree(){
        return new ResponseEntity<>(categoryService.getCategoryTree(), HttpStatus.OK);

    }
}
