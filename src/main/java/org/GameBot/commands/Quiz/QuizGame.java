package org.GameBot.commands.Quiz;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class QuizGame extends ListenerAdapter {

    private final ArrayList<QuizQuestion> questions = new ArrayList<>();
    private final String userId;
    private final TextChannel quizChannel;

    private int currentQuestionNumber;

    private final int numberOfQuestions;

    public int correctAnswers = 0;
    public QuizGame(String userId, TextChannel quizChannel, int numberOfQuestions) {
        this.userId = userId;
        this.quizChannel = quizChannel;
        this.currentQuestionNumber = 1;
        this.numberOfQuestions = numberOfQuestions;
        fetchData();

        quizChannel.sendMessageEmbeds(createMessage(this.currentQuestionNumber))
                .queue(message -> {
                    message.addReaction(Emoji.fromUnicode("U+0031 U+FE0F U+20E3")).queue();
                    message.addReaction(Emoji.fromUnicode("U+0032 U+FE0F U+20E3")).queue();
                    message.addReaction(Emoji.fromUnicode("U+0033 U+FE0F U+20E3")).queue();
                    message.addReaction(Emoji.fromUnicode("U+0034 U+FE0F U+20E3")).queue();
                });
        this.currentQuestionNumber++;



    }

    public QuizGame(String userId) {
        this.userId = userId;
        this.quizChannel = null;
        this.numberOfQuestions = 0;
    }


    private void fetchData() {
        try {
            URL url = new URL(String.format("https://opentdb.com/api.php?amount=%d&difficulty=easy&type=multiple", this.numberOfQuestions));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Getting the response code
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                StringBuilder inline = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                //Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }

                //Close the scanner
                scanner.close();

                //Using the JSON simple library parse the string into a json object
                JSONParser parse = new JSONParser();
                JSONObject data_obj = (JSONObject) parse.parse(inline.toString());

                //Get the required object from the above created object
                JSONArray data = (JSONArray) data_obj.get("results");

                //Get the required data using its key
                for (Object o: data) {
                    JSONObject dataItem = (JSONObject) o;
                    String question = Jsoup.parse((String) dataItem.get("question")).text();
                    String correctAnswer = Jsoup.parse((String) dataItem.get("correct_answer")).text();
                    JSONArray inCorrectAnswers = (JSONArray) dataItem.get("incorrect_answers");
                    this.questions.add(new QuizQuestion(question, sortAnswersRandomly(correctAnswer, inCorrectAnswers)));
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    private QuizAnswer[] sortAnswersRandomly(String correctAnswer, JSONArray incorrectAnswers) {
        QuizAnswer[] randomlyOrderedAnswers = new QuizAnswer[incorrectAnswers.size() + 1];
        int correctRandomNum = ThreadLocalRandom.current().nextInt(0, 4);
        randomlyOrderedAnswers[correctRandomNum] = new QuizAnswer(correctAnswer, true);

        for (int i = 0; i < incorrectAnswers.size(); i++) {
            int incorectRandomNum = ThreadLocalRandom.current().nextInt(0, 4);
            if (randomlyOrderedAnswers[incorectRandomNum] != null) {
                i--;
            } else {
                randomlyOrderedAnswers[incorectRandomNum] = new QuizAnswer(Jsoup.parse((String) incorrectAnswers.get(i)).text(), false);
            }
        }
        return randomlyOrderedAnswers;
    }
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getChannel().getIdLong() == this.quizChannel.getIdLong() && event.getMember().getId().equals(this.userId)) {
            boolean isCorrect = false;
            quizChannel.removeReactionById(event.getMessageIdLong(),Emoji.fromUnicode(event.getReaction().getEmoji().asUnicode().getAsCodepoints()), event.getUser()).queue();
            switch (event.getReaction().getEmoji().asUnicode().getAsCodepoints()) {
                case ("U+31U+fe0fU+20e3") -> // Answer 1
                        isCorrect = checkAnswer(1);
                case ("U+32U+fe0fU+20e3") -> // Answer 2
                        isCorrect = checkAnswer(2);
                case ("U+33U+fe0fU+20e3") -> // Answer 3
                        isCorrect = checkAnswer(3);
                case ("U+34U+fe0fU+20e3") -> // Answer 4
                        isCorrect = checkAnswer(4);
                default -> System.out.println("Not A Valid Emoji");
            }

            if (isCorrect) {
                correctAnswers++;
            }

            quizChannel.sendMessage(isCorrect ? "Correct!" : "Incorrect!").queue();

            if (this.currentQuestionNumber <= this.questions.size()) {
                quizChannel.sendMessageEmbeds(createMessage(this.currentQuestionNumber))
                        .queue(message -> {
                            message.addReaction(Emoji.fromUnicode("U+0031 U+FE0F U+20E3")).queue();
                            message.addReaction(Emoji.fromUnicode("U+0032 U+FE0F U+20E3")).queue();
                            message.addReaction(Emoji.fromUnicode("U+0033 U+FE0F U+20E3")).queue();
                            message.addReaction(Emoji.fromUnicode("U+0034 U+FE0F U+20E3")).queue();
                        });

            } else {
                event.getChannel().sendMessage("You scored " + correctAnswers + "/" + this.questions.size() + "\n To close this quiz, enter /close-quiz").queue();

            }
            this.currentQuestionNumber++;

        }
    }

    private boolean checkAnswer(int answerNumber) {
        QuizQuestion question = this.questions.get(this.currentQuestionNumber - 2);
        return question.answers()[answerNumber - 1].isCorrect();
    }

    private MessageEmbed createMessage(int questionNumber) {

        ArrayList<MessageEmbed.Field> answerFields = new ArrayList<>();
        int count = 1;
        for (QuizAnswer a: this.questions.get(this.currentQuestionNumber - 1).answers()) {
            answerFields.add(new MessageEmbed.Field(Integer.toString(count), a.answer(), false));
            count++;
        }

        return new MessageEmbed(
                null,
                "Question " + questionNumber,
                this.questions.get(this.currentQuestionNumber - 1).question(),
                EmbedType.AUTO_MODERATION,
                OffsetDateTime.now(),
                0,
                null,
                null,
                new MessageEmbed.AuthorInfo("Quiz Game", null, null, null),
                null,
                null,
                null,
                answerFields);

    }

    public TextChannel getQuizChannel() {
        return this.quizChannel;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() == this.getClass()) {
            QuizGame quizGame = (QuizGame) o;
            return quizGame.userId.equals(this.userId);
        }
        return false;
    }

}
