package wbs.chatgame.data;

import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsFieldType;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;

public enum TrackedPeriod {
    TOTAL,
    MONTHLY,
    WEEKLY;

    public final WbsField field;

    TrackedPeriod() {
        field = new WbsField(toString().toLowerCase(), WbsFieldType.INT, 0);
    }

    public boolean inSamePeriod(LocalDateTime date1, LocalDateTime date2) {
        return switch (this) {
            case TOTAL -> true;
            case MONTHLY -> date1.getMonth().equals(date2.getMonth()) && date1.getYear() == date2.getYear();
            case WEEKLY -> date1.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == date2.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        };
    }

    public boolean inCurrentPeriod(LocalDateTime compare) {
        return inSamePeriod(compare, LocalDateTime.now());
    }
}
