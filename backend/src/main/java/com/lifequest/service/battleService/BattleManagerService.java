package com.lifequest.service.battleService;
import com.lifequest.model.entity.battleSystem.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lifequest.model.dto.*;


@Service
public class BattleManagerService {

    public BattleManagerService(){
        
    }   

    public BattleStatusDto executeBattleTurn(Battle battle){
        increaseTurn(battle);
        
        
        return null;
    }

    public int increaseTurn(Battle battle){
        int turn = battle.getTurn();
        battle.setTurn(turn++);
        return turn+1;
    }

}
