package wbs.chatgame.games.word.generator;

import wbs.chatgame.LangUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class KeyedWordGenerator extends WordGenerator {

    @Override
    protected List<GeneratedWord> generateWords() {
        Map<String, String> lang = LangUtil.getLangConfig();

        List<GeneratedWord> words = new LinkedList<>();

        String prefix = getLangPrefix().toLowerCase() + ".";

        for (String key : lang.keySet()) {
            if (key.toLowerCase().startsWith(prefix)) {
                String postPrefix = key.substring(prefix.length());

                if (!postPrefix.contains(".")) { // Ignore child nodes; only get leaves
                    String word = lang.get(key);
                    words.add(new GeneratedWord(word, 0, this, true));
                }
            }
        }

        if (!words.isEmpty()) {
            return words;
        } else {
            return getDefault();
        }
    }

    protected abstract String getLangPrefix();
    protected abstract List<GeneratedWord> getDefault();
}
