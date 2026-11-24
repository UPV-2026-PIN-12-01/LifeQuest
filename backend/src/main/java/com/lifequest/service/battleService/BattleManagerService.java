package com.lifequest.service.battleService;
import com.lifequest.model.entity.battleSystem.*;


public class BattleManagerService {

    public Battle battle;
    public int nOfCommandsToStart;

    public BattleManagerService(Battle battle){
        this.battle = battle;

    }

    public Battle getBattle() {
        return battle;
    }
    public int getnOfCommandsToStart() {
        return nOfCommandsToStart;
    }

}
