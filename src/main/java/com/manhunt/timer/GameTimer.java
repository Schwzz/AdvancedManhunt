package com.manhunt.timer;

import lombok.Getter;

@Getter
public class GameTimer {
    
    private long startTime;
    private boolean running;
    
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }
    
    public void stop() {
        this.running = false;
    }
    
    public void reset() {
        this.startTime = 0;
        this.running = false;
    }
    
    public String getFormattedTime() {
        if (startTime == 0) {
            return "00:00:00:00";
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        long hours = elapsed / 3600000;
        long minutes = (elapsed % 3600000) / 60000;
        long seconds = (elapsed % 60000) / 1000;
        long centiseconds = (elapsed % 1000) / 10;
        
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, centiseconds);
    }
}