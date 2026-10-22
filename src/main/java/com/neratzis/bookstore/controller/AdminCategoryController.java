package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasAuthority('MANAGE_CATEGORIES')")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin - Categories", description = "Requires MANAGE_CATEGORIES")
public class AdminCategoryController {

    private final ICategoryService categoryService;

    @Operation(summary = "Create a category",
            description = "Creates a root category, or a subcategory if parentCategoryId is given.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CategoryReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Parent category not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "A category with this name already exists",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })


    @PostMapping
    public ResponseEntity<CategoryReadOnlyDTO> createCategory(
            @Valid @RequestBody
            CategoryInsertDTO categoryInsertDTO,
            BindingResult bindingResult
            ) throws ValidationException, EntityAlreadyExistsException, EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("CATEGORY", "Invalid category data", bindingResult);
        }
        return new ResponseEntity<>(categoryService.createCategory(categoryInsertDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a category",
            description = "Rename or move a category. A category cannot be moved under itself or one of its descendants.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CategoryReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or cycle in the category tree",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category or parent not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "A category with this name already exists",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PutMapping("/{id}")
    public ResponseEntity<CategoryReadOnlyDTO> updateCategory(
            @PathVariable long id,
            @Valid @RequestBody
            CategoryUpdateDTO categoryUpdateDTO,
            BindingResult bindingResult
    )throws ValidationException,EntityNotFoundException,EntityAlreadyExistsException, InvalidArgumentException {

        if (bindingResult.hasErrors()){
            throw new ValidationException("CATEGORY", "Invalid category data", bindingResult);
        }
        return new ResponseEntity<>(categoryService.updateCategory(id, categoryUpdateDTO),HttpStatus.OK);
    }

    @Operation(summary = "Delete a category",
            description = "Not allowed if the category has subcategories or products.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "400", description = "Category has subcategories or products",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id) throws EntityNotFoundException,
            InvalidArgumentException{
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
