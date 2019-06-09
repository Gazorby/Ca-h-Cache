package com.mimi.cachecache;

public enum UserCategory {
    HIDDEN("hidden"),
    FINDER("finder");

    private final String type;

    UserCategory(String type) {
     this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
