package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.ErrorResponseDTO;
import com.neratzis.bookstore.dto.ReviewInsertDTO;
import com.neratzis.bookstore.dto.ReviewReadOnlyDTO;
import com.neratzis.bookstore.dto.ValidationResponseDTO;
import com.neratzis.bookstore.service.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews")
public class ReviewController {

    private final IReviewService reviewService;

    @Operation(summary = "Review a product",
            description = "One review per user per product. The author is taken from the token and shown publicly only as first name and last initial.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ReviewReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (rating must be 1-5)",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ValidationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "The user has already reviewed this product",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("hasAuthority('CREATE_REVIEW')")
    public ResponseEntity<ReviewReadOnlyDTO> createReview(
            Authentication authentication, @PathVariable long productId,
            @Valid @RequestBody
            ReviewInsertDTO insertDTO, BindingResult bindingResult)throws ValidationException,
            EntityAlreadyExistsException, EntityNotFoundException{
        if (bindingResult.hasErrors()){
            throw new ValidationException("REVIEW","Invalid review data", bindingResult);
        }
        return new ResponseEntity<>(reviewService.createReview(authentication.getName(),
                productId,insertDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "List a product's reviews", description = "Public. Paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewReadOnlyDTO>> getProductReviews(
            @PathVariable long productId,
            @PageableDefault(size = 10)Pageable pageable)throws EntityNotFoundException{
        return new ResponseEntity<>(reviewService.getProductReviews(productId,pageable),HttpStatus.OK);
    }

    @Operation(summary = "Average rating of a product", description = "Public. Calculated from the reviews, never stored.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Average rating returned",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = Double.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping("/products/{productId}/reviews/average")
    public ResponseEntity<Double> getAverageRating(
            @PathVariable long productId) throws EntityNotFoundException{
        return new ResponseEntity<>(reviewService.getAverageRating(productId),HttpStatus.OK);
    }

    @Operation(summary = "Latest reviews", description = "Public. Used for the testimonials banner on the homepage.")
    @ApiResponse(responseCode = "200", description = "Reviews returned")

    @GetMapping("/reviews/latest")
    public ResponseEntity<Page<ReviewReadOnlyDTO>> getLatestReviews(@PageableDefault(size = 5)Pageable pageable) {
        return new ResponseEntity<>(reviewService.getLatestReviews(pageable),HttpStatus.OK);
    }

    @Operation(summary = "Delete my review", description = "A user can only delete their own review.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deleted"),
            @ApiResponse(responseCode = "400", description = "The review belongs to another user",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ErrorResponseDTO.class)))
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasAuthority('MANAGE_OWN_REVIEWS')")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication, @PathVariable long reviewId)
            throws EntityNotFoundException, InvalidArgumentException {
        reviewService.deleteReview(authentication.getName(), reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
