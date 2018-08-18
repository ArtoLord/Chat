package com.example.root.forhelp.Requests;

import retrofit2.http.Query;

/**
 * Created by root on 06.03.18.
 */

public class LogUser {
    String email;
    String password;

    public LogUser(String email, String pass) {
        this.email = email;
        this.password = pass;
    }
}
