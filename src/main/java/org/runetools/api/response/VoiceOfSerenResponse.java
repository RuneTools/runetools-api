package org.runetools.api.response;

import lombok.Data;
import org.runetools.api.enm.PrifddinasClan;

import java.util.List;

@Data
public class VoiceOfSerenResponse {
    private final List<PrifddinasClan> clans;
}
