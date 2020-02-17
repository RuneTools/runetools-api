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

import java.util.stream.StreamSupport;

@ExtendWith(MockitoExtension.class)
class NemiForestServiceTest {
    private static final JsonNode NONE_VIABLE_JSON = TestUtil.jsonResource("/org/runetools/api/service/NemiForestService/none-viable.json");
    private static final JsonNode MALFORMED_JSON = TestUtil.jsonResource("/org/runetools/api/service/NemiForestService/malformed.json");
    private static final JsonNode VALID_JSON = TestUtil.jsonResource("/org/runetools/api/service/NemiForestService/valid.json");

    @Mock
    private RedditService redditService;

    private NemiForestService service;

    @BeforeEach
    public void setUp() {
        var valid = StreamSupport.stream(VALID_JSON.spliterator(), false);
        Mockito.lenient().doReturn(valid).when(redditService).getPosts("nemiforest", 5);
        service = new NemiForestService(redditService, new RetryTemplate());
        service.onApplicationReady();
    }

    @Test
    void testGetLocationNoneViable() {
        var noneViable = StreamSupport.stream(NONE_VIABLE_JSON.spliterator(), false);
        Mockito.doReturn(noneViable).when(redditService).getPosts("nemiforest", 5);
        service.onApplicationReady();

        var location = service.getLocation();
        Assertions.assertTrue(location.isEmpty());
    }

    @Test
    void testGetLocationMalformed() {
        var malformed = StreamSupport.stream(MALFORMED_JSON.spliterator(), false);
        Mockito.doReturn(malformed).when(redditService).getPosts("nemiforest", 5);
        service.onApplicationReady();

        var location = service.getLocation();
        Assertions.assertTrue(location.isEmpty());
    }

    @Test
    void testGetLocationSuccess() {
        var location = service.getLocation();
        Assertions.assertTrue(location.isPresent());
        Assertions.assertEquals("good", location.get().getAuthor());
    }
}
