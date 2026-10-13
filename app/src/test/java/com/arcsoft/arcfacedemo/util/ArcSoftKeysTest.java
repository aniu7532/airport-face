package com.arcsoft.arcfacedemo.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArcSoftKeysTest {

    @Test
    public void isComplete_requiresAllThreeNonEmpty() {
        assertTrue(ArcSoftKeys.isComplete("app", "sdk", "code"));
        assertFalse(ArcSoftKeys.isComplete(null, "sdk", "code"));
        assertFalse(ArcSoftKeys.isComplete("app", "  ", "code"));
        assertFalse(ArcSoftKeys.isComplete("app", "sdk", ""));
        assertFalse(ArcSoftKeys.isComplete("app", "sdk", null));
    }
}
