package com.revconnect.dto.request;

public class VerifySecurityRequest {
    private String email;
    private String answer1;
    private String answer2;
    private String answer3;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAnswer1() { return answer1; }
    public void setAnswer1(String answer1) { this.answer1 = answer1; }

    public String getAnswer2() { return answer2; }
    public void setAnswer2(String answer2) { this.answer2 = answer2; }

    public String getAnswer3() { return answer3; }
    public void setAnswer3(String answer3) { this.answer3 = answer3; }
}