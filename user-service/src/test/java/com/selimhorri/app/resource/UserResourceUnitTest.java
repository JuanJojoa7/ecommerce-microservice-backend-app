package com.selimhorri.app.resource;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class UserResourceUnitTest {

    @Test
    void findAll_returnsOk() {
        UserService service = Mockito.mock(UserService.class);
        when(service.findAll()).thenReturn(List.of(UserDto.builder().userId(1).build()));
        UserResource resource = new UserResource(service);

        ResponseEntity<DtoCollectionResponse<UserDto>> resp = resource.findAll();

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCollection()).hasSize(1);
    }
}
