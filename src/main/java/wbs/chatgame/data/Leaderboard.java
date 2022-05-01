package wbs.chatgame.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.chatgame.games.Game;

import java.util.*;

public class Leaderboard implements Iterable<LeaderboardEntry> {
    @NotNull
    private final TrackedPeriod period;
    @Nullable
    private final Game game;
    private final List<LeaderboardEntry> entries = new ArrayList<>();

    // The max position to store in the leaderboard, which will likely be reflected by the length.
    // However, there may be more if there are shared positions due to ties
    public int maxPosition = StatsManager.topListSize;

    public Leaderboard(List<LeaderboardEntry> entries, @NotNull TrackedPeriod period, @Nullable Game game) {
        this(period, game);
        this.entries.addAll(entries);
    }

    public Leaderboard(@NotNull TrackedPeriod period, @Nullable Game game) {
        this.period = period;
        this.game = game;
    }

    public Leaderboard(@NotNull TrackedPeriod period) {
        this(period, null);
    }

    @NotNull
    public TrackedPeriod getPeriod() {
        return period;
    }

    @Nullable
    public Game getGame() {
        return game;
    }

    public void setMaxPosition(int maxPosition) {
        this.maxPosition = maxPosition;
    }

    public void add(LeaderboardEntry entry) {
        entries.add(entry);
        sort();

    }

    public void sort() {
        entries.sort(Comparator.comparing(LeaderboardEntry::points).reversed());

        int position = -1;
        int sharedPosition = 1;
        int currentPoints = Integer.MIN_VALUE;
        for (LeaderboardEntry entry : entries) {
            if (entry.points() != currentPoints) {
                position += sharedPosition;
                sharedPosition = 1;
            } else {
                sharedPosition++;
            }

            entry.setPosition(position);
            currentPoints = entry.points();
        }

        if (position > maxPosition) {
            entries.removeIf(entry -> entry.getPosition() > maxPosition);
        }
    }

    /**
     * Update this leaderboard with the given record, adding or updating the entries.
     * @param record The record to update the leaderboard ordering/contents for
     */
    public void update(PlayerRecord record) {
        boolean updated = false;

        int recordPoints;
        if (game != null) {
            recordPoints = record.getPoints(game, period);
        } else {
            recordPoints = record.getPoints(period);
        }

        if (entries.isEmpty()) {
            LeaderboardEntry entry = new LeaderboardEntry(record.getUUID(), record.getName(), recordPoints, period, game);

            entry.setPosition(0);

            add(entry);
        } else {
            LeaderboardEntry minEntry = entries.get(entries.size() - 1);

            for (LeaderboardEntry entry : entries) {
                if (entry.points() < minEntry.points()) {
                    minEntry = entry;
                }

                if (entry.uuid().equals(record.getUUID())) {
                    entry.setPoints(recordPoints);

                    // Already in leaderboard; break and just update the order.
                    updated = true;
                    break;
                }
            }

            if (updated) {
                sort();
            } else {
                if (recordPoints >= minEntry.points()) {
                    LeaderboardEntry entry = new LeaderboardEntry(record.getUUID(), record.getName(), recordPoints, period, game);

                    if (recordPoints == minEntry.points()) {
                        entry.setPosition(minEntry.getPosition());
                    } else {
                        entry.setPosition(minEntry.getPosition() + 1);
                    }

                    add(entry);
                }
            }
        }
    }

    // Delegated methods

    @NotNull
    @Override
    public Iterator<LeaderboardEntry> iterator() {
        return entries.iterator();
    }

    public int size() {
        return entries.size();
    }

    public LeaderboardEntry get(int position) {
        return entries.get(position);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
