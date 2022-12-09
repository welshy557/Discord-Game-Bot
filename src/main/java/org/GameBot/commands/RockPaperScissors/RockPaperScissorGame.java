package org.GameBot.commands.RockPaperScissors;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.OffsetDateTime;
import java.util.ArrayList;

public class RockPaperScissorGame extends ListenerAdapter {
    private static final String ROCK = "rock";
    private static final String PAPER = "paper";
    private static final String SCISSORS = "scissors";
    private final User user;
    private final User opponent;
    private String usersSelection;
    private String opponentsSelection;
    private final TextChannel gameChannel;

    public RockPaperScissorGame(User user, User opponent, TextChannel gameChannel) {
        this.user = user;
        this.gameChannel = gameChannel;
        this.opponent = opponent;
        sendGameMessage();
    }

    public RockPaperScissorGame(User user, User opponent) {
        this.user = user;
        this.opponent = opponent;
        this.gameChannel = null;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getChannel().getIdLong() != this.gameChannel.getIdLong()) return;

        if (event.getButton().getId().equals("yes")) {
            this.opponentsSelection = null;
            this.usersSelection = null;
            event.reply("Started new game").setEphemeral(true).queue();
            sendGameMessage();
        } else if (event.getUser().getIdLong() == this.user.getIdLong()) {
            if (this.usersSelection != null) {
                event.reply("You already selected your move").setEphemeral(true).queue();
            } else {
                this.usersSelection = event.getButton().getId();
                event.reply("Selection Made").setEphemeral(true).queue();
            }
        } else if (event.getUser().getIdLong() == this.opponent.getIdLong()) {
            if (this.opponentsSelection != null) {
                event.reply("You already selected your move").setEphemeral(true).queue();
            } else {
                this.opponentsSelection = event.getButton().getId();
                event.reply("Selection Made").setEphemeral(true).queue();
            }
        }

        if (this.usersSelection != null && this.opponentsSelection != null) {
            try {
                Thread.sleep(500);
                this.gameChannel.sendMessage("ROCK!").queue();
                Thread.sleep(500);
                this.gameChannel.sendMessage("PAPER!").queue();
                Thread.sleep(500);
                this.gameChannel.sendMessage("SCISSORS!").queue();
                Thread.sleep(500);
                this.gameChannel.sendMessage("SHOOT!").queue();

                sendPlayAgainMessage();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendPlayAgainMessage() {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("yes", "Yes"));
        buttons.add(Button.danger("no", "No"));

        ArrayList<MessageEmbed.Field> fields = new ArrayList<>();
        fields.add(new MessageEmbed.Field("Play Again", "Would you like to play again?", false));

        MessageEmbed msgEmbed =  new MessageEmbed(
                null,
                "Rock Paper Scissors",
                this.opponentsSelection.equals(this.usersSelection) ? "Tie Game" : (String.format("%s Wins!", userWins() ? this.user.getAsMention() : this.opponent.getAsMention())),
                EmbedType.RICH,
                OffsetDateTime.now(),
                0,
                null,
                null,
                new MessageEmbed.AuthorInfo("Game Bot", null, null, null),
                null,
                null,
                null,
                fields);

        this.gameChannel.sendMessageEmbeds(msgEmbed).addActionRow(buttons).queue();
    }

    private boolean userWins() {
        if (this.usersSelection.equals(RockPaperScissorGame.ROCK)) {
            return this.opponentsSelection.equals(RockPaperScissorGame.SCISSORS);
        }
        if (this.usersSelection.equals(RockPaperScissorGame.PAPER)) {
            return this.opponentsSelection.equals(RockPaperScissorGame.ROCK);
        }

        return this.opponentsSelection.equals(RockPaperScissorGame.PAPER);

    }

    private void sendGameMessage() {
        // Create Buttons
        ArrayList<Button> buttons = new ArrayList<>();

        buttons.add(Button.primary(RockPaperScissorGame.ROCK, "Rock").withEmoji(Emoji.fromUnicode("U+1FAA8")));
        buttons.add(Button.primary(RockPaperScissorGame.PAPER, "Paper").withEmoji(Emoji.fromUnicode("U+1F4C4")));
        buttons.add(Button.primary(RockPaperScissorGame.SCISSORS, "Scissors").withEmoji(Emoji.fromUnicode("U+2702")));

        // Draw Board Emojis

        MessageEmbed msgEmbed =  new MessageEmbed(
                null,
                "Rock Paper Scissors",
                String.format("%s vs %s", this.user.getAsMention(), this.opponent.getAsMention()),
                EmbedType.RICH,
                OffsetDateTime.now(),
                0,
                null,
                null,
                new MessageEmbed.AuthorInfo("Game Bot", null, null, null),
                null,
                null,
                null,
                null);

        gameChannel.sendMessageEmbeds(msgEmbed).addActionRow(buttons).queue();

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
            RockPaperScissorGame RockPaperScissorGame = (RockPaperScissorGame) o;
            return RockPaperScissorGame.user.getId().equals(this.user.getId()) && RockPaperScissorGame.opponent.getId().equals(this.opponent.getId());
        }
        return false;
    }

}
