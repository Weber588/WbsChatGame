package wbs.chatgame.games.trivia;

public record TriviaQuestion(String id,
                             String question,
                             int points,
                             boolean showAnswer,
                             boolean useRegex,
                             String ... answers) {
}
