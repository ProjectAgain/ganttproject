package net.sourceforge.ganttproject.model.time.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by IntelliJ IDEA.
 *
 * @author bard Date: 01.02.2004
 */
public class GregorianTimeStackTest {

    @Test
    public void testDayContains1440Minutes() throws Exception {
        assertTrue(
            GregorianTimeUnitStack.DAY
                .isConstructedFrom(GregorianTimeUnitStack.MINUTE),
            "Day isn't constructed from minutes :("
        );
        assertEquals(1440, GregorianTimeUnitStack.DAY
                         .getAtomCount(GregorianTimeUnitStack.MINUTE),
                     "Unexpected minutes count in one day"
        );
    }

    @Test
    public void testDayContains1Day() throws Exception {
        assertTrue(
            GregorianTimeUnitStack.DAY
                .isConstructedFrom(GregorianTimeUnitStack.DAY),
            "Day isn't constructed from days :("
        );
        assertEquals(1, GregorianTimeUnitStack.DAY
                         .getAtomCount(GregorianTimeUnitStack.DAY),
                     "Unexpected days count in one day"
        );
    }

    @Test
    public void testDayContains24Hours() throws Exception {
        assertTrue(
            GregorianTimeUnitStack.DAY
                .isConstructedFrom(GregorianTimeUnitStack.HOUR),
            "Day isn't constructed from hours :("
        );
        assertEquals(24, GregorianTimeUnitStack.DAY
                         .getAtomCount(GregorianTimeUnitStack.HOUR),
                     "Unexpected hours count in one day"
        );
    }

    @Test
    public void testDayContains86400Seconds() throws Exception {
        assertTrue(
            GregorianTimeUnitStack.DAY
                .isConstructedFrom(GregorianTimeUnitStack.SECOND),
            "Day isn't constructed from seconds :("
        );
        assertEquals(86400, GregorianTimeUnitStack.DAY
                         .getAtomCount(GregorianTimeUnitStack.SECOND),
                     "Unexpected minutes count in one day"
        );
    }
}
