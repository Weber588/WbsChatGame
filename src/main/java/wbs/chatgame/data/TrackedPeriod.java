package wbs.chatgame.data;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsFieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

public enum TrackedPeriod {
    TOTAL,
    MONTHLY,
    WEEKLY;

    public final WbsField field;

    TrackedPeriod() {
        field = new WbsField(toString().toLowerCase(), WbsFieldType.INT, 0);
    }

    public boolean inSamePeriod(LocalDateTime date1, LocalDateTime date2) {
        return Objects.equals(getNextPeriod(date1), getNextPeriod(date2));
    }

    public boolean inCurrentPeriod(LocalDateTime compare) {
        return inSamePeriod(compare, LocalDateTime.now());
    }

    @Nullable
    public LocalDateTime getNextPeriodStart() {
        return getNextPeriod(LocalDateTime.now());
    }

    private LocalDateTime getNextPeriod(LocalDateTime datetime) {
        LocalDate date = datetime.toLocalDate();
        ChatGameSettings settings = WbsChatGame.getInstance().settings;
        return switch (this) {
            case TOTAL -> null;
            case MONTHLY -> date
                    .with(TemporalAdjusters.firstDayOfNextMonth())
                    .atTime(settings.resetTime);
            case WEEKLY -> date
                    .with(TemporalAdjusters.next(settings.resetDay))
                    .atTime(settings.resetTime);
        };
    }
}
