package com.neratzis.bookstore.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static com.neratzis.bookstore.TestData.book;
import static com.neratzis.bookstore.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Spy private ReviewMapper reviewMapper = new ReviewMapper();

    @InjectMocks private ReviewServiceImpl reviewService;

    private final User maria = user("maria");
    private final Product product = book(1, "10.00", null, 5);

    private void givenUserAndProduct() {
        when(userRepository.findByUsernameAndDeletedFalse("maria")).thenReturn(Optional.of(maria));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
    }

    // ---------- create ----------

    @Test
    void create_savesTheReview_andShowsOnlyTheFirstNameAndTheInitialOfTheLastName() throws Exception {
        givenUserAndProduct();
        when(reviewRepository.existsByUserUsernameAndProduct("maria", product)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewReadOnlyDTO dto = reviewService.createReview("maria", 1L, new ReviewInsertDTO(5, "Great"));

        assertThat(dto.authorName()).isEqualTo("Maria P."); // never the username
        assertThat(dto.rating()).isEqualTo(5);
        assertThat(dto.comment()).isEqualTo("Great");
        assertThat(dto.productTitle()).isEqualTo("Book 1");
    }

    @Test
    void create_theAuthorIsTheLoggedInUser_notSomethingTheClientSends() throws Exception {
        givenUserAndProduct();
        when(reviewRepository.existsByUserUsernameAndProduct("maria", product)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        reviewService.createReview("maria", 1L, new ReviewInsertDTO(4, "Ok"));

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(maria);
        assertThat(captor.getValue().getProduct()).isSameAs(product);
    }

    @Test
    void create_aSecondReviewOfTheSameProductByTheSameUser_isRejected() {
        givenUserAndProduct();
        when(reviewRepository.existsByUserUsernameAndProduct("maria", product)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview("maria", 1L, new ReviewInsertDTO(5, "Again")))
                .isInstanceOf(EntityAlreadyExistsException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void create_forAProductThatDoesNotExistOrWasDeleted_throwsNotFound() {
        when(userRepository.findByUsernameAndDeletedFalse("maria")).thenReturn(Optional.of(maria));
        when(productRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview("maria", 99L, new ReviewInsertDTO(5, "x")))
                .isInstanceOf(EntityNotFoundException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void create_forAnUnknownUser_throwsNotFound() {
        when(userRepository.findByUsernameAndDeletedFalse("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview("ghost", 1L, new ReviewInsertDTO(5, "x")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ---------- delete ----------

    private Review reviewBy(User author) {
        Review review = new Review();
        review.setId(10L);
        review.setUser(author);
        review.setProduct(product);
        review.setRating(5);
        return review;
    }

    @Test
    void delete_ownReview_works() throws Exception {
        Review review = reviewBy(maria);
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        reviewService.deleteReview("maria", 10L);

        verify(reviewRepository).delete(review);
    }

    @Test
    void delete_someoneElsesReview_isRejected_andNothingIsDeleted() {
        Review review = reviewBy(maria);
        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview("giorgos", 10L))
                .isInstanceOf(InvalidArgumentException.class);

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void delete_aReviewThatDoesNotExist_throwsNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview("maria", 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
