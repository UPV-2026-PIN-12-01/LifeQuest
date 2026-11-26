package com.lifequest.model.entity.battleSystem;

public enum CommandType {
    ATTACK("atacado"),
    DEFEND("defendido"),
    HEAL("curado"),
    PASS("quedado en el suelo");

    private final String label;

    CommandType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
