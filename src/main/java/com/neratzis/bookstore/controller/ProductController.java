package com.neratzis.bookstore.controller;


import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public product browsing and filtering")
public class ProductController {

    private final IProductService productService;

    @Operation(summary = "List and filter products",
            description = "Public. Returns a page of product cards shared by every screen (category, search, featured, offers, new). "
                    + "Filters: search, categoryId, minPrice, maxPrice, featured, onSale, productType. Soft-deleted products are excluded.")
    @ApiResponse(responseCode = "200", description = "Products returned")

    @GetMapping
    public ResponseEntity<Page<ProductSummaryDTO>> getProducts(
            @ParameterObject
            @ModelAttribute ProductFilterDTO filterDTO,
            @ParameterObject
            @PageableDefault(size = 20)Pageable pageable){

        return new ResponseEntity<>(productService.getAllProducts(filterDTO,pageable), HttpStatus.OK);
    }

    @Operation(summary = "Get a product",
            description = "Public. Returns the full details for the product's type: Book, Music, Toy or Stationery. "
                    + "The response includes the exact stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {BookReadOnlyDTO.class, MusicReadOnlyDTO.class,
                                    ToyReadOnlyDTO.class, StationeryReadOnlyDTO.class}))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDTO> getProductById(@PathVariable long id) throws EntityNotFoundException{
        return new ResponseEntity<>(productService.getProductById(id),HttpStatus.OK);

    }

}
