package wbs.chatgame.games.word.generator;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SimpleWordGenerator extends WordGenerator {
    @Override
    protected final List<GeneratedWord> generateWords() {
        return generateStrings().stream()
                .map(string -> new GeneratedWord(string, this, null))
                .collect(Collectors.toList());
    }


    protected abstract List<String> generateStrings();
}
