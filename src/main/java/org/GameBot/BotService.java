package org.GameBot;

import javax.security.auth.login.LoginException;

public class BotService {
    public static void main(String[] args) {
        try {
            new GameBot();
        } catch (LoginException e) {
            System.out.println("Invalid Token");
        }
    }
}
