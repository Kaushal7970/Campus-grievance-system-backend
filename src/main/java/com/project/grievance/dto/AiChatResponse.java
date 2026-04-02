package com.project.grievance.dto;

public class AiChatResponse {

    private String response;
    private String category;
    private String priority;
    private String suggested_solution;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSuggested_solution() {
        return suggested_solution;
    }

    public void setSuggested_solution(String suggested_solution) {
        this.suggested_solution = suggested_solution;
    }
}
