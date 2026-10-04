package com.neratzis.bookstore.mapper;

import com.neratzis.bookstore.dto.CategoryInsertDTO;
import com.neratzis.bookstore.dto.CategoryReadOnlyDTO;
import com.neratzis.bookstore.model.Category;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class CategoryMapper {

    public CategoryReadOnlyDTO toCategoryDTO(Category category){

        List<CategoryReadOnlyDTO> subCategories = category.getSubCategories()
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .map(this::toCategoryDTO)
                .toList();


        return new CategoryReadOnlyDTO(
                category.getId(),
                category.getName(),
                subCategories
        );
    }

    public Category mapToCategoryEntity(CategoryInsertDTO categoryInsertDTO, Category parent){
        Category category = new Category();

        category.setName(categoryInsertDTO.name());
        category.setParentCategory(parent);
        return category;

    }
}
