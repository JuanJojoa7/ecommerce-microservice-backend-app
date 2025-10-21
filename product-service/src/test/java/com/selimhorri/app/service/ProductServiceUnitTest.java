package com.selimhorri.app.service;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.helper.ProductMappingHelper;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

class ProductServiceUnitTest {

    @Test
    void findAll_mapsEntities() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
    when(repo.findAll()).thenReturn(List.of(
        ProductMappingHelper.map(
            ProductDto.builder()
                .productId(1)
                .productTitle("T")
                .categoryDto(CategoryDto.builder()
                    .categoryId(100)
                    .categoryTitle("C")
                    .build())
                .build()
        )
    ));
        ProductService service = new ProductServiceImpl(repo);
        List<ProductDto> res = service.findAll();
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getProductTitle()).isEqualTo("T");
    }

    @Test
    void findById_notFound_throws() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        when(repo.findById(99)).thenReturn(Optional.empty());
        ProductService service = new ProductServiceImpl(repo);
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(ProductNotFoundException.class);
    }
}
