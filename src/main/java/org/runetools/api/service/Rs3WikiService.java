package org.runetools.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.runetools.api.enm.PrifddinasClan;
import org.runetools.api.util.JsonUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@RequiredArgsConstructor
@Slf4j
public class Rs3WikiService {
    private final RestTemplate restTemplate;
    @Qualifier("voiceOfSeren")
    private final RetryTemplate retryTemplate;

    private List<PrifddinasClan> clans;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshVoiceOfSeren();
    }

    @Scheduled(cron = "0 0 * * * *")
    private void refreshVoiceOfSeren() {
        var oldHashCode = Objects.hashCode(clans);

        clans = retryTemplate.execute(context ->
                queryAllMessages("VoS")
                        .findFirst()
                        .map(node -> Stream.of(node.get("content").asText().split(","))
                                .flatMap(clanName -> PrifddinasClan.fromDisplay(clanName).stream())
                                .collect(Collectors.toList()))
                        .filter(clansFetched -> clansFetched.hashCode() != oldHashCode)
                        .orElseThrow()
        );

        log.info("Refreshed Voice of Seren - " + clans.toString());
    }

    public Optional<List<PrifddinasClan>> getClans() {
        return Optional.ofNullable(clans);
    }

    private Stream<JsonNode> queryAllMessages(String filter) {
        var uri = uriBuilder()
                .queryParam("action", "query")
                .queryParam("meta", "allmessages")
                .queryParam("amlang", "en-gb")
                .queryParam("ammessages", filter == null ? "*" : filter)
                .build().toUri();

        return Optional.ofNullable(restTemplate.getForObject(uri, JsonNode.class))
                .flatMap(body -> JsonUtils.path(body, "query", "allmessages"))
                .filter(node -> node.isArray() && node.get(0).findValue("missing") == null)
                .map(node -> StreamSupport.stream(node.spliterator(), false))
                .orElseGet(Stream::empty);
    }

    private UriComponentsBuilder uriBuilder() {
        return UriComponentsBuilder.fromUriString("https://runescape.wiki/api.php")
                .queryParam("format", "json")
                .queryParam("formatversion", 2);
    }
}
