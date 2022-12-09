package org.GameBot.commands.ConnectFour;

import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class ConnectFourGame extends ListenerAdapter {
    private static final String EMPTY = ":black_circle:";
    private static final String USER = ":red_circle:";
    private static final String OPPONENT = ":yellow_circle:";
    private final User user;
    private final User opponent;
    private final TextChannel gameChannel;
    private User playersTurn;

    private Message boardMessage;
    private Message buttonMessage;
    private boolean isCompleted = false;
    private User winner;


    private final String[][] board = new String[6][7];
    public ConnectFourGame(User user, User opponent, TextChannel gameChannel) {
        this.user = user;
        this.gameChannel = gameChannel;
        this.opponent = opponent;
        this.playersTurn = user;

        // Make empty board
        for (String[] row : this.board) {
            Arrays.fill(row, ConnectFourGame.EMPTY);
        }


        // Create Buttons
        ArrayList<Button> buttonsOne = new ArrayList<>();
        buttonsOne.add(Button.primary("0", "1"));
        buttonsOne.add(Button.primary("1", "2"));
        buttonsOne.add(Button.primary("2", "3"));
        buttonsOne.add(Button.primary("3", "4"));

        ArrayList<Button> buttonsTwo = new ArrayList<>();
        buttonsTwo.add(Button.primary("4", "5"));
        buttonsTwo.add(Button.primary("5", "6"));
        buttonsTwo.add(Button.primary("6", "7"));

        // Draw Board Emojis
        gameChannel.sendMessageEmbeds(drawBoard()).addActionRow(buttonsOne).queue(boardMessage -> this.boardMessage = boardMessage);
        gameChannel.sendMessage("").addActionRow(buttonsTwo).queue(buttonMessage -> this.buttonMessage = buttonMessage);
    }

    public ConnectFourGame(User user, User opponent) {
        this.user = user;
        this.opponent = opponent;
        this.gameChannel = null;
        this.boardMessage = null;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if ((event.getMessage().getIdLong() == this.boardMessage.getIdLong() || event.getMessage().getIdLong() == this.buttonMessage.getIdLong()) &&
                event.getChannel().getIdLong() == this.gameChannel.getIdLong()
        ) {
            if (event.getUser().getId().equals(this.playersTurn.getId()) && !isCompleted) {
                this.playersTurn = event.getUser().getId().equals(this.user.getId()) ?
                        this.opponent : this.user;

                addBoardPiece(event, Integer.parseInt(event.getButton().getId()), 5);


                if (this.isCompleted) {
                    event.reply(this.winner.getAsMention() + " won!\nEnter /close-connect-four to close the channel").queue();
                } else {
                    event.reply("Successful Move").setEphemeral(true).queue();
                }

            } else if (isCompleted) {
                event.reply("Game is over.\nEnter /close-connect-four to close the channel").setEphemeral(true).queue();
            } else {
                event.reply("Not your turn!").setEphemeral(true).queue();
            }
        }


    }


    private void addBoardPiece(ButtonInteractionEvent event, int column, int row) {

        if (row == -1) {
            event.reply("Column is Full").setEphemeral(true).queue();
            return;
        }
        if (this.board[row][column].equals(ConnectFourGame.EMPTY)) {

            this.board[row][column] = event.getUser().getId().equals(this.user.getId()) ?
                    ConnectFourGame.USER : ConnectFourGame.OPPONENT;

            if (isWinner()) {
                this.isCompleted = true;
                this.winner = this.playersTurn.getId().equals(this.user.getId()) ? this.opponent : this.user;
            }

            this.boardMessage.editMessageEmbeds(drawBoard()).queue();

            return; // End recursive loop
        }

        addBoardPiece(event, column, row - 1);
    }

    private boolean isWinner() {
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                String color = this.board[i][j];
                if (!color.equals(ConnectFourGame.EMPTY) && isFourInRow(i, j, this.board[i][j])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFourInRow(int row, int column, String color) {
        int rightCount = 0;
        int rightColumn = column;
        for (int i = 0; i < 3; i++) {
            if (rightColumn + 1 < this.board.length && color.equals(this.board[row][rightColumn + 1])) {
                rightCount += 1;
            }
            rightColumn++;
        }
        int topRightCount = 0;
        int topRightColumn = column;
        int topRightRow = row;
        for (int i = 0; i < 3; i++) {
            if (topRightRow - 1 >= 0 && topRightColumn + 1 < this.board.length && color.equals(this.board[topRightRow - 1][topRightColumn + 1])) {
                topRightCount += 1;
            }
            topRightColumn++;
            topRightRow--;
        }

        int topCount = 0;
        int topRow = row;
        for (int i = 0; i < 3; i++) {
            if (topRow - 1 >= 0 && color.equals(this.board[topRow - 1][column])) {
                topCount += 1;
            }
            topRow--;
        }

        int topLeftCount = 0;
        int topLeftColumn = column;
        int topLeftRow = row;
        for (int i = 0; i < 3; i++) {
            if (topLeftRow - 1 >= 0 && topLeftColumn - 1 >= 0 && color.equals(this.board[topLeftRow - 1][topLeftColumn - 1])) {
                topLeftCount += 1;
            }
            topLeftColumn--;
            topLeftRow--;
        }

        int leftCount = 0;
        int leftColumn = column;
        for (int i = 0; i < 3; i++) {
            if (leftColumn - 1 >= 0 && color.equals(this.board[row][leftColumn - 1])) {
                leftCount += 1;
            }
            leftColumn--;
        }

        return rightCount == 3 || topRightCount == 3 || topCount == 3 || topLeftCount == 3 || leftCount == 3;
    }


    private MessageEmbed drawBoard() {
        ArrayList<MessageEmbed.Field> fields = new ArrayList<>();
        for (String[] row: this.board) {
            StringBuilder circles = new StringBuilder();
            for (int i = 0; i < row.length; i++) {
                if (i == row.length - 1) {
                    circles.append(row[i]);
                } else {
                    circles.append(row[i]).append(" ");
                }

            }

            fields.add(new MessageEmbed.Field("", circles.toString(), false));
        }

        return new MessageEmbed(
                null,
                "Connect 4",
                 this.isCompleted ? this.winner.getAsMention() + " Won!"  : this.playersTurn.getAsMention() + "'s turn",
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
            ConnectFourGame connectFourGame = (ConnectFourGame) o;
            return connectFourGame.user.getId().equals(this.user.getId()) && connectFourGame.opponent.getId().equals(this.opponent.getId());
        }
        return false;
    }

}
