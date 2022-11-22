package org.GameBot.commands.Guess;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.GameBot.commands.Quiz.QuizAnswer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GuessGame extends ListenerAdapter {
    private final String userId;
    private final TextChannel gameChannel;
    private final int numberOfGuesses;
    private int guessCount;
    private int randomNumber;
    private boolean completed = false;
    public GuessGame(String userId, TextChannel gameChannel, int numberOfGuesses) {
        this.userId = userId;
        this.gameChannel = gameChannel;
        this.numberOfGuesses = numberOfGuesses;
        this.guessCount = 0;
        this.randomNumber = ThreadLocalRandom.current().nextInt(0, 101);

        this.gameChannel.sendMessageEmbeds(createMessage()).queue();

    }

    public GuessGame(String userId) {
        this.userId =userId;
        this.gameChannel = null;
        this.numberOfGuesses = -1;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getMessage().getAuthor().isBot() && event.getMessage().getAuthor().getId().equals(this.userId) && !completed) {
            try {
                System.out.println(event.getMessage().getContentRaw());
                int guess = Integer.parseInt(event.getMessage().getContentRaw());
                if (guess == this.randomNumber) {
                    this.gameChannel.sendMessage(String.format("Correct! You got the number in %d tries.\nEnter /close-guess to close this game", this.guessCount)).queue();
                    this.completed = true;
                } else {
                    this.guessCount++;

                    if (this.guessCount == this.numberOfGuesses) {
                        this.completed = true;
                        this.gameChannel.sendMessage("Sorry, you're out of guesses. The number was " + this.randomNumber + ".\nEnter /close-guess to close this game").queue();
                    } else {
                        this.gameChannel.sendMessage("Incorrect! Try a " + (guess < this.randomNumber ? "higher" : "lower") + " number").queue();
                    }
                }

                if (!completed) {
                    this.gameChannel.sendMessageEmbeds(createMessage()).queue();
                }
            } catch (NumberFormatException e) {
                this.gameChannel.sendMessage("Not a valid number!").queue();
                this.gameChannel.sendMessageEmbeds(createMessage()).queue();
            }

        }
    }

    private MessageEmbed createMessage() {

        return new MessageEmbed(
                null,
                "Guesses Left: " + (this.numberOfGuesses - this.guessCount),
                "Guess a number between 1 and 100",
                EmbedType.AUTO_MODERATION,
                OffsetDateTime.now(),
                0,
                null,
                null,
                new MessageEmbed.AuthorInfo("Guess Game", null, null, null),
                null,
                null,
                null,
                null);

    }

    public TextChannel getGameChannel() {
        return this.gameChannel;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() == this.getClass()) {
            GuessGame guessGame = (GuessGame) o;
            return guessGame.userId.equals(this.userId);
        }
        return false;
    }

}
