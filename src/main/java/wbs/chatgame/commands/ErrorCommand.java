package wbs.chatgame.commands;

import wbs.chatgame.WbsChatGame;
import wbs.utils.util.commands.WbsErrorsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

public class ErrorCommand extends WbsErrorsSubcommand {
    public ErrorCommand(WbsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected WbsSettings getSettings() {
        return WbsChatGame.getInstance().settings;
    }
}
