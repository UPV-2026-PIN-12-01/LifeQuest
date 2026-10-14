package com.lifequest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/character")
public class CharacterController {

    @GetMapping
    public String getCharacter() {
        return "List of characters";
    }

    public record CharacterResponse(String name, int level,
             String classType, int xp, int gold, int hp, int strength, int speed, int wisdom, int stamina) {
            
                
    }
}