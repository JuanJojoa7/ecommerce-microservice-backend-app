package com.selimhorri.app.resource;

import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PaymentResourceUnitTest {

    @Test
    void findAll_returnsOk() {
        PaymentService service = Mockito.mock(PaymentService.class);
        when(service.findAll()).thenReturn(List.of(PaymentDto.builder().paymentId(1).build()));
        PaymentResource resource = new PaymentResource(service);

        ResponseEntity<DtoCollectionResponse<PaymentDto>> resp = resource.findAll();

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCollection()).hasSize(1);
    }
}
