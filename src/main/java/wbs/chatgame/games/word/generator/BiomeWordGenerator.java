package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BiomeWordGenerator extends RegistryWordGenerator<Biome> {
    @Override
    protected @Nullable String getLangPrefix() {
        return "biome";
    }

    @Override
    protected @NotNull RegistryKey<Biome> getRegistryKey() {
        return RegistryKey.BIOME;
    }

    @Override
    protected @Nullable Component getHint(Biome biome) {
        if (!biome.key().namespace().equals(NamespacedKey.MINECRAFT_NAMESPACE)) {
            return Component.text("This is a custom biome");
        }

        return Component.text("This word is a type of biome");
    }
}
