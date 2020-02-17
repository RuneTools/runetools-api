package org.runetools.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.runetools.api.util.JsonUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RedditService {
    private static final Object LOCK = new Object();

    private final String credentials;
    private final RestTemplate restTemplate;

    private String accessToken;
    private long accessTokenExpires = Long.MIN_VALUE;

    public RedditService(Environment env, RestTemplate restTemplate) {
        String rawCredentials = env.getProperty("REDDIT_CLIENT_ID") + ":" + env.getProperty("REDDIT_SECRET");

        this.credentials = Base64.getEncoder().encodeToString(rawCredentials.getBytes());
        this.restTemplate = restTemplate;
    }

    private String getOAuthToken() {
        synchronized (LOCK) {
            if (accessTokenExpires <= System.currentTimeMillis()) {
                refreshOAuthToken();
            }
        }

        return accessToken;
    }

    public Stream<JsonNode> getPosts(@NotNull String board, int limit) {
        var request = RequestEntity.get(URI.create("https://oauth.reddit.com/r/" + board + "?limit=" + limit))
                .header("Authorization", "Bearer " + getOAuthToken())
                .build();

        var response = restTemplate.exchange(request, JsonNode.class);
        if (!response.getStatusCode().is2xxSuccessful() || !response.hasBody()) {
            throw new RuntimeException("Failed to read post data from Reddit");
        }

        // We're looking to get the "data" object on each item in "data.children"
        return JsonUtils.path(response.getBody(), "data", "children")
                .map(node -> StreamSupport.stream(node.spliterator(), false).map(child -> child.get("data")))
                .orElseThrow(() -> new RuntimeException("Malformed or unexpected JSON from Reddit"));
    }

    private void refreshOAuthToken() {
        var request = RequestEntity.post(URI.create("https://www.reddit.com/api/v1/access_token"))
                .header("Authorization", "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials");

        var response = restTemplate.exchange(request, JsonNode.class);
        if (!response.getStatusCode().is2xxSuccessful() || !response.hasBody()) {
            throw new RuntimeException("Failed to fetch access token from Reddit");
        }

        var body = Objects.requireNonNull(response.getBody());
        var expiresInMillis = body.get("expires_in").asLong() * 1000L;

        accessToken = body.get("access_token").asText();
        // Because of 403s for the token expiring it's probably better to not trust the last minute or so.
        accessTokenExpires = System.currentTimeMillis() + expiresInMillis - Duration.ofMinutes(1).toMillis();
    }
}
