package com.lifequest.model.entity.battleSystem;

public class Command {
    public CommandType type;
    public int priority;
    public int speed;
    public Fighter source;
    public Fighter target;

    public Command(CommandType type, int priority, int speed, Fighter source, Fighter target){
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

}
