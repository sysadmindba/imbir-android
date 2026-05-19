package com.imbir.game.data

enum class CatAnimation(val key: String) {
    // Idle
    IDLE_BLINK("idle_blink"),
    IDLE_TAIL("idle_tail"),
    IDLE_BREATH("idle_breath"),
    
    // Movement
    WALK_LEFT("walk_left"),
    WALK_RIGHT("walk_right"),
    RUN("run"),
    JUMP("jump"),
    STRETCH("stretch"),
    
    // Care
    EAT("eat"),
    DRINK("drink"),
    SLEEP("sleep"),
    WAKE("wake"),
    BATH("bath"),
    PET("pet"),
    GROOM("groom"),
    
    // Emotions
    HAPPY("happy"),
    SAD("sad"),
    ANGRY("angry"),
    SICK("sick"),
    SLEEPY("sleepy"),
    EXCITED("excited"),
    SCARED("scared"),
    
    // Play
    LASER("laser"),
    BALL("ball"),
    MOUSE("mouse"),
    SCRATCH("scratch"),
    
    // Special
    LOOK_AT_PLAYER("look_player"),
    BEG("beg"),
    LIE_BACK("lie_back"),
    DEATH("death")
}
