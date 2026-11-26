package com.lifequest.service.battleService;

import com.lifequest.exception.InvalidEnumException;
import com.lifequest.model.dto.BattleStatusDto;
import com.lifequest.model.entity.battleSystem.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class BattleManagerService {

    public final static double DEFENSE_CONSTANT=0.5;

    public BattleManagerService(){
    }   

    public Battle creatBattle(Party partyA, Party partyB){
        return new Battle(partyA, partyB);
    }

    public BattleStatusDto executeBattleTurn(Battle battle){
        increaseTurn(battle);

        List<PartyMember> entities = new ArrayList<>(battle.getPartyA().getMembers());
                          entities.addAll(battle.getPartyB().getMembers());
        List<PartyMember> defenders = new ArrayList<PartyMember>();

        List<Command> commands = entities.stream().map(n -> n.getCommand()).sorted().toList();

        List<String> commandTrace = new ArrayList<String>();

        Fighter attacker;
        Fighter victim;
        PartyMember attackerMember;
        PartyMember victimMember;
        int chainId=0;

        //Ejecución del turno
        for (Command command : commands) {
          try{

            attacker = command.getSource();
            victim = command.getTarget();
            attackerMember = findPartyMemberById(entities,command.getSource().getFighterId()); //memberId == fighterId
            victimMember = findPartyMemberById(entities,command.getTarget().getFighterId());

            if(attackerMember==null ||victimMember==null) throw new Exception("Id nulo");
            if(attackerMember.getActualHp()==0 || victimMember.getActualHp()==0) continue;

            switch (command.getType()){

              case CommandType.PASS :
                continue;
            
              case CommandType.ATTACK :

                decreaseHp( BattleCalculator.finalDamageCalc(
                                                 attacker.getStrength(), 
                                                 victim.getStamina(),
                                                 attackerMember.getDmgBuff(),
                                                 victimMember.getDmgReduction(),
                                                 attacker.getSpeed(), 
                                                 victim.getSpeed(),
                                                 attacker.getWisdom(),
                                                 victim.getWisdom(),
                                                 attackerMember.getCritExtraDamage()
                                                ),
                            victimMember
                        ); break;

              case CommandType.DEFEND : 
                defend(victimMember, defenders); break;

              case CommandType.HEAL : 
                increaseHp( 100, victimMember); break;

              default :
                throw new InvalidEnumException("No soportada");
                          
            }

            //Traza de las acciones
            commandTrace.add(command.getCommandMessage(battle.turn, chainId));
            chainId++;

          }catch(InvalidEnumException e){
            System.out.println("Enumeracion no soportada");
            
          }catch(Exception e){
            System.out.println("Ocurrió un error en el comando:" +command);
          }
        }

        //Eliminar defensa
        for(PartyMember defender : defenders){
            unDefend(defender, defenders);
        }

        return null;
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

    public void increaseHp(int healing, PartyMember member){
        Long ActualHp = member.getActualHp();
        member.setActualHp(Math.min(ActualHp + healing, member.getFighter().getHp()));
    }

    public void defend(PartyMember defender, List<PartyMember> defenders){
        double dmgReduction = defender.getDmgReduction();
        defender.setDmgReduction(DEFENSE_CONSTANT*dmgReduction);
        defenders.add(defender);
    }
    //Precond: DEFENSE_CONSTANT > 0
    public void unDefend(PartyMember defender, List<PartyMember> defenders){
        double newDmgReduction = defender.getDmgReduction()*(1/DEFENSE_CONSTANT);
        defender.setDmgReduction(newDmgReduction);
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
