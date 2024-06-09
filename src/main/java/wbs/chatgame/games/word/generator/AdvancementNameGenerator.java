package wbs.chatgame.games.word.generator;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.StreamSupport;

public class AdvancementNameGenerator extends SimpleWordGenerator {
    private List<String> ignoreNamespaces = new LinkedList<>();

    @Override
    protected List<String> generateStrings() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        Bukkit.advancementIterator(), Spliterator.ORDERED),
                false)
                .filter(advancement -> !ignoreNamespaces.contains(advancement.getKey().getKey()))
                .map(Advancement::getDisplay)
                .filter(Objects::nonNull)
                .map(AdvancementDisplay::getTitle)
                .toList();
    }

    @Override
    public void configure(ConfigurationSection genSection) {
        this.ignoreNamespaces = genSection.getStringList("ignore-namespace");

        super.configure(genSection);
    }
}
