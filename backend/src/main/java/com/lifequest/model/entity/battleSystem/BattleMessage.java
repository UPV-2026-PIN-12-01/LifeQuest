package com.lifequest.model.entity.battleSystem;

public class BattleMessage {
    private int id;
    private int turn;
    private String source;
    private String target;
    private String command;
    private int chainId;

    public BattleMessage(int turn, String source, String target, String command, int chainId){
        this.id = turn*100 + chainId;
        this.turn = turn;
        this.source = source;
        this.target = target;
        this.command = command;
        this.chainId = chainId;
    }

    public String startBattleMsg(){
        return "¡El combate de "+source+" contra "+target+" ha comenzado!¡A luchar!";
    }

    public int getTurn() { return turn; }
    public String getSource() { return source; }
    public String getTarget() { return target; }
    public String getCommand() { return command; }
    public int getChainId() { return chainId; }

    public String showBattleMsg(){
        String res = "¡";
        if (chainId == 0)
            res += "TURNO "+turn+": ";
        if (target != null && target.equals(source)){
            res += source+" se ha "+command;
        }else{
            switch (command) {
                case("faint") : res += source+ " cayó derrotado"; break;
                default : res += source+" ha "+command+ " a "+target;        
            }
        }
        res+="!";
        return res;
    }
}
