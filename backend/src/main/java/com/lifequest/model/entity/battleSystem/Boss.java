package com.lifequest.model.entity.battleSystem;

public class Boss extends Monster {

    protected String bossPassive;

    public Boss(int id, String name, Long hp, int strength, int stamina, int speed, int wisdom, int roar, String passive) {
        super(id, name, hp, strength, stamina, speed, wisdom, roar);
        this.bossPassive = passive != null ? passive : "";
    }

    public String getBossPassive() {
        return this.bossPassive;
    }

    public void setBossPassive(String bossPassive) {
        this.bossPassive = bossPassive != null ? bossPassive : "";
    }
}