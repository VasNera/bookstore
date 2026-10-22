package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.IProductService;
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
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('MANAGE_PRODUCTS')")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin - Products", description = "Manage books, music, toys and stationery (MANAGE_PRODUCTS)")
public class AdminProductController {

    private final IProductService productService;

    @Operation(summary = "Create a book",
            description = "The product must belong to a leaf category. The sku and isbn must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = BookReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "A product with this sku (or isbn) already exists",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PostMapping("/books")
    public ResponseEntity<BookReadOnlyDTO> createBook(
            @Valid @RequestBody
            BookInsertDTO insertDTO,
            BindingResult bindingResult
            ) throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException, EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("BOOK", "Invalid book data", bindingResult);
        }
        return new ResponseEntity<>(productService.createBook(insertDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a book",
            description = "Full update of an existing book. The stock can be changed here.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or category not found, or the product is not a book",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "The sku (or isbn) belongs to another product, or concurrent modification",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PutMapping("/books/{id}")
    public ResponseEntity<BookReadOnlyDTO> updateBook(
            @PathVariable long id,
            @Valid @RequestBody
            BookInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException,EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("BOOK", "Invalid book data", bindingResult);
        }

        return new ResponseEntity<>(productService.updateBook(id,insertDTO),HttpStatus.OK);
    }

    @Operation(summary = "Create music",
            description = "The product must belong to a leaf category. The sku must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Music created",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = MusicReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "A product with this sku already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)))
    })


    @PostMapping("/music")
    public ResponseEntity<MusicReadOnlyDTO> createMusic(
            @Valid @RequestBody
            MusicInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException, EntityAlreadyExistsException,
    InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("MUSIC", "Invalid music data", bindingResult);
        }
        return new ResponseEntity<>(productService.createMusic(insertDTO),HttpStatus.CREATED);
    }

    @Operation(summary = "Update Music",
            description = "Full update of an existing music. The stock can be changed here.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Music updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = MusicReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or category not found, or the product is not music",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "The sku belongs to another product, or concurrent modification",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PutMapping("/music/{id}")
    public ResponseEntity<MusicReadOnlyDTO> updateMusic(
            @PathVariable long id,
            @Valid @RequestBody
            MusicInsertDTO insertDTO,
            BindingResult bindingResult
    )throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()) {
            throw new ValidationException("MUSIC", "Invalid music data", bindingResult);
        }
            return new ResponseEntity<>(productService.updateMusic(id,insertDTO),HttpStatus.OK);
    }

    @Operation(summary = "Create a Toy",
            description = "The product must belong to a leaf category. The sku and isbn must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Toy created",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ToyReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "A product with this sku (or isbn) already exists",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PostMapping("/toys")
    public ResponseEntity<ToyReadOnlyDTO> createToy(
            @Valid @RequestBody
            ToyInsertDTO insertDTO,
            BindingResult bindingResult
    )throws ValidationException,EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{

        if (bindingResult.hasErrors()){
            throw new ValidationException("TOY", "Invalid toy data", bindingResult);
        }
        return new ResponseEntity<>(productService.createToy(insertDTO),HttpStatus.CREATED);
    }

    @Operation(summary = "Update a toy",
            description = "Full update of an existing toy. The stock can be changed here.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Toy updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ToyReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or category not found, or the product is not a toy",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "The sku (or isbn) belongs to another product, or concurrent modification",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PutMapping("/toys/{id}")
    public ResponseEntity<ToyReadOnlyDTO> updateToy(
            @PathVariable long id,
            @Valid @RequestBody
            ToyInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException,EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("TOY","Invalid toy data", bindingResult);
        }
        return new ResponseEntity<>(productService.updateToy(id,insertDTO),HttpStatus.OK);
    }

    @Operation(summary = "Create Stationery",
            description = "The product must belong to a leaf category. The sku and isbn must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Stationery created",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = StationeryReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "A product with this sku (or isbn) already exists",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PostMapping("/stationery")
    public ResponseEntity<StationeryReadOnlyDTO> createStationery(
            @Valid @RequestBody
            StationeryInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException,EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("STATIONERY", "Invalid stationery data", bindingResult);
        }
        return new ResponseEntity<>(productService.createStationery(insertDTO),HttpStatus.CREATED);
    }

    @Operation(summary = "Update Stationery",
            description = "Full update of an existing stationery. The stock can be changed here.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stationery updated",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = StationeryReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or the category is not a leaf",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or category not found, or the product is not stationery",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "The sku (or isbn) belongs to another product, or concurrent modification",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PutMapping("/stationery/{id}")
    public ResponseEntity<StationeryReadOnlyDTO> updateStationery(
            @PathVariable long id,
            @Valid @RequestBody
            StationeryInsertDTO insertDTO,
            BindingResult bindingResult
    ) throws ValidationException, EntityAlreadyExistsException,
            InvalidArgumentException, EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("STATIONERY", "Invalid stationery data", bindingResult);
        }
        return new ResponseEntity<>(productService.updateStationery(id,insertDTO),HttpStatus.OK);
    }

    @Operation(summary = "Delete a product",
            description = "Soft delete. The product disappears from the catalogue but stays in existing orders and carts history.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) throws EntityNotFoundException{
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Toggle featured flag",
            description = "Switches a product between featured and not featured (used for the homepage suggestions).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Flag toggled"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PatchMapping("/products/{id}/featured")
    public ResponseEntity<Void> toggleFeatured(@PathVariable long id)throws EntityNotFoundException{
        productService.toggleFeatured(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
