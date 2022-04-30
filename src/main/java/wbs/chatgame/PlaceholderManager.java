package wbs.chatgame;

import org.bukkit.OfflinePlayer;
import wbs.chatgame.data.LeaderboardEntry;
import wbs.chatgame.data.StatsManager;
import wbs.chatgame.data.TrackedPeriod;
import wbs.chatgame.games.Game;
import wbs.chatgame.games.GameManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;

import java.util.List;

public class PlaceholderManager {
    private PlaceholderManager() {}

    // wbschatgame_top_[place]_[stat]
    // wbschatgame_top_<game>_[place]_[stat]
    // wbschatgame_top_<period>_[place]_[stat]
    // wbschatgame_top_<game>_<period>_[place]_[stat]
    private static final String TOP_KEY = "top";

    private static final String NOT_FOUND = "N/A";

    public static void registerPlaceholders() {
        PlaceholderAPIWrapper.registerSimplePlaceholder(WbsChatGame.getInstance(), "Weber588", PlaceholderManager::parseParams);
    }

    private static String parseParams(OfflinePlayer player, String params) {
        if (params.startsWith(TOP_KEY)) {
            String[] args = params.split("_");

            if (args.length == 1 || args[1].isEmpty()) {
                List<LeaderboardEntry> top = StatsManager.getCachedTop(TrackedPeriod.TOTAL);
                if (top.isEmpty()) {
                    return NOT_FOUND;
                } else {
                    return top.get(0).name();
                }
            }

            int nextIndex = 1;

            Game game = GameManager.getGame(args[nextIndex]);
            TrackedPeriod period = TrackedPeriod.TOTAL;

            if (game != null) {
                nextIndex++;
            }

            if (args.length > nextIndex) {
                period = WbsEnums.getEnumFromString(TrackedPeriod.class, args[nextIndex]);

                if (period == null) {
                    period = TrackedPeriod.TOTAL;
                } else {
                    nextIndex++;
                }
            }

            int place = 1;
            if (args.length > nextIndex) {
                try {
                    place = Integer.parseInt(args[nextIndex]);
                    nextIndex++;
                } catch (NumberFormatException e) {
                    return "[Invalid place number: " + args[nextIndex] + "]";
                }

                if (place <= 0) {
                    return "[Invalid place number: " + place + "]";
                }
            }

            // Convert to 0-index
            place--;

            List<LeaderboardEntry> top;
            if (game != null) {
                top = StatsManager.getCachedTop(period, game);
            } else {
                top = StatsManager.getCachedTop(period);
            }

            if (top.size() <= place) {
                return NOT_FOUND;
            }
            LeaderboardEntry entry = top.get(place);

            PlayerProperty property;
            if (args.length > nextIndex) {
                property = WbsEnums.getEnumFromString(PlayerProperty.class, args[nextIndex]);
                if (property == null) {
                    return "[Invalid property: " + args[nextIndex] + "]";
                }
            } else {
                property = PlayerProperty.NAME;
            }

            return property.getProperty(entry);
        }

        return null;
    }

    private enum PlayerProperty {
        NAME,
        POINTS
        ;

        public String getProperty(LeaderboardEntry entry) {
            return switch (this) {
                case NAME -> entry.name();
                case POINTS -> entry.points() + "";
            };
        }
    }
}
