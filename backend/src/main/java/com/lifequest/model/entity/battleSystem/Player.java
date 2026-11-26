package com.lifequest.model.entity.battleSystem;

public class Player extends Fighter {

    protected String playerPassive;

    public Player(int id, String name, Long hp, int strength, int stamina, int speed, int wisdom, String passive) {
        super(id, name, hp, strength, stamina, speed, wisdom);
        this.playerPassive = passive != null ? passive : "";
    }

    public String getPlayerPassive() {
        return this.playerPassive;
    }

    public void setPlayerPassive(String playerPassive) {
        this.playerPassive = playerPassive != null ? playerPassive : "";
    }
}
