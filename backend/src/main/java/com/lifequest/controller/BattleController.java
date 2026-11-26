package com.lifequest.controller;

import org.hibernate.type.descriptor.jdbc.InstantAsTimestampJdbcType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lifequest.model.dto.BattleStatusDto;
import com.lifequest.model.entity.Reward;
import com.lifequest.service.RewardService;
import com.lifequest.service.battleService.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {
    private final BattleManagerService battleManagerService;
    private final RewardService rewardService;

    public BattleController(BattleManagerService battleManagerService, RewardService rewardService){
        this.battleManagerService = battleManagerService;
        this.rewardService = rewardService;
    }

    @PostMapping("/command/{command}/{battleId}")
    public BattleStatusDto battleResponse(){
        return null;
    }

    @PostMapping("/claim")
    public String claimReward(@RequestBody String entity) {
        //TODO: process POST request
        rewardService.claim(new Reward(), 100, 0, null);
        return entity;
    }
    
 
}
