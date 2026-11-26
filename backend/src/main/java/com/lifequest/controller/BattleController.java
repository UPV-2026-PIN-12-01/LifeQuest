package com.lifequest.controller;

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
import com.lifequest.service.battleService.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {
    private final BattleManagerService battleManagerService;

    public BattleController(BattleManagerService battleManagerService){
        this.battleManagerService = battleManagerService;
    }

    @PostMapping("/command/{command}/{battleId}")
    public BattleStatusDto battleResponse(){
        return null;
    }
 
}
