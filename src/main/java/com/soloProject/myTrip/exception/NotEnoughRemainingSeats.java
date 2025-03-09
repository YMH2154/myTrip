package com.soloProject.myTrip.exception;

public class NotEnoughRemainingSeats extends RuntimeException {
    public NotEnoughRemainingSeats(String message) {
        super(message);
    }
}
