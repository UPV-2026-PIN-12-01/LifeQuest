package com.lifequest.model.entity.battleSystem;

public class PartyMember {
    private Fighter fighter;
    private Command command;

    public PartyMember(Fighter fighter){
        this.fighter=fighter;
    }

    public Fighter getFighter() {
        return fighter;
    }

    public Command getCommand(){
        return this.command;
    }
}
