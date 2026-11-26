package com.lifequest.service.battleService;

import com.lifequest.model.dto.BattleStatusDto;
import com.lifequest.model.entity.battleSystem.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BattleManagerService {


    public BattleManagerService(){
    }   

    public BattleStatusDto executeBattleTurn(Battle battle){
        increaseTurn(battle);

        List<PartyMember> entities = new ArrayList<>(battle.getPartyA().getMembers());
                          entities.addAll(battle.getPartyB().getMembers());

        List<Command> commands = entities.stream().map(n -> n.getCommand()).sorted().toList();

        Fighter attacker;
        Fighter defender;
        PartyMember attackerMember;
        PartyMember defenderMember;
        for (Command command : commands) {
            if (command.getType() == (CommandType.ATTACK))
            try{
                attacker = command.getSource();
                defender = command.getTarget();
                attackerMember = findPartyMemberById(entities,command.getSource().getFighterId()); //memberId == fighterId
                defenderMember = findPartyMemberById(entities,command.getTarget().getFighterId());
                if(attackerMember==null ||defenderMember==null) throw new Exception("Id nulo");
                decreaseHp( BattleCalculator.finalDamageCalc(
                                                 attacker.getStrength(), 
                                                 defender.getStamina(),
                                                 attackerMember.getDmgBuff(),
                                                 defenderMember.getDmgReduction(),
                                                 attacker.getSpeed(), 
                                                 defender.getSpeed(),
                                                 attacker.getWisdom(),
                                                 defender.getWisdom(),
                                                 attackerMember.getCritExtraDamage()
                                                ),
                            defenderMember
                        );
              }catch(Exception e){
                    System.out.println("Ocurrió un error en el comando:" +command);
              }
        }

        return null; //Para que compile
    }

    public int increaseTurn(Battle battle){
        int turn = battle.getTurn();
        battle.setTurn(++turn);
        return turn;
    }

    public void decreaseHp(int loss, PartyMember member){
       Long ActualHp = member.getActualHp(); 
       member.setActualHp(Math.max(ActualHp-loss, 0));
    }

    public PartyMember findPartyMemberById(List<PartyMember> entities, int memberId){
      try{  
            for(PartyMember entity : entities) {
                if(entity.getMemberId()==memberId){
                    return entity;
                }
            }
            throw new Exception("No se encontró el memberId: "+memberId);
        }catch(Exception e){
            return null;
        }
    }

}
