package org.runetools.api.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runetools.api.enm.PrifddinasClan;
import org.runetools.api.response.NemiForestLocation;
import org.runetools.api.service.NemiForestService;
import org.runetools.api.service.Rs3WikiService;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RepeatableEventControllerTest {
    @Mock
    private NemiForestService nemiForest;

    @Mock
    private Rs3WikiService rs3Wiki;

    private RepeatableEventController controller;

    @BeforeEach
    public void setUp() {
        controller = new RepeatableEventController(nemiForest, rs3Wiki);
    }

    @Test
    public void testGetNemiForestOk() {
        var entity = NemiForestLocation.builder()
                .author("me")
                .mapUrl("http://my.url")
                .postedAt(LocalDateTime.now())
                .world(420)
                .build();

        Mockito.doReturn(Optional.of(entity)).when(nemiForest).getLocation();

        var response = controller.getNemiForest();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(entity, response.getBody());
    }

    @Test
    public void testGetNemiForestNotFound() {
        Mockito.doReturn(Optional.empty()).when(nemiForest).getLocation();

        var response = controller.getNemiForest();
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetVoiceOfSerenOk() {
        var voices = List.of(PrifddinasClan.AMLODD, PrifddinasClan.CADARN);
        Mockito.doReturn(Optional.of(voices)).when(rs3Wiki).getClans();

        var response = controller.getVoiceOfSeren();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(voices, response.getBody().getClans());
    }

    @Test
    public void testGetVoiceOfSerenNotFound() {
        Mockito.doReturn(Optional.empty()).when(rs3Wiki).getClans();

        var response = controller.getVoiceOfSeren();
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
