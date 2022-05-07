package wbs.chatgame.data;

import org.jetbrains.annotations.Nullable;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsFieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public enum TrackedPeriod {
    TOTAL,
    MONTHLY,
    WEEKLY;

    public final WbsField pointsField;
    public final WbsField speedField;

    TrackedPeriod() {
        pointsField = new WbsField(toString().toLowerCase(), WbsFieldType.INT, 0);
        speedField = new WbsField(toString().toLowerCase() + "_speed", WbsFieldType.DOUBLE, Double.MAX_VALUE);
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
