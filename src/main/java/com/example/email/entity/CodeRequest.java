package com.example.email.entity;
import jakarta.validation.constraints.NotBlank;

public class CodeRequest {
    @NotBlank(message = "code is empty")
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}