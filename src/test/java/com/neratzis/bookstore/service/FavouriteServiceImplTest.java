package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.dto.ProductSummaryDTO;
import com.neratzis.bookstore.mapper.ProductMapper;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.ProductRepository;
import com.neratzis.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static com.neratzis.bookstore.TestData.book;
import static com.neratzis.bookstore.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FavouriteServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Spy private ProductMapper productMapper = new ProductMapper();

    @InjectMocks private FavouriteServiceImpl favouriteService;

    private final User maria = user("maria");

    @BeforeEach
    void setUp() {
        when(userRepository.findWithFavouritesByUsernameAndDeletedFalse("maria")).thenReturn(Optional.of(maria));
    }

    private Product activeProduct(long id, String title) {
        Product product = book(id, "10.00", null, 5);
        product.setTitle(title);
        when(productRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(product));
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        return product;
    }

    @Test
    void add_putsTheProductInTheFavourites() throws Exception {
        Product product = activeProduct(1, "Zorbas");

        favouriteService.addFavourite("maria", 1L);

        assertThat(maria.getFavourites()).containsExactly(product);
    }

    @Test
    void add_isIdempotent_theSameProductTwiceIsStillOneFavourite() throws Exception {
        activeProduct(1, "Zorbas");

        favouriteService.addFavourite("maria", 1L);
        favouriteService.addFavourite("maria", 1L);

        assertThat(maria.getFavourites()).hasSize(1);
    }

    @Test
    void add_aProductThatDoesNotExistOrWasDeleted_throwsNotFound() {
        when(productRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteService.addFavourite("maria", 99L))
                .isInstanceOf(EntityNotFoundException.class);
        assertThat(maria.getFavourites()).isEmpty();
    }

    @Test
    void remove_takesTheProductOut_andRemovingItAgainIsHarmless() throws Exception {
        Product product = activeProduct(1, "Zorbas");
        maria.addFavourite(product);

        favouriteService.removeFavourite("maria", 1L);
        favouriteService.removeFavourite("maria", 1L);

        assertThat(maria.getFavourites()).isEmpty();
    }

    @Test
    void remove_worksEvenIfTheProductWasSoftDeletedInTheMeantime() throws Exception {
        Product product = activeProduct(1, "Zorbas");
        maria.addFavourite(product);
        product.softDelete();

        favouriteService.removeFavourite("maria", 1L);

        assertThat(maria.getFavourites()).isEmpty();
    }

    @Test
    void list_isSortedByTitle_andHidesDeletedProducts() throws Exception {
        Product zorbas = activeProduct(1, "Zorbas");
        Product agatha = activeProduct(2, "Agatha");
        Product gone = activeProduct(3, "Deleted book");
        maria.addFavourite(zorbas);
        maria.addFavourite(agatha);
        maria.addFavourite(gone);
        gone.softDelete();

        List<ProductSummaryDTO> favourites = favouriteService.getFavourites("maria");

        assertThat(favourites).extracting(ProductSummaryDTO::title).containsExactly("Agatha", "Zorbas");
    }

    @Test
    void list_ofAUserWithoutFavourites_isEmpty() throws Exception {
        assertThat(favouriteService.getFavourites("maria")).isEmpty();
    }

    @Test
    void aUnknownUser_throwsNotFound() {
        when(userRepository.findWithFavouritesByUsernameAndDeletedFalse("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteService.getFavourites("ghost"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
