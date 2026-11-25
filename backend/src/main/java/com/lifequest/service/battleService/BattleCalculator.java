package com.lifequest.service.battleService;
import java.lang.Math;

public class BattleCalculator {
    public static final int DAMAGE_MIN = 1;
    public static final double BASE_CRITICAL_CHANCE = 0.1;
    public static final double BASE_CRITICAL_DAMAGE = 2.0;
    public static final double BASE_HIT_CHANCE = 1.0;
    public static final double MIN_HIT_CHANCE = 0.3;
    public static final double STAMINA_DEFENSE_CONSTANT = 0.5;
    public static final double STRENGTH_OFENSE_CONSTANT = 0.5;

    public static int basicDamageCalc(int attack, int stamina, double dmgBuff, double dmgReduction){
        return (int)Math.max((STRENGTH_OFENSE_CONSTANT*attack*(1+dmgBuff) - STAMINA_DEFENSE_CONSTANT*stamina)*(1 - Math.min(1, dmgReduction)), 
                        DAMAGE_MIN);
    }

    public static double hitChanceCalc(int speedAtt, int speedDef){
        return Math.max(Math.min((speedAtt*1.0/speedDef), 1), MIN_HIT_CHANCE);
    }

    public static double critChanceCalc(int wisdomAtt, int wisdomDef){
        return Math.min((wisdomAtt*1.0/wisdomDef) * BASE_CRITICAL_CHANCE, 1);
    }

    public static double critDamageCalc(double critChance, double critExtraDamage){
        double crit = Math.random();
        return crit >= 1-critChance ? (BASE_CRITICAL_DAMAGE+critExtraDamage) : 1.0; 
    }

     /**
     * Calcula el daño final del combate combinando acierto, crítico y mitigaciones.
     * 
     * @return El daño total infligido (mínimo {@link #DAMAGE_MIN}), o {@code 0} si el ataque falla.
     */
    public static int finalDamageCalc(int attack, int stamina, double dmgBuff, double dmgReduction, int speedAtt, int speedDef, int wisdomAtt, int wisdomDef, double critExtraDamage){
        speedDef = Math.max(speedDef,1); //Nunca debería ocurrir
        wisdomDef = Math.max(wisdomDef,1);
        int result =(int)(basicDamageCalc(attack, stamina, dmgBuff, dmgReduction) * critDamageCalc(critChanceCalc(wisdomAtt, wisdomDef), critExtraDamage));

        result = hitChanceCalc(speedAtt, speedDef) >= 1-Math.random() ? Math.max(result, DAMAGE_MIN) : 0;

        return result;
    }

    public static int finalDamageCalc(int attack, int stamina){
        return finalDamageCalc(attack, stamina, 0, 0, 5, 5, 5, 5, 0); //El base son 5 en las stats
    }
}