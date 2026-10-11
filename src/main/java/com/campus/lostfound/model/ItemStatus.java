package com.campus.lostfound.model;

public enum ItemStatus {
    OPEN("Open"), IN_PROGRESS("Someone replied"), CLOSED("Closed");
    private final String label;
    ItemStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
