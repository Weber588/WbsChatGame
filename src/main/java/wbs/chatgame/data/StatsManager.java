package wbs.chatgame.data;

import org.jetbrains.annotations.NotNull;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.database.WbsDatabase;
import wbs.utils.util.database.WbsRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class StatsManager {

    public static final String TOTAL_POINTS_NAME = "total_points";

    private static final int topListSize = 25;

    private static final Map<GameStats.TrackedPeriod, List<LeaderboardEntry>> periodTops = new HashMap<>();
    private static final Map<Game, Map<GameStats.TrackedPeriod, List<LeaderboardEntry>>> gameTops = new HashMap<>();

    public static boolean pointsUpdated = false;

    @NotNull
    public List<LeaderboardEntry> getCachedTop(GameStats.TrackedPeriod stat) {
        List<LeaderboardEntry> top = periodTops.get(stat);
        if (top == null) {
            top = new LinkedList<>();
        }
        return top;
    }

    @NotNull
    public List<LeaderboardEntry> getCachedTop(Game game) {
        return getCachedTop(GameStats.TrackedPeriod.TOTAL, game);
    }

    @NotNull
    public List<LeaderboardEntry> getCachedTop(GameStats.TrackedPeriod stat, Game game) {
        Map<GameStats.TrackedPeriod, List<LeaderboardEntry>> gamePeriods = gameTops.get(game);
        if (gamePeriods == null) {
            gamePeriods = new HashMap<>();
        }

        List<LeaderboardEntry> top = gamePeriods.get(stat);
        if (top == null) {
            top = new LinkedList<>();
        }
        return top;
    }

    public static void recalculateAll() {
        for (GameStats.TrackedPeriod stat : GameStats.TrackedPeriod.values()) {
            recalculate(stat);
            for (Game game : GameManager.getGames()) {
                recalculate(stat, game);
            }
        }
    }

    public static List<LeaderboardEntry> recalculate(GameStats.TrackedPeriod period) {
        List<LeaderboardEntry> topList = new LinkedList<>();

        String query =
                "SELECT " +
                        "p." + ChatGameDB.uuidField.getFieldName() + ", " +
                        "p." + ChatGameDB.nameField.getFieldName() + ", " +
                        "SUM(s." + period.field.getFieldName() + ") AS " + TOTAL_POINTS_NAME + " " +
                "FROM " + ChatGameDB.statsTable.getName() + " AS s " +
                "JOIN " + ChatGameDB.playerTable.getName() + " AS p " +
                    "ON s." + ChatGameDB.uuidField.getFieldName() + " = p." + ChatGameDB.uuidField.getFieldName() + " " +

                "GROUP BY s." + ChatGameDB.uuidField.getFieldName() + " " +
                "ORDER BY " + TOTAL_POINTS_NAME + " DESC " +
                "LIMIT " + topListSize
                ;

        WbsDatabase db = ChatGameDB.getDatabase();
        List<WbsRecord> selected;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            selected = db.select(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            return topList;
        }

        for (WbsRecord record : selected) {
            LeaderboardEntry entry = new LeaderboardEntry(record, period);
            topList.add(entry);
        }

        periodTops.put(period, topList);
        return topList;
    }

    public static List<LeaderboardEntry> recalculate(@NotNull GameStats.TrackedPeriod period, @NotNull Game game) {
        List<LeaderboardEntry> topList = new LinkedList<>();

        String query =
                "SELECT " +
                        "p." + ChatGameDB.uuidField.getFieldName() + ", " +
                        "p." + ChatGameDB.nameField.getFieldName() + ", " +
                        "s." + period.field.getFieldName() + " AS " + TOTAL_POINTS_NAME + " " +
                "FROM " + ChatGameDB.statsTable.getName() + " AS s " +
                "JOIN " + ChatGameDB.playerTable.getName() + " AS p " +
                        "ON s." + ChatGameDB.uuidField.getFieldName() + " = p." + ChatGameDB.uuidField.getFieldName() + " " +

                "WHERE s." + ChatGameDB.gameField.getFieldName() + " = ? " +
                "ORDER BY " + TOTAL_POINTS_NAME + " DESC " +
                "LIMIT " + topListSize
                ;

        WbsDatabase db = ChatGameDB.getDatabase();
        List<WbsRecord> selected;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, game.getGameName());
            selected = db.select(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            return topList;
        }

        for (WbsRecord record : selected) {
            LeaderboardEntry entry = new LeaderboardEntry(record, period);
            topList.add(entry);
        }

        Map<GameStats.TrackedPeriod, List<LeaderboardEntry>> gamePeriods = gameTops.get(game);
        if (gamePeriods == null) {
            gamePeriods = new HashMap<>();
        }
        gamePeriods.put(period, topList);
        gameTops.put(game, gamePeriods);
        return topList;
    }

    public static List<LeaderboardEntry> getTop(GameStats.TrackedPeriod period) {
        List<LeaderboardEntry> top = periodTops.get(period);

        if (top != null && !pointsUpdated) {
            return top;
        }

        return recalculate(period);
    }

    public static int getTopAsync(GameStats.TrackedPeriod period, Consumer<List<LeaderboardEntry>> callback) {
        List<LeaderboardEntry> top = periodTops.get(period);

        if (top != null && !pointsUpdated) {
            callback.accept(top);
            return -1;
        }

        return WbsChatGame.getInstance().runAsync(
                () -> recalculate(period),
                () -> callback.accept(periodTops.get(period))
        );
    }

    public static List<LeaderboardEntry> getTop(Game game) {
        return getTop(GameStats.TrackedPeriod.TOTAL, game);
    }

    public static List<LeaderboardEntry> getTop(GameStats.TrackedPeriod period, Game game) {
        Map<GameStats.TrackedPeriod, List<LeaderboardEntry>> gamePeriods = gameTops.get(game);

        if (gamePeriods != null) {
            List<LeaderboardEntry> top = gamePeriods.get(period);

            if (top != null && !pointsUpdated) {
                return top;
            }
        }

        return recalculate(period);
    }

    public static int getTopAsync(Game game, Consumer<List<LeaderboardEntry>> callback) {
        return getTopAsync(GameStats.TrackedPeriod.TOTAL, game, callback);
    }

    public static int getTopAsync(GameStats.TrackedPeriod period, Game game, Consumer<List<LeaderboardEntry>> callback) {
        Map<GameStats.TrackedPeriod, List<LeaderboardEntry>> gamePeriods = gameTops.get(game);

        if (gamePeriods != null) {
            List<LeaderboardEntry> top = gamePeriods.get(period);

            if (top != null && !pointsUpdated) {
                callback.accept(top);
                return -1;
            }
        }

        return WbsChatGame.getInstance().runAsync(
                () -> recalculate(period, game),
                () -> callback.accept(getTop(period, game))
        );
    }
}

