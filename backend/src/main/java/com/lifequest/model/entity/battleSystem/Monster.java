package com.lifequest.model.entity.battleSystem;

public class Monster extends Fighter {

    private int roar;

    public Monster(int id, String name, Long hp, int strength, int stamina, int speed, int wisdom, int roar) {
        super(
            id,
            name != null ? name : "Monstruo",
            hp != null && hp > 0 ? hp : 1L,
            strength,
            stamina,
            speed,
            wisdom
        );
        this.roar = Math.max(0, roar);
    }

    public int getRoar() {
        return roar;
    }

    public void setRoar(int roar) {
        this.roar = Math.max(0, roar);
    }

    public int getRoarStrength() {
        return this.roar + this.strength;
    }
}