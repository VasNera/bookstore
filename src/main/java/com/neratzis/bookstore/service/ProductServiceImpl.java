package com.neratzis.bookstore.service;


import com.neratzis.bookstore.core.exceptions.AppGenericException;
import com.neratzis.bookstore.core.exceptions.EntityAlreadyExistsException;
import com.neratzis.bookstore.core.exceptions.EntityNotFoundException;
import com.neratzis.bookstore.core.exceptions.InvalidArgumentException;
import com.neratzis.bookstore.dto.*;
import com.neratzis.bookstore.mapper.ProductMapper;
import com.neratzis.bookstore.model.*;
import com.neratzis.bookstore.repository.*;
import com.neratzis.bookstore.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;


    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryDTO> getAllProducts(ProductFilterDTO productFilterDTO, Pageable pageable) {
        return productRepository.findAll(ProductSpecification.build(productFilterDTO), pageable)
                .map(productMapper::toSummaryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long id) throws EntityNotFoundException {
        return productMapper.toDetail(findActiveProduct(id));
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public BookReadOnlyDTO createBook(BookInsertDTO bookInsertDTO)
            throws EntityAlreadyExistsException, EntityNotFoundException, InvalidArgumentException {
        validateSkuIsFree(bookInsertDTO.sku());
        Book saved = productRepository.save(productMapper.mapToBookEntity(bookInsertDTO,
                findLeafCategory(bookInsertDTO.categoryId())));
        log.info("Book with sku={} created",saved.getSku());
        return productMapper.toBookDTO(saved);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public MusicReadOnlyDTO createMusic(MusicInsertDTO musicInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException, InvalidArgumentException {
        validateSkuIsFree(musicInsertDTO.sku());
        Music saved = productRepository.save(productMapper.mapToMusicEntity(musicInsertDTO,
                findLeafCategory(musicInsertDTO.categoryId())));
        log.info("Music with sku={} created", saved.getSku());
        return productMapper.toMusicDTO(saved);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public ToyReadOnlyDTO createToy(ToyInsertDTO toyInsertDTO) throws EntityAlreadyExistsException,
            EntityNotFoundException, InvalidArgumentException {
        validateSkuIsFree(toyInsertDTO.sku());
        Toy saved = productRepository.save(productMapper.mapToToyEntity(toyInsertDTO,
                findLeafCategory(toyInsertDTO.categoryId())));
        log.info("Toy with sku={} created" , saved.getSku());
        return productMapper.toToyDTO(saved);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public StationeryReadOnlyDTO createStationery(StationeryInsertDTO stationeryInsertDTO) throws EntityAlreadyExistsException, EntityNotFoundException, InvalidArgumentException {
        validateSkuIsFree(stationeryInsertDTO.sku());
        Stationery saved = productRepository.save(productMapper.mapToStationeryEntity(stationeryInsertDTO,
                findLeafCategory(stationeryInsertDTO.categoryId())));
        log.info("Stationery with sku={} created" ,saved.getSku());
        return productMapper.toStationeryDTO(saved);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public BookReadOnlyDTO updateBook(Long id, BookInsertDTO bookInsertDTO) throws EntityAlreadyExistsException, EntityNotFoundException, InvalidArgumentException {

        Book book = findProductOfType(id, Book.class);
        validateSkuOnUpdate(book,bookInsertDTO.sku());
        productMapper.updateBookFromDTO(book,bookInsertDTO,findLeafCategory(bookInsertDTO.categoryId()));
        log.info("Book with id={} updated" , id);
        return productMapper.toBookDTO(book);

    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public MusicReadOnlyDTO updateMusic(Long id, MusicInsertDTO musicInsertDTO) throws EntityAlreadyExistsException, EntityNotFoundException, InvalidArgumentException {

        Music music = findProductOfType(id, Music.class);
        validateSkuOnUpdate(music, musicInsertDTO.sku());
        productMapper.updateMusicFromDTO(music,musicInsertDTO,findLeafCategory(musicInsertDTO.categoryId()));
        log.info("Music with id={} updated", id);
        return productMapper.toMusicDTO(music);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public ToyReadOnlyDTO updateToy(Long id, ToyInsertDTO toyInsertDTO) throws EntityAlreadyExistsException, EntityNotFoundException, InvalidArgumentException {
        Toy toy = findProductOfType(id,Toy.class);
        validateSkuOnUpdate(toy,toyInsertDTO.sku());
        productMapper.updateToyFromDTO(toy,toyInsertDTO,findLeafCategory(toyInsertDTO.categoryId()));
        log.info("Toy with id={} updated" , id);
        return productMapper.toToyDTO(toy);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public StationeryReadOnlyDTO updateStationery(Long id, StationeryInsertDTO stationeryInsertDTO) throws EntityAlreadyExistsException, EntityNotFoundException, InvalidArgumentException {
        Stationery stationery = findProductOfType(id, Stationery.class);
        validateSkuOnUpdate(stationery,stationeryInsertDTO.sku());
        productMapper.updateStationeryFromDTO(stationery,stationeryInsertDTO,findLeafCategory(stationeryInsertDTO.categoryId()));
        log.info("Stationery with id={} updated" , id);
        return productMapper.toStationeryDTO(stationery);
    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void deleteProduct(Long id) throws EntityNotFoundException {
        Product product = findActiveProduct(id);
        product.softDelete();
        log.info("Product with id={} deleted", id);

    }

    @Override
    @Transactional(rollbackFor = AppGenericException.class)
    public void toggleFeatured(Long id) throws EntityNotFoundException {
        Product product = findActiveProduct(id);
        product.setFeatured(!product.isFeatured());
        log.info("Product with id={} featured ={}",id, product.isFeatured());

    }

    // Helpers

    private Product findActiveProduct(Long id) throws EntityNotFoundException{
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new EntityNotFoundException("PRODUCT", "Product with id: "
                        + id + " was not found"));
    }

    private void validateSkuIsFree(String sku) throws EntityAlreadyExistsException{
        if (productRepository.findBySkuAndDeletedFalse(sku).isPresent()){
            throw new EntityAlreadyExistsException("PRODUCT", "Product with sku: " + sku
                    + " already exists");

        }
    }

    private void validateSkuOnUpdate(Product product, String newSku)
            throws EntityAlreadyExistsException {
        if (!product.getSku().equals(newSku)) {
            validateSkuIsFree(newSku);
        }
    }

    private Category findLeafCategory(Long categoryId) throws EntityNotFoundException,InvalidArgumentException{
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new EntityNotFoundException("CATEGORY","Category with id: " + categoryId
                        + " not found"));
        if (!category.getSubCategories().isEmpty()){
            throw new InvalidArgumentException("CATEGORY", "Products must have parent category");
        }
        return category;

    }

    private <T extends Product> T findProductOfType(Long id, Class<T> type)
            throws EntityNotFoundException {

        Product product = findActiveProduct(id);
        if (!type.isInstance(product)) {
            throw new EntityNotFoundException("PRODUCT",
                    "Product with id: " + id + " is not a " + type.getSimpleName());
        }
        return type.cast(product);
    }
}
