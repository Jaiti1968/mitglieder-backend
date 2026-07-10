package de.emc.mitglieder.reporting.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class YearBasedCalculationService {

    public Integer calculateYearsReachedInYear(
            LocalDate startDate,
            Integer targetYear
    ) {
        if (startDate == null || targetYear == null) {
            return null;
        }

        return targetYear - startDate.getYear();
    }

    public boolean isFiveYearMilestoneFrom(
            Integer value,
            int minimumValue
    ) {
        if (value == null) {
            return false;
        }

        return value >= minimumValue && value % 5 == 0;
    }
}