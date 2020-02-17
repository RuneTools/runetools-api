package org.runetools.api.controller;

import lombok.RequiredArgsConstructor;
import org.runetools.api.response.NemiForestLocation;
import org.runetools.api.response.VoiceOfSerenResponse;
import org.runetools.api.service.NemiForestService;
import org.runetools.api.service.Rs3WikiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RepeatableEventController {
    private final NemiForestService nemiForest;
    private final Rs3WikiService rs3Wiki;

    @GetMapping(path = "/nemi-forest")
    public ResponseEntity<NemiForestLocation> getNemiForest() {
        return nemiForest.getLocation().map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/voice-of-seren")
    public ResponseEntity<VoiceOfSerenResponse> getVoiceOfSeren() {
        return rs3Wiki.getClans().map(VoiceOfSerenResponse::new).map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
