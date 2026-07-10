package de.emc.mitglieder.reporting.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class YearBasedCalculationServiceTest {

    private final YearBasedCalculationService service =
            new YearBasedCalculationService();

    @Test
    void shouldCalculateYearsReachedInYear() {

        Integer result = service.calculateYearsReachedInYear(
                LocalDate.of(1980, 7, 12),
                2026
        );

        assertEquals(46, result);
    }

    @Test
    void shouldReturnNullWhenDateIsNull() {

        Integer result =
                service.calculateYearsReachedInYear(null, 2026);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTargetYearIsNull() {

        Integer result =
                service.calculateYearsReachedInYear(
                        LocalDate.of(1980, 1, 1),
                        null
                );

        assertNull(result);
    }

    @Test
    void shouldRecognizeFiveYearMilestone() {

        assertTrue(service.isFiveYearMilestoneFrom(60, 60));
        assertTrue(service.isFiveYearMilestoneFrom(65, 60));
        assertTrue(service.isFiveYearMilestoneFrom(70, 60));
    }

    @Test
    void shouldRejectNonMilestones() {

        assertFalse(service.isFiveYearMilestoneFrom(61, 60));
        assertFalse(service.isFiveYearMilestoneFrom(64, 60));
        assertFalse(service.isFiveYearMilestoneFrom(59, 60));
    }

    @Test
    void shouldRejectNullValue() {

        assertFalse(service.isFiveYearMilestoneFrom(null, 60));
    }
}
