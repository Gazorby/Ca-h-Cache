package com.example.cachecache;

public enum Radio {
    GSM("gsm"),
    WCDMA("wcdma"),
    LTE("lte");

    private String toString;

    Radio(String toString) {
        this.toString = toString;
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
