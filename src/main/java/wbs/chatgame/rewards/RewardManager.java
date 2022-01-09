package wbs.chatgame.rewards;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.chatgame.ChatGameSettings;
import wbs.chatgame.WbsChatGame;
import wbs.chatgame.data.PlayerRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class RewardManager {
    private RewardManager() {}

    private static WbsChatGame plugin;
    private static ChatGameSettings settings;
    private static Logger logger;

    public static Map<String, Reward> rewards = new HashMap<>();
    public static String rewardMessage = "Configure this message in &hrewards.yml";

    public static boolean doRewards = false;

    public static void reloadRewards(YamlConfiguration rewardsConfig) {
        plugin = WbsChatGame.getInstance();
        settings = plugin.settings;
        logger = plugin.logger;

        String directory = "rewards.yml";

        rewardMessage = "Configure this message in &h" + directory;
        doRewards = false;
        rewards.clear();

        if (rewardsConfig.isBoolean("enable-rewards")) {
            doRewards = rewardsConfig.getBoolean("enable-rewards", doRewards);
            if (doRewards) {
                logger.info("Rewards are enabled!");
            } else {
                logger.info("Rewards disabled in " + directory + ".");
            }
        } else {
            logger.info("\"enable-rewards\" missing in " + directory + ". Rewards have been disabled.");
        }

        if (!rewardsConfig.isList("message")) {
            logger.info("No message configured in " + directory + "; rewards subcommand disabled!");
        } else {
            String message = String.join("\n&r", rewardsConfig.getStringList("message"));
            if (!message.isBlank()) {
                rewardMessage = message;
            } else {
                logger.info("Message empty in " + directory + "; rewards subcommand disabled!");
            }
        }

        directory += "/rewards";

        ConfigurationSection rewardsSection = rewardsConfig.getConfigurationSection("rewards");
        if (rewardsSection == null) {
            settings.logError("Rewards section missing.", directory);
            return;
        }

        ConfigurationSection random = rewardsSection.getConfigurationSection("random");
        if (random != null) {
            loadRandomRewards(random, directory + "/random");
        } else {
            settings.logError("No random rewards defined; skipping.", directory + "/random");
        }

        ConfigurationSection recurring = rewardsSection.getConfigurationSection("recurring");
        if (recurring != null) {
            loadRecurringRewards(recurring, directory + "/recurring");
        } else {
            settings.logError("No recurring rewards defined; skipping.", directory + "/recurring");
        }

        ConfigurationSection milestone = rewardsSection.getConfigurationSection("milestone");
        if (milestone != null) {
            loadMilestoneRewards(milestone, directory + "/milestone");
        } else {
            settings.logError("No milestone rewards defined; skipping.", directory + "/milestone");
        }
    }

    private static void loadRandomRewards(ConfigurationSection random, String directory) {
        for (String key : random.getKeys(false)) {
            String sectionDir = directory + "/" + key;

            ConfigurationSection rewardSection = random.getConfigurationSection(key);
            if (rewardSection == null) {
                settings.logError("Value must be a section: " + key, sectionDir);
                continue;
            }

            double chance = rewardSection.getDouble("chance");
            int points = rewardSection.getInt("points-needed");
            double money = rewardSection.getDouble("money");
            String message = rewardSection.getString("message");
            List<String> commands = rewardSection.getStringList("commands");
            String broadcast = rewardSection.getString("broadcast");

            Reward reward = new Reward(Reward.RewardType.RANDOM, chance, points, money, message, commands, broadcast);
            rewards.put(key, reward);
        }
    }

    private static void loadRecurringRewards(ConfigurationSection recurring, String directory) {
        for (String key : recurring.getKeys(false)) {
            String sectionDir = directory + "/" + key;

            ConfigurationSection rewardSection = recurring.getConfigurationSection(key);
            if (rewardSection == null) {
                settings.logError("Value must be a section: " + key, sectionDir);
                continue;
            }

            int points = rewardSection.getInt("points-needed");
            if (points == 0) {
                settings.logError("A recurring reward was incorrectly configured; points-needed is a required field.", sectionDir + "/points-needed");
                continue;
            }
            double money = rewardSection.getDouble("money");
            String message = rewardSection.getString("message");
            List<String> commands = rewardSection.getStringList("commands");
            String broadcast = rewardSection.getString("broadcast");

            Reward reward = new Reward(Reward.RewardType.RECURRING, 0, points, money, message, commands, broadcast);
            rewards.put(key, reward);
        }
    }

    private static void loadMilestoneRewards(ConfigurationSection milestone, String directory) {
        for (String key : milestone.getKeys(false)) {
            String sectionDir = directory + "/" + key;

            ConfigurationSection rewardSection = milestone.getConfigurationSection(key);
            if (rewardSection == null) {
                settings.logError("Value must be a section: " + key, sectionDir);
                continue;
            }

            int points = rewardSection.getInt("points-needed");
            if (points == 0) {
                settings.logError("A milestone reward was incorrectly configured; points-needed is a required field.", sectionDir + "/points-needed");
                continue;
            }
            double money = rewardSection.getDouble("money");
            String message = rewardSection.getString("message");
            List<String> commands = rewardSection.getStringList("commands");
            String broadcast = rewardSection.getString("broadcast");

            Reward reward = new Reward(Reward.RewardType.MILESTONE, 0, points, money, message, commands, broadcast);
            rewards.put(key, reward);
        }
    }

    public static void giveRewards(PlayerRecord record, int points) {
        for (Reward reward : rewards.values()) {
            reward.run(record, points);
        }
    }
}
