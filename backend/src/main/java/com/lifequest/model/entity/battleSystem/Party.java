package com.lifequest.model.entity.battleSystem;

import java.util.List;

public class Party {
    public List<PartyMember> members;

    public Party(List<PartyMember> members){
        this.members = members;
    }

    public List<PartyMember> getMembers(){
        return this.members;
    }

    public void addMember(PartyMember member){
        this.members.add(member);
    }

    public void removeMember(PartyMember id){
        this.members.remove(id);
    }

    public List<Player> getPlayerMembers(){
        return this.getMembers().stream()
                    .filter(members -> members.getFighterType()=="Player")
                    .map(members -> (Player) members.getFighter()).toList();
    }
}
