package com.selimhorri.app.resource;

import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OrderItemResourceUnitTest {

    @Test
    void findAll_returnsOk() {
        OrderItemService service = Mockito.mock(OrderItemService.class);
    when(service.findAll()).thenReturn(List.of(OrderItemDto.builder().orderId(1).productId(1).orderedQuantity(1).build()));
        OrderItemResource resource = new OrderItemResource(service);

        ResponseEntity<DtoCollectionResponse<OrderItemDto>> resp = resource.findAll();

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCollection()).hasSize(1);
    }
}
