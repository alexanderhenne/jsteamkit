package com.jsteamkit.steam;

public class LoginCredentials {

    public String username = "";

    public String password = "";

    public String authCode = "";

    public String authenticatorSecret = "";

    public boolean acceptSentry = true;

    /**
     * 1 = PC
     * 2 = PS3
     */
    public int accountInstance = 1;
}
