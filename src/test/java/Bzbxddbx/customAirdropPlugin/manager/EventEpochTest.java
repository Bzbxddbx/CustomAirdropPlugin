package Bzbxddbx.customAirdropPlugin.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventEpochTest {

    @Test
    void generationIsCurrentUntilInvalidated() {
        EventEpoch epoch = new EventEpoch();
        int generation = epoch.next();
        assertTrue(epoch.isCurrent(generation));

        epoch.invalidate();
        assertFalse(epoch.isCurrent(generation));
    }

    @Test
    void newGenerationReplacesOld() {
        EventEpoch epoch = new EventEpoch();
        int first = epoch.next();
        epoch.invalidate();
        int second = epoch.next();

        assertFalse(epoch.isCurrent(first));
        assertTrue(epoch.isCurrent(second));
    }

    @Test
    void consecutiveStartsReplaceOldGeneration() {
        EventEpoch epoch = new EventEpoch();
        int first = epoch.next();
        int second = epoch.next();

        assertFalse(epoch.isCurrent(first));
        assertTrue(epoch.isCurrent(second));
    }
}