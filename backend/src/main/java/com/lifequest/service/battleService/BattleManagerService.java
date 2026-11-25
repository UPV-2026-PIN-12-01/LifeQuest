package com.lifequest.service.battleService;
import com.lifequest.model.entity.battleSystem.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lifequest.model.dto.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class BattleManagerService {

    public BattleManagerService(){

    }   

    public BattleStatusDto executeBattleTurn(Battle battle){
        increaseTurn(battle);

        List<PartyMember> entities = new ArrayList<>(battle.partyA.members);
                          entities.addAll(battle.partyB.members);

        List<Command> commands = entities.stream().map(n -> n.getCommand()).toList();

        for (PartyMember member : entities) {
            BattleCalculator.finalDamageCalc(member.getFighter().getStrength(), member.getCommand().getTarget().getStamina());
        }
        
        return null;
    }

    public int increaseTurn(Battle battle){
        int turn = battle.getTurn();
        battle.setTurn(turn++);
        return turn+1;
    }

}
