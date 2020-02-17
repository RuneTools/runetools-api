package org.runetools.api.enm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum PrifddinasClan {
    AMLODD("Amlodd"),
    CADARN("Cadarn"),
    CRWYS("Crwys"),
    HEFIN("Hefin"),
    IORWERTH("Iorwerth"),
    ITHELL("Ithell"),
    MEILYR("Meilyr"),
    TRAHAEARN("Trahaearn");

    private static final Map<String, PrifddinasClan> DISPLAY_LOOKUP = new HashMap<>();

    static {
        for (var value : PrifddinasClan.values()) {
            DISPLAY_LOOKUP.put(value.getDisplay(), value);
        }
    }

    private final String display;

    public static Optional<PrifddinasClan> fromDisplay(String display) {
        return Optional.ofNullable(DISPLAY_LOOKUP.get(display));
    }
}
