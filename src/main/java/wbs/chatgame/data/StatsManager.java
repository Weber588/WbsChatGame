package wbs.chatgame.data;

import wbs.chatgame.WbsChatGame;
import wbs.utils.util.database.WbsDatabase;
import wbs.utils.util.database.WbsRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class StatsManager {

    private static final int topListSize = 25;

    private static final Map<GameStats.TrackedPeriod, List<PlayerRecord>> periodTops = new HashMap<>();

    public static boolean pointsUpdated = false;

    public static void recalculateAll() {
        for (GameStats.TrackedPeriod stat : GameStats.TrackedPeriod.values()) {
            recalculate(stat);
        }
    }

    public static List<PlayerRecord> recalculate(GameStats.TrackedPeriod period) {
        List<PlayerRecord> topList = new LinkedList<>();

        String query =
                "SELECT * " +
                "FROM " + ChatGameDB.statsTable.getName() + " AS s " +
                "JOIN " + ChatGameDB.playerTable.getName() + " AS p " +
                    "ON s." + ChatGameDB.uuidField.getFieldName() + " = p." + ChatGameDB.uuidField.getFieldName() + " " +
                "ORDER BY s." + period.field.getFieldName() + " ASC " +
                "LIMIT " + topListSize
                ;

        WbsDatabase db = ChatGameDB.getDatabase();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            List<WbsRecord> selected = db.select(statement);

            for (WbsRecord record : selected) {
                topList.add(new PlayerRecord(record));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        periodTops.put(period, topList);
        return topList;
    }

    public static List<PlayerRecord> getTop(GameStats.TrackedPeriod period) {
        List<PlayerRecord> top = periodTops.get(period);

        if (top != null && !pointsUpdated) {
            return top;
        }

        return recalculate(period);
    }

    public static int getTopAsync(GameStats.TrackedPeriod period, Consumer<List<PlayerRecord>> callback) {
        List<PlayerRecord> top = periodTops.get(period);

        if (top != null && !pointsUpdated) {
            callback.accept(top);
            return -1;
        }

        return WbsChatGame.getInstance().runAsync(
                () -> recalculate(period),
                () -> callback.accept(periodTops.get(period))
        );
    }
}

