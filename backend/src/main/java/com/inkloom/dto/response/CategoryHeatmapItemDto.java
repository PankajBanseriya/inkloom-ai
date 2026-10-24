package com.inkloom.dto.response;

import java.util.ArrayList;
import java.util.List;

public class CategoryHeatmapItemDto {

    private String category;
    private long occurrences;
    private List<String> repeatedPhrases = new ArrayList<>();
    private String acceptanceRate;
    private String conflictSignal;
    private String status; // NORMAL, NEEDS_ATTENTION, SOURCE_GAP

    public CategoryHeatmapItemDto() {}

    public CategoryHeatmapItemDto(String category, long occurrences, List<String> repeatedPhrases,
                                 String acceptanceRate, String conflictSignal, String status) {
        this.category = category;
        this.occurrences = occurrences;
        this.repeatedPhrases = repeatedPhrases;
        this.acceptanceRate = acceptanceRate;
        this.conflictSignal = conflictSignal;
        this.status = status;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getOccurrences() { return occurrences; }
    public void setOccurrences(long occurrences) { this.occurrences = occurrences; }

    public List<String> getRepeatedPhrases() { return repeatedPhrases; }
    public void setRepeatedPhrases(List<String> repeatedPhrases) { this.repeatedPhrases = repeatedPhrases; }

    public String getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(String acceptanceRate) { this.acceptanceRate = acceptanceRate; }

    public String getConflictSignal() { return conflictSignal; }
    public void setConflictSignal(String conflictSignal) { this.conflictSignal = conflictSignal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
