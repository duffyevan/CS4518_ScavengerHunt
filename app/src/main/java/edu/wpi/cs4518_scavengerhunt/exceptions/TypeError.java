package edu.wpi.cs4518_scavengerhunt.exceptions;

public class TypeError extends Exception {
    private final String message;
    public TypeError(String message){
        this.message = message;
    }

    @Override
    public String toString() {
        return "TypeError: " + message;
    }
}
