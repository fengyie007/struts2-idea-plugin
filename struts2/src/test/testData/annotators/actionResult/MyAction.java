package com.example;

public class MyAction {
    
    public String execute() {
        // This method should show gutter icons with:
        // success → /success.jsp
        // error → /error.jsp  
        // input → /input.jsp
        return "success";
    }
    
    public String login() {
        // This method should show gutter icons with:
        // success → /welcome.jsp
        // failure → /login.jsp
        return "success";
    }
}