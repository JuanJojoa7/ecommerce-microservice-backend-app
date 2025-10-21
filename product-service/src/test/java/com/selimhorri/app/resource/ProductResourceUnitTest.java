package com.selimhorri.app.resource;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ProductResourceUnitTest {

    @Test
    void findAll_returnsOk() {
        ProductService service = Mockito.mock(ProductService.class);
        when(service.findAll()).thenReturn(List.of(ProductDto.builder().productId(1).productTitle("P1").build()));
        ProductResource resource = new ProductResource(service);

        ResponseEntity<DtoCollectionResponse<ProductDto>> resp = resource.findAll();

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCollection()).hasSize(1);
    }
}
