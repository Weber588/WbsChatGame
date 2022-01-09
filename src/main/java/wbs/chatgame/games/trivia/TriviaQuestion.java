package wbs.chatgame.games.trivia;

public record TriviaQuestion(String question, int points, boolean showAnswer, boolean useRegex, String ... answers) {
}
