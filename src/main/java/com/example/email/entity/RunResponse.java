package com.example.email.entity;


public class RunResponse {
    private boolean success;
    private String stage; // compile/run
    private String output; // stdout
    private String error; // stderr
    private long timeMs;


    public RunResponse(boolean success, String stage, String output, String error, long timeMs) {
        this.success = success; this.stage = stage; this.output = output; this.error = error; this.timeMs = timeMs;
    }


    public boolean isSuccess() { return success; }
    public String getStage() { return stage; }
    public String getOutput() { return output; }
    public String getError() { return error; }
    public long getTimeMs() { return timeMs; }
}