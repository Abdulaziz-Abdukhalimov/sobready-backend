package com.sobready.backend.enums;

/**
 * Your React enum uses numeric values: THIRTY = 30, FIFTY = 50, HUNDRED = 100
 * In Java, we store the numeric value inside the enum.
 */
public enum ProductVolume {
    THIRTY(30),
    FIFTY(50),
    HUNDRED(100);

    private final int value;

    ProductVolume(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
