package com.selimhorri.app.resource;

import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.FavouriteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FavouriteResourceUnitTest {

    @Test
    void findAll_returnsOk() {
        FavouriteService service = Mockito.mock(FavouriteService.class);
    when(service.findAll()).thenReturn(List.of(FavouriteDto.builder().userId(1).productId(1).likeDate(java.time.LocalDateTime.now()).build()));
        FavouriteResource resource = new FavouriteResource(service);

        ResponseEntity<DtoCollectionResponse<FavouriteDto>> resp = resource.findAll();

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCollection()).hasSize(1);
    }
}
