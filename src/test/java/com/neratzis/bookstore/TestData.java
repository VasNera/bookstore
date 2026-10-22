package com.neratzis.bookstore;

import com.neratzis.bookstore.model.Book;
import com.neratzis.bookstore.model.Cart;
import com.neratzis.bookstore.model.CartItem;
import com.neratzis.bookstore.model.Order;
import com.neratzis.bookstore.model.OrderItem;
import com.neratzis.bookstore.model.Product;
import com.neratzis.bookstore.model.User;
import com.neratzis.bookstore.model.enums.OrderStatus;
import com.neratzis.bookstore.model.enums.PaymentMethod;
import com.neratzis.bookstore.model.enums.PaymentStatus;

import java.math.BigDecimal;

/** Small factory methods that build plain entities for unit tests (no database involved). */
public final class TestData {

    private TestData() {
    }

    public static Book book(long id, String price, String discountPrice, int stock) {
        Book book = new Book();
        book.setId(id);
        book.setTitle("Book " + id);
        book.setSku("SKU-" + id);
        book.setPrice(new BigDecimal(price));
        book.setDiscountPrice(discountPrice == null ? null : new BigDecimal(discountPrice));
        book.setStock(stock);
        return book;
    }

    public static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setFirstname("Maria");
        user.setLastname("Papadopoulou");
        return user;
    }

    public static Cart cart(User user) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        return cart;
    }

    public static CartItem cartItem(long id, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    /** An order with one line that bought {@code quantity} pieces of {@code product}. */
    public static Order order(User owner, OrderStatus status, PaymentStatus paymentStatus,
                              Product product, int quantity) {
        Order order = new Order();
        order.setId(1L);
        order.setUser(owner);
        order.setOrderStatus(status);
        order.setPaymentStatus(paymentStatus);
        order.setPaymentMethod(PaymentMethod.CARD);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductTitle(product.getTitle());
        item.setQuantity(quantity);
        item.setPriceAtPurchase(product.getPrice());
        order.addOrderItem(item);
        return order;
    }
}
