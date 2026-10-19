package com.neratzis.bookstore.controller;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.core.exceptions.ValidationException;
import com.neratzis.bookstore.dto.ReviewInsertDTO;
import com.neratzis.bookstore.dto.ReviewReadOnlyDTO;
import com.neratzis.bookstore.service.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @PostMapping("/products/{productId}/reviews")
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

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewReadOnlyDTO>> getProductReviews(
            @PathVariable long productId,
            @PageableDefault(size = 10)Pageable pageable)throws EntityNotFoundException{
        return new ResponseEntity<>(reviewService.getProductReviews(productId,pageable),HttpStatus.OK);
    }

    @GetMapping("/products/{productId}/reviews/average")
    public ResponseEntity<Double> getAverageRating(
            @PathVariable long productId) throws EntityNotFoundException{
        return new ResponseEntity<>(reviewService.getAverageRating(productId),HttpStatus.OK);
    }

    @GetMapping("/latest")
    public ResponseEntity<Page<ReviewReadOnlyDTO>> getLatestReviews(@PageableDefault(size = 5)Pageable pageable) {
        return new ResponseEntity<>(reviewService.getLatestReviews(pageable),HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication, @PathVariable long reviewId)
            throws EntityNotFoundException, InvalidArgumentException {
        reviewService.deleteReview(authentication.getName(), reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
