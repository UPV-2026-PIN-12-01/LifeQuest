package com.lifequest.model.entity.battleSystem;

public class Monster extends Fighter {

    private int roar; 

    public Monster(Long hp, int strength, int stamina, int speed, int wisdom, int roar){
        super(hp, strength, stamina, speed, wisdom);
        this.roar = roar;
    }

    public int getRoarStrength(){
        return this.roar+this.strength;
    }
}
