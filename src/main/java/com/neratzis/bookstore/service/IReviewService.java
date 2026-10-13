package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.ReviewInsertDTO;
import com.neratzis.bookstore.dto.ReviewReadOnlyDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReviewService {

    ReviewReadOnlyDTO createReview(String username, Long productId, ReviewInsertDTO reviewInsertDTO)
        throws EntityNotFoundException, EntityAlreadyExistsException;

    Page<ReviewReadOnlyDTO> getProductReviews(Long productId, Pageable pageable)
        throws EntityNotFoundException;

    Double getAverageRating(Long productId) throws EntityNotFoundException;

    Page<ReviewReadOnlyDTO> getLatestReviews(Pageable pageable);

    void deleteReview(String username, Long reviewId) throws EntityNotFoundException, InvalidArgumentException;
}
