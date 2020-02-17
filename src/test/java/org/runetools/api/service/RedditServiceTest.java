package org.runetools.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runetools.api.test.TestUtil;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class RedditServiceTest {
    private static final JsonNode OAUTH_JSON = TestUtil.jsonResource("/org/runetools/api/service/RedditService/oauth-response.json");
    private static final JsonNode POSTS_JSON = TestUtil.jsonResource("/org/runetools/api/service/RedditService/posts-response.json");

    @Mock
    private Environment env;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<JsonNode> oauthResponse;

    @Mock
    private ResponseEntity<JsonNode> postsResponse;

    @Captor
    ArgumentCaptor<RequestEntity<String>> requestEntityCaptor;

    private RedditService service;

    @BeforeEach
    public void setUp() throws IOException {

        Mockito.doReturn("CLIENT_ID").when(env).getProperty("REDDIT_CLIENT_ID");
        Mockito.doReturn("SECRET").when(env).getProperty("REDDIT_SECRET");

        // Mark these mocks as lenient, in our failure cases the mocks won't be necessary but they are for the rest.
        Mockito.lenient().doReturn(true).when(oauthResponse).hasBody();
        Mockito.lenient().doReturn(OAUTH_JSON).when(oauthResponse).getBody();
        Mockito.lenient().doReturn(HttpStatus.OK).when(oauthResponse).getStatusCode();

        Mockito.lenient().doReturn(true).when(postsResponse).hasBody();
        Mockito.lenient().doReturn(POSTS_JSON).when(postsResponse).getBody();
        Mockito.lenient().doReturn(HttpStatus.OK).when(postsResponse).getStatusCode();

        Mockito.doReturn(oauthResponse)
                .doReturn(postsResponse)
                .when(restTemplate).exchange(Mockito.any(), Mockito.eq(JsonNode.class));

        service = new RedditService(env, restTemplate);
    }

    @Test
    public void testGetPostsSuccessful() {
        var posts = service.getPosts("all", 5);

        // Verify outbound calls
        Mockito.verify(restTemplate, Mockito.times(2))
                .exchange(requestEntityCaptor.capture(), Mockito.eq(JsonNode.class));
        var requestEntities = requestEntityCaptor.getAllValues();

        Assertions.assertLinesMatch(
                Collections.singletonList("Basic Q0xJRU5UX0lEOlNFQ1JFVA=="),
                requestEntities.get(0).getHeaders().get("Authorization"),
                "The basic auth header on the OAuth request is wrong"
        );

        Assertions.assertEquals("https://oauth.reddit.com/r/all?limit=5", requestEntities.get(1).getUrl().toString());
        Assertions.assertLinesMatch(
                Collections.singletonList("Bearer abc123"),
                requestEntities.get(1).getHeaders().get("Authorization"),
                "The bearer auth header on the posts request is wrong"
        );

        Assertions.assertEquals(1, posts.count());
    }

    @Test
    public void testGetPostsOAuthFailure() {
        Mockito.doReturn(HttpStatus.UNAUTHORIZED).when(oauthResponse).getStatusCode();
        Assertions.assertThrows(RuntimeException.class, () -> service.getPosts("all", 5));
    }

    @Test
    public void testGetPostsResponseFailure() {
        Mockito.doReturn(HttpStatus.UNAUTHORIZED).when(postsResponse).getStatusCode();
        Assertions.assertThrows(RuntimeException.class, () -> service.getPosts("all", 5));
    }

    @Test
    public void testGetPostsCaching() {
        // The first invocation should result in 2 restTemplate calls (oauth and listing posts)
        service.getPosts("all", 5);
        Mockito.verify(restTemplate, Mockito.times(2))
                .exchange(Mockito.any(), Mockito.eq(JsonNode.class));

        // The second invocation should result in one extra call with the oauth token cached
        service.getPosts("all", 5);
        Mockito.verify(restTemplate, Mockito.times(3))
                .exchange(Mockito.any(), Mockito.eq(JsonNode.class));
    }
}
