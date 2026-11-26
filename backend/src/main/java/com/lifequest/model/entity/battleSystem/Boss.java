package com.lifequest.model.entity.battleSystem;

public class Boss extends Monster {

    public String bossPassive;

    public Boss(int id, String name, Long hp, int strength, int stamina, int speed, int wisdom, int roar, String passive){
        super(id, name, hp, strength, stamina, speed, wisdom, roar);
        this.bossPassive = passive;
    }

    public String getPassive(){
        return this.bossPassive;
    }

    public void setPassive(String passive){
        this.bossPassive = passive;
    }
}