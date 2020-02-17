package org.runetools.api.enm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PrifddinasClanTest {
    @Test
    void testFromDisplay() {
        Assertions.assertTrue(PrifddinasClan.fromDisplay("Amlodd").isPresent());
        Assertions.assertTrue(PrifddinasClan.fromDisplay("Cow").isEmpty());
    }
}
