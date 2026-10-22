package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.ErrorResponseDTO;
import com.neratzis.bookstore.dto.ProductSummaryDTO;
import com.neratzis.bookstore.service.IFavouriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favourites")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Favourites", description = "Own favourite products")
public class FavouriteController {

    private final IFavouriteService favouriteService;

    @Operation(summary = "List my favourites",
            description = "Product cards of the user's favourites, sorted by title. Soft-deleted products are excluded.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favourites returned",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema =
                            @Schema(implementation = ProductSummaryDTO.class)))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping
    public ResponseEntity<List<ProductSummaryDTO>> getFavourites(Authentication authentication)
            throws EntityNotFoundException {
        return new ResponseEntity<>(favouriteService.getFavourites(authentication.getName()), HttpStatus.OK);
    }

    @Operation(summary = "Add a favourite", description = "Idempotent: adding the same product again has no effect.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product is now in the favourites"),
            @ApiResponse(responseCode = "404", description = "User or product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addFavourite(Authentication authentication, @PathVariable long productId)
            throws EntityNotFoundException {
        favouriteService.addFavourite(authentication.getName(), productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Remove a favourite", description = "Idempotent. Works even if the product was soft-deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product is no longer in the favourites"),
            @ApiResponse(responseCode = "404", description = "User or product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFavourite(Authentication authentication, @PathVariable long productId)
            throws EntityNotFoundException {
        favouriteService.removeFavourite(authentication.getName(), productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

