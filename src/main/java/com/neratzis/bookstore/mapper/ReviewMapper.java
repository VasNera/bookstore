package com.neratzis.bookstore.mapper;


import com.neratzis.bookstore.dto.ReviewInsertDTO;
import com.neratzis.bookstore.dto.ReviewReadOnlyDTO;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.Review;
import com.neratzis.bookstore.model.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {


public Review mapToReviewEntity(ReviewInsertDTO reviewInsertDTO, User user, Product product){

    Review review = new Review();

    review.setComment(reviewInsertDTO.comment());
    review.setRating(reviewInsertDTO.rating());
    review.setUser(user);
    review.setProduct(product);

return review;
    }


public ReviewReadOnlyDTO toReviewDTO(Review review) {
    return new ReviewReadOnlyDTO(
            review.getId(),
            review.getComment(),
            review.getRating(),
            review.getUser().getUsername(),
            review.getCreatedAt(),
            review.getProduct().getTitle()
    );

    }
}
