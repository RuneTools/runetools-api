package org.runetools.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.runetools.api.response.NemiForestLocation;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <p>
 * A service designed to capture the most recent Nemi Forest location. This works by scanning the most recent five
 * /r/NemiForest posts and parsing out specific details. There's a ton of extra properties but an example of the details
 * we are looking for is as such:
 *
 * <pre>{@code
 * {
 *     "author": "me",
 *     "created_utc": 1234567890,
 *     "link_flair_text": "Depleted",
 *     "stickied": false,
 *     "title": "W50 9/9",
 *     "url": "https://my.url/to-some-image.png"
 * }
 * }</pre>
 *
 * <p>
 * We have one simple method of fetching the most recent location and return an <code>Optional</code> indicating if it
 * was found or not. Service failures will also be represented as a not found scenario to help reduce API calls our
 * service is allowed to make to Reddit.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@RequiredArgsConstructor
@Slf4j
public class NemiForestService {
    private static final Pattern WORLD_PATTERN = Pattern.compile("^W(\\d+)");

    private final RedditService redditService;
    private final RetryTemplate retryTemplate;

    private NemiForestLocation location;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshLocation();
    }

    @Scheduled(cron = "0 */5 * * * *")
    private void refreshLocation() {
        try {
            location = retryTemplate.execute(context -> redditService.getPosts("nemiforest", 5))
                    .flatMap(this::flattenPostToLocation)
                    .findFirst()
                    .orElseThrow();

            // Log the world and return so error conditions are free to continue processing.
            log.info("Nemi Forest location is world " + location.getWorld());
            return;
        } catch (NoSuchElementException ignored) {
            log.info("Nemi Forest location is currently depleted");
        } catch (Exception exception) {
            log.warn("Failed to refresh the Nemi Forest location", exception);
        }

        location = null;
    }

    public Optional<NemiForestLocation> getLocation() {
        return Optional.ofNullable(location);
    }

    private Stream<NemiForestLocation> flattenPostToLocation(JsonNode post) {
        // Stickied posts should be ignored and depleted worlds will have link flair.
        if (post.get("stickied").booleanValue() || !post.findValue("link_flair_text").isNull()) {
            return Stream.empty();
        }

        // Parse out the world number from the title, which must be present.
        var worldMatcher = WORLD_PATTERN.matcher(post.get("title").asText());
        if (!worldMatcher.find()) {
            return Stream.empty();
        }

        var postedAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(post.get("created_utc").asLong()),
                ZoneId.of("UTC")
        );

        return Stream.of(NemiForestLocation.builder()
                .author(post.get("author").asText())
                .mapUrl(post.get("url").asText())
                .postedAt(postedAt)
                .world(Integer.parseInt(worldMatcher.group(1)))
                .build());
    }
}
