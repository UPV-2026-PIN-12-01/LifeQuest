package com.lifequest.model.entity.battleSystem;

public class Fighter {
    protected Long hp;
    protected int strength;
    protected int stamina;
    protected int speed;
    protected int wisdom;

    public Fighter(Long hp, int strength, int stamina, int speed, int wisdom){
        this.hp = hp;
        this.strength = strength;
        this.stamina = stamina;
        this.speed= speed;
        this.wisdom = wisdom;
    }

    public Long getHp(){
        return this.hp;
    }

    public int getStrength(){
        return this.strength;
    }

    public int getStamina(){
        return this.stamina;
    }

    public int getSpeed(){
        return this.speed;
    }

    public int getWisdom(){
        return this.wisdom;
    }

    public void setHp(Long hp){
        this.hp = hp;
    }

    public void setStrength(int strength){
        this.strength=strength;
    }

    public void setStamina(int stamina){
        this.stamina=stamina;
    }

    public void setSpeed(int speed){
        this.speed=speed;
    }

    public void setWisdom(int wisdom){
        this.wisdom=wisdom;
    }
}
