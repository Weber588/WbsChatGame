package wbs.chatgame.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.chatgame.games.word.generator.GeneratedWord;
import wbs.chatgame.games.word.generator.GeneratorManager;
import wbs.chatgame.games.word.generator.WordGenerator;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public class GeneratorCommand extends WbsSubcommand {
    public GeneratorCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "generator");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        if (args.length <= 1) {
            sendMessage("Usage: /cg generator <generator>", sender);
            return true;
        }

        WordGenerator generator = GeneratorManager.getGenerator(args[1]);
        if (generator == null) {
            sendMessage("Invalid generator", sender);
            return true;
        }

        StringBuilder words = new StringBuilder("&r");

        List<GeneratedWord> wordList = new LinkedList<>(generator.getAll());
        for (int i = 0; i < wordList.size(); i += 2) {
            GeneratedWord word = wordList.get(i);
            words.append(word.word);
            if (i + 1 < wordList.size()) {
                GeneratedWord nextWord = wordList.get(i + 1);
                words.append(", &h").append(nextWord.word);

                if (i + 2 < wordList.size()) {
                    words.append(", &r");
                }
            }
        }

        sendMessage(WbsStrings.capitalize(args[1]) + " words:\n" + words, sender);

        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return GeneratorManager.getIds();
        }

        return new LinkedList<>();
    }
}
