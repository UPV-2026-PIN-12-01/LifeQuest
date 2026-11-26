package com.lifequest.model.entity.battleSystem;

public class Battle {
    public Party partyA;
    public Party partyB;
    public int nOfCommandsToStart;
    public int turn;

    public int getTurn(){
        return this.turn;
    }

    public void setTurn(int turn){
        this.turn = turn;
    }

    public int getnOfCommandsToStart() {
        return nOfCommandsToStart;
    }

    public void setnOfCommandsToStart(int nCommands){
        this.nOfCommandsToStart = nCommands;
    }

    public Party getPartyA(){
        return this.partyA;
    }

    public void setPartyA(Party partyA){
        this.partyA = partyA;
    }

    public Party getPartyB(){
        return this.partyB;
    }

    public void setPartyB(Party partyB){
        this.partyB = partyB;
    }


}
