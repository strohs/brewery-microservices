package org.cjs.beerservice.mapper;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.*;

/**
 * Maps a java.sql.Timestamp to/from java.time.OffsetDateTime
 */
@Component
public class DateMapper {
    public OffsetDateTime asOffsetDateTime(Timestamp timestamp) {
        if (timestamp != null) {
            LocalDateTime ldt = timestamp.toLocalDateTime();
            ZoneOffset systemZoneOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
            return ldt.atOffset(systemZoneOffset);
        } else {
            return null;
        }
    }

    public Timestamp asTimestamp(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            Instant instant = offsetDateTime.toInstant();
            return Timestamp.from(instant);
        } else {
            return null;
        }
    }
}
