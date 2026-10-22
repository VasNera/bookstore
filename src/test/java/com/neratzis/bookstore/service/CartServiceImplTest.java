package com.neratzis.bookstore.service;

import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.CartItemInsertDTO;
import com.neratzis.bookstore.dto.CartReadOnlyDTO;
import com.neratzis.bookstore.mapper.CartMapper;
import com.neratzis.bookstore.mapper.ProductMapper;
import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.CartItem;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.repository.CartRepository;
import com.neratzis.bookstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static com.neratzis.bookstore.TestData.cart;
import static com.neratzis.bookstore.TestData.cartItem;
import static com.neratzis.bookstore.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Spy private CartMapper cartMapper = new CartMapper(new ProductMapper(), new ProductPriceCalculator());

    @InjectMocks private CartServiceImpl cartService;

    private final User maria = user("maria");
    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = cart(maria);
        when(cartRepository.findByUserUsername("maria")).thenReturn(Optional.of(cart));
    }

    private Product productInCatalogue(long id, String price, String discount, int stock) {
        Product product = book(id, price, discount, stock);
        when(productRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(product));
        return product;
    }

    private CartItem quantityOf(Product product) {
        return cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst().orElseThrow();
    }

    // ---------- add ----------

    @Test
    void add_aNewProduct_createsALine() throws Exception {
        Product product = productInCatalogue(1, "10.00", null, 5);

        cartService.addItemToCart("maria", new CartItemInsertDTO(1L, 2));

        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(quantityOf(product).getQuantity()).isEqualTo(2);
    }

    @Test
    void add_theSameProductAgain_increasesTheQuantity_insteadOfAddingALine() throws Exception {
        Product product = productInCatalogue(1, "10.00", null, 10);
        cart.addCartItem(cartItem(1, product, 2));

        cartService.addItemToCart("maria", new CartItemInsertDTO(1L, 3));

        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(quantityOf(product).getQuantity()).isEqualTo(5);
    }

    @Test
    void add_isLimitedByTheStock_includingWhatIsAlreadyInTheCart() throws Exception {
        Product product = productInCatalogue(1, "10.00", null, 5);
        cart.addCartItem(cartItem(1, product, 3));

        assertThatThrownBy(() -> cartService.addItemToCart("maria", new CartItemInsertDTO(1L, 3)))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> cartService.addItemToCart("maria", new CartItemInsertDTO(1L, 6)))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(quantityOf(product).getQuantity()).isEqualTo(3); // unchanged
    }

    @Test
    void add_upToExactlyTheStock_isAllowed() throws Exception {
        Product product = productInCatalogue(1, "10.00", null, 5);

        cartService.addItemToCart("maria", new CartItemInsertDTO(1L, 5));

        assertThat(quantityOf(product).getQuantity()).isEqualTo(5);
    }

    @Test
    void add_aProductThatDoesNotExistOrWasDeleted_throwsNotFound() {
        when(productRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart("maria", new CartItemInsertDTO(99L, 1)))
                .isInstanceOf(EntityNotFoundException.class);
        assertThat(cart.getCartItems()).isEmpty();
    }

    // ---------- totals are calculated on the server ----------

    @Test
    void theCart_calculatesSubtotalsAndTotal_usingTheEffectivePrice() throws Exception {
        Product discounted = productInCatalogue(1, "14.90", "11.90", 10);
        Product regular = productInCatalogue(2, "20.00", null, 10);
        cart.addCartItem(cartItem(1, discounted, 2));
        cart.addCartItem(cartItem(2, regular, 1));

        CartReadOnlyDTO dto = cartService.getCart("maria");

        assertThat(dto.items()).hasSize(2);
        assertThat(dto.items().get(0).subtotal()).isEqualByComparingTo("23.80"); // 2 x the discount price
        assertThat(dto.items().get(1).subtotal()).isEqualByComparingTo("20.00");
        assertThat(dto.totalAmount()).isEqualByComparingTo("43.80");
    }

    @Test
    void anEmptyCart_hasATotalOfZero() throws Exception {
        CartReadOnlyDTO dto = cartService.getCart("maria");

        assertThat(dto.items()).isEmpty();
        assertThat(dto.totalAmount()).isEqualByComparingTo("0");
    }

    // ---------- update ----------

    @Test
    void update_changesTheQuantity() throws Exception {
        Product product = productInCatalogue(1, "10.00", null, 5);
        cart.addCartItem(cartItem(1, product, 1));

        CartReadOnlyDTO dto = cartService.updateItemQuantity("maria", 1L, 4);

        assertThat(quantityOf(product).getQuantity()).isEqualTo(4);
        assertThat(dto.totalAmount()).isEqualByComparingTo("40.00");
    }

    @Test
    void update_isLimitedByTheStock_andRejectsNonPositiveQuantities() {
        Product product = productInCatalogue(1, "10.00", null, 5);
        cart.addCartItem(cartItem(1, product, 1));

        assertThatThrownBy(() -> cartService.updateItemQuantity("maria", 1L, 6))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> cartService.updateItemQuantity("maria", 1L, 0))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> cartService.updateItemQuantity("maria", 1L, -2))
                .isInstanceOf(InvalidArgumentException.class);

        assertThat(quantityOf(product).getQuantity()).isEqualTo(1);
    }

    @Test
    void update_aProductThatIsNotInTheCart_throwsNotFound() {
        assertThatThrownBy(() -> cartService.updateItemQuantity("maria", 1L, 2))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ---------- remove and clear ----------

    @Test
    void remove_deletesOnlyThatLine() throws Exception {
        Product first = productInCatalogue(1, "10.00", null, 5);
        Product second = productInCatalogue(2, "12.00", null, 5);
        cart.addCartItem(cartItem(1, first, 1));
        cart.addCartItem(cartItem(2, second, 1));

        cartService.removeItem("maria", 1L);

        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems().iterator().next().getProduct()).isSameAs(second);
    }

    @Test
    void remove_aProductThatIsNotInTheCart_throwsNotFound() {
        assertThatThrownBy(() -> cartService.removeItem("maria", 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void clear_removesEveryLine() throws Exception {
        cart.addCartItem(cartItem(1, productInCatalogue(1, "10.00", null, 5), 1));
        cart.addCartItem(cartItem(2, productInCatalogue(2, "12.00", null, 5), 2));

        cartService.clearCart("maria");

        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    void aUserWithoutACart_throwsNotFound() {
        when(cartRepository.findByUserUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart("ghost"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
