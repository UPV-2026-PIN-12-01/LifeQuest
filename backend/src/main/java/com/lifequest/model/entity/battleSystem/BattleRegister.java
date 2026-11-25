package com.lifequest.model.entity.battleSystem;

public class BattleRegister {
    public int id;
    public int turn;
    public String source;
    public String target;
    public String command;
    public int chainId;

    public BattleRegister(int turn, String source, String target, String command, int chainId){
        this.id = turn + chainId;
        this.turn = turn;
        this.source = source;
        this.target = target;
        this.command = command;
        this.chainId = chainId;
    }

    public String startBattleMasg(){
        return "¡El combate de "+source+" contra "+target+" ha comenzado!¡A luchar!";
    }

    public String showBattleMsg(){
        String res = "¡";
        if (chainId == id)
            res += "TURNO "+turn+": ";
        if (target == source){
            res+=turn+": "+source+" se ha "+command;
        }else{
            switch (command) {
                case("faint") : res += source+ " "+command+" cayó derrotado";
                default : res += source+" ha "+command+ " a "+target;        
            }
        }
        res+="!";
        return res;
    }
}
