package com.geekbrains.krilov.serverNetty.AuthService;


public interface AuthService {

    void start();

    void stop();

    boolean logIn(String login, String password);

    void logOut(String login);

    void registerNewUser(String login, String password);
}
