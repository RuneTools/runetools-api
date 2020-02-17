package org.runetools.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runetools.api.test.TestUtil;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class Rs3WikiServiceTest {
    private static final JsonNode EMPTY_JSON = TestUtil.jsonResource("/org/runetools/api/service/Rs3WikiService/empty.json");
    private static final JsonNode VALID_JSON = TestUtil.jsonResource("/org/runetools/api/service/Rs3WikiService/valid.json");
    private static final JsonNode VALID_SWITCH_JSON = TestUtil.jsonResource("/org/runetools/api/service/Rs3WikiService/valid-switch.json");

    @Mock
    private RestTemplate restTemplate;

    private Rs3WikiService service;

    @BeforeEach
    public void setUp() {
        Mockito.doReturn(VALID_JSON).when(restTemplate).getForObject(Mockito.any(URI.class), Mockito.eq(JsonNode.class));

        service = new Rs3WikiService(restTemplate, new RetryTemplate());
        service.onApplicationReady();
    }

    @Test
    public void testGetVoiceOfSerenEmpty() {
        Mockito.doReturn(EMPTY_JSON).when(restTemplate).getForObject(Mockito.any(URI.class), Mockito.eq(JsonNode.class));
        Assertions.assertThrows(NoSuchElementException.class, service::onApplicationReady);
    }

    @Test
    public void testGetVoiceOfSeren() {
        var voiceOfSeren = service.getClans();
        Assertions.assertTrue(voiceOfSeren.isPresent());
    }

    @Test
    public void testGetVoiceOfSerenRetry() {
        Mockito.doReturn(VALID_JSON).doReturn(VALID_SWITCH_JSON)
                .when(restTemplate).getForObject(Mockito.any(URI.class), Mockito.eq(JsonNode.class));

        service.onApplicationReady(); // Trigger a refresh which should retry once to get the new voice.

        Mockito.verify(restTemplate, Mockito.times(3))
                .getForObject(Mockito.any(URI.class), Mockito.eq(JsonNode.class));
    }
}
