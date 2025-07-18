package wbs.chatgame.games.word.generator;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.JukeboxSong;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SongWordGenerator extends RegistryWordGenerator<JukeboxSong> {
    @Override
    protected @Nullable String getLangPrefix() {
        return null;
    }

    @Override
    protected @NotNull RegistryKey<JukeboxSong> getRegistryKey() {
        return RegistryKey.JUKEBOX_SONG;
    }

    @Override
    protected @Nullable Component getHint(JukeboxSong jukeboxSong) {
        return Component.text("This word is a song from a music disc");
    }
}
