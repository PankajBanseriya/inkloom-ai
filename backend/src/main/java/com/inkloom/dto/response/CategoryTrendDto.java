package com.inkloom.dto.response;

public class CategoryTrendDto {

    private String category;
    private long count;
    private int percentage;
    private String severity;

    public CategoryTrendDto() {}

    public CategoryTrendDto(String category, long count, int percentage, String severity) {
        this.category = category;
        this.count = count;
        this.percentage = percentage;
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
