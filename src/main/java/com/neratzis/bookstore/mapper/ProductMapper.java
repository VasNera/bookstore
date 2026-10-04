package com.neratzis.bookstore.mapper;

import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.model.*;
import com.neratzis.bookstore.model.enums.Availability;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    private static final int LOW_STOCK_THRESHOLD = 5;

    public ProductSummaryDTO toSummaryDTO(Product product){
        return new ProductSummaryDTO(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getDiscountPrice(),
                resolveAvailability(product.getStock()),
                product.getImageUrl()

        );
    }

    public ProductDetailDTO toDetail(Product product){

        return switch (product){
            case Book b -> toBookDTO(b);
            case Music m -> toMusicDTO(m);
            case Toy t -> toToyDTO(t);
            case Stationery s -> toStationeryDTO(s);
            default -> throw new IllegalStateException(
                    "Unsupported product type:" + product.getClass().getSimpleName()
            );
        };

    }

    public BookReadOnlyDTO toBookDTO(Book book){
        return new BookReadOnlyDTO(
                book.getId(),
                book.getTitle(),
                book.getSku(),
                book.getDimensions(),
                book.getPrice(),
                book.getDiscountPrice(),
                book.getImageUrl(),
                resolveAvailability(book.getStock()),
                book.getDescription(),
                categoryName(book),
                book.getIsbn(),
                book.getPages(),
                book.getAuthor(),
                book.getReleaseYear(),
                book.getPublisher()

        );

    }

    public MusicReadOnlyDTO toMusicDTO(Music music){
        return new MusicReadOnlyDTO(
                music.getId(),
                music.getTitle(),
                music.getSku(),
                music.getArtist(),
                music.getProductionCompany(),
                music.getPrice(),
                music.getDiscountPrice(),
                music.getImageUrl(),
                music.getDescription(),
                categoryName(music),
                resolveAvailability(music.getStock())
        );
    }

    public ToyReadOnlyDTO toToyDTO(Toy toy){
        return new ToyReadOnlyDTO(
                toy.getId(),
                toy.getTitle(),
                toy.getSku(),
                toy.getPrice(),
                toy.getDiscountPrice(),
                toy.getDimensions(),
                toy.getAgeRange(),
                resolveAvailability(toy.getStock()),
                toy.getImageUrl(),
                toy.getDescription(),
                categoryName(toy)
        );
    }

    public StationeryReadOnlyDTO toStationeryDTO(Stationery stationery){
        return new StationeryReadOnlyDTO(
                stationery.getId(),
                stationery.getTitle(),
                stationery.getSku(),
                stationery.getPrice(),
                stationery.getDiscountPrice(),
                stationery.getDimensions(),
                resolveAvailability(stationery.getStock()),
                stationery.getCompany(),
                stationery.getImageUrl(),
                stationery.getDescription(),
                categoryName(stationery)
        );
    }

    public Book mapToBookEntity(BookInsertDTO bookInsertDTO, Category category){
        Book book = new Book();

        applyCommon(book, bookInsertDTO.title(), bookInsertDTO.sku(),
                bookInsertDTO.description(),
                bookInsertDTO.price(), bookInsertDTO.discountPrice(),
                bookInsertDTO.dimensions(),
                bookInsertDTO.stock(), bookInsertDTO.imageUrl(),
                bookInsertDTO.featured(), category);
        book.setIsbn(bookInsertDTO.isbn());
        book.setPages(bookInsertDTO.pages());
        book.setAuthor(bookInsertDTO.author());
        book.setReleaseYear(bookInsertDTO.releaseYear());
        book.setPublisher(bookInsertDTO.publisher());

        return book;
    }

    public Music mapToMusicEntity(MusicInsertDTO musicInsertDTO, Category category){
        Music music = new Music();

        applyCommon(music, musicInsertDTO.title(), musicInsertDTO.sku(),
                musicInsertDTO.description(), musicInsertDTO.price(),
                musicInsertDTO.discountPrice(), musicInsertDTO.dimensions(),
                musicInsertDTO.stock(),
                musicInsertDTO.imageUrl(),musicInsertDTO.featured(),category);

        music.setArtist(musicInsertDTO.artist());
        music.setProductionCompany(musicInsertDTO.productionCompany());

        return music;
    }

    public Toy mapToToyEntity(ToyInsertDTO toyInsertDTO, Category category){
        Toy toy = new Toy();

        applyCommon(toy, toyInsertDTO.title(), toyInsertDTO.sku(),
                toyInsertDTO.description(), toyInsertDTO.price(),
                toyInsertDTO.discountPrice(), toyInsertDTO.dimensions(),
                toyInsertDTO.stock(),
                toyInsertDTO.imageUrl(),toyInsertDTO.featured(),category);

        toy.setAgeRange(toyInsertDTO.ageRange());
        return toy;

    }

    public Stationery mapToStationeryEntity(StationeryInsertDTO stationeryInsertDTO, Category category){
        Stationery stationery = new Stationery();

        applyCommon(stationery, stationeryInsertDTO.title(),stationeryInsertDTO.sku(),
                stationeryInsertDTO.description(),stationeryInsertDTO.price(),
                stationeryInsertDTO.discountPrice(),stationeryInsertDTO.dimensions(),
                stationeryInsertDTO.stock(),stationeryInsertDTO.imageUrl(),
                stationeryInsertDTO.featured(), category);
        stationery.setCompany(stationeryInsertDTO.company());
        return stationery;
    }


    private void applyCommon(Product product, String title, String sku, String description,
                             BigDecimal price, BigDecimal discountPrice, String dimensions,
                             int stock, String imageUrl, boolean featured, Category category){
        product.setTitle(title);
        product.setSku(sku);
        product.setDescription(description);
        product.setPrice(price);
        product.setDimensions(dimensions);
        product.setDiscountPrice(discountPrice);
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        product.setFeatured(featured);
        product.setCategory(category);
    }




    private String categoryName(Product product){
        return product.getCategory() !=null ? product.getCategory().getName() : null;
    }

    private Availability resolveAvailability(int stock){
        if  (stock <=0) return Availability.OUT_OF_STOCK;
        if (stock <= LOW_STOCK_THRESHOLD) return Availability.LOW_STOCK;
        return Availability.IN_STOCK;

    }
}
