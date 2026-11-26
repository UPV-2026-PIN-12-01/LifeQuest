package com.lifequest.model.entity.battleSystem;

import java.util.ArrayList;
import java.util.List;

public class PartyMember {
    private int memberId;
    private Fighter fighter;
    private Command command;
    private Long ActualHp;
    private String fighterType;
    private int ActualStamina;
    private double critExtraDamage;
    private double dmgBuff;
    private double dmgReduction;

    public PartyMember(Fighter fighter){
        this.memberId = fighter.id;
        this.fighter=fighter;
    }

    public String getFighterType(){
        return this.fighterType;
    }

    public void setFighterType(String type){
        this.fighterType = type;
    }

    public Fighter getFighter() {
        return fighter;
    }

    public Command getCommand(){
        return this.command;
    }

    public void setCommand(Command command){
        this.command = command;
    }

    public void setCommand(CommandType type, Fighter target){
        this.command = new Command(type, 0, 10, this.fighter, target);
    }

    public Long getActualHp(){
        return this.ActualHp;
    }

    public void setActualHp(Long hp){
        this.ActualHp = hp;
    }

    public double getCritExtraDamage(){
        return this.critExtraDamage;
    }

    public void setCritExtraDamage(double critExtraDamage){
        this.critExtraDamage = critExtraDamage;
    }

    public int getMemberId(){
        return this.memberId;
    }

    public void setMemberId(int id){
        this.memberId = id;
    }

    public int getActuaStamina(){
        return this.ActualStamina;
    }

    public double getDmgBuff(){
        return this.dmgBuff;
    }

    public void setDmgBuff(double dmgBuff){
        this.dmgBuff=dmgBuff;
    }

    public double getDmgReduction(){
        return this.dmgReduction;
    }
    
    public void setDmgReduction(double dmgReduction){
        this.dmgReduction=dmgReduction;
    }
}
