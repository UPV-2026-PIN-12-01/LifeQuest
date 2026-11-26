package com.lifequest.model.entity.battleSystem;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.internal.util.compare.ComparableComparator;

public class Command implements Comparable<Command>{
    public CommandType type;
    public int priority;
    public int speed;
    public Fighter source;
    public Fighter target;

    public Command(CommandType type, int priority, int speed, Fighter source, Fighter target){
        super();
        this.type = type;
        this.priority = priority;
        this.speed = speed;
        this.source = source;
        this.target = target;
    }

    public CommandType getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public int getSpeed() {
        return speed;
    }

    public Fighter getTarget() {
        return this.target;
    }

    public Fighter getSource() {
        return this.source;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setTarget(Fighter target) {
        this.target = target;
    }

    public void setSource(Fighter source) {
        this.source = source;
    }

    public String getCommandMessage(int turn, int chainId){
        BattleMessage battleMessage = new BattleMessage(turn, this.source.toString(), this.target.toString(), this.type.getLabel(), chainId);
        return battleMessage.showBattleMsg();
    }

    //Sort de mayor a menor
    public int compareTo(Command commandB){
        if (this.getPriority() > commandB.getPriority())
            return -1;
        else if(this.getPriority() < commandB.getPriority())
            return 1;
        else if(this.getSpeed() > commandB.getSpeed())
            return -1;
        else if(this.getSpeed() < commandB.getSpeed())
            return 1;
        else
            return 0;
    }

}
