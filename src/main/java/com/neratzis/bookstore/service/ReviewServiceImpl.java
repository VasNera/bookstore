package com.neratzis.bookstore.service;


import com.neratzis.bookstore.core.exceptions.AppGenericException;
import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.ReviewInsertDTO;
import com.neratzis.bookstore.dto.ReviewReadOnlyDTO;
import com.neratzis.bookstore.mapper.ReviewMapper;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.Review;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.ProductRepository;
import com.neratzis.bookstore.repository.ReviewRepository;
import com.neratzis.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements IReviewService{

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public ReviewReadOnlyDTO createReview(String username, Long productId, ReviewInsertDTO reviewInsertDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new EntityNotFoundException("USER", "User with username: "
                        + username + " not found"));

        Product product = findActiveProduct(productId);

        if (reviewRepository.existsByUserUsernameAndProduct(username, product)) {
            throw new EntityAlreadyExistsException("REVIEW", "You already reviewed the product");
        }

            Review saved = reviewRepository.save(
                    reviewMapper.mapToReviewEntity(reviewInsertDTO, user, product));
            log.info("Review from user={} for product={} created", username, product.getSku());
            return reviewMapper.toReviewDTO(saved);
        }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewReadOnlyDTO> getProductReviews(Long productId, Pageable pageable)
            throws EntityNotFoundException {

        Product product = findActiveProduct(productId);

        return reviewRepository.findByProductOrderByCreatedAtDesc(product,pageable)
                .map(reviewMapper::toReviewDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) throws EntityNotFoundException {
        Product product = findActiveProduct(productId);
        return reviewRepository.findAverageRatingByProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewReadOnlyDTO> getLatestReviews(Pageable pageable) {
        return reviewRepository.findByCommentIsNotNullOrderByCreatedAtDesc(pageable)
                .map(reviewMapper::toReviewDTO);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void deleteReview(String username, Long reviewId)
            throws EntityNotFoundException, InvalidArgumentException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("REVIEW", "Review not found"));

        if (!review.getUser().getUsername().equals(username)){
            throw new InvalidArgumentException("REVIEW", "You can't delete someone else review");
        }
        reviewRepository.delete(review);
        log.info("Review with id ={} deleted", reviewId);

    }

    //Helper

    private Product findActiveProduct(Long productId) throws EntityNotFoundException{
        return productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(()-> new EntityNotFoundException("PRODUCT", "Product with id: "
                + productId + " not found"));
    }
}
