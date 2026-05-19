# Imbir — UI/UX & Animation System Specification
**Version 1.0 | Android Mobile Tamagotchi Cat Game**

> This document is the authoritative reference for all visual, animation, interaction, and feedback behavior in Imbir. It is structured for four audiences: UI/UX designers, Android/Unity developers, animation designers, and AI asset generators. Sections are tagged accordingly.

---

## Table of Contents

1. [Core Design Principles](#1-core-design-principles)
2. [Cat Visual Architecture](#2-cat-visual-architecture)
3. [Emotion State System](#3-emotion-state-system)
4. [Idle Animation Loop System](#4-idle-animation-loop-system)
5. [Touch Interaction System](#5-touch-interaction-system)
6. [Action Animation Sequences](#6-action-animation-sequences)
7. [Micro-Reaction System](#7-micro-reaction-system)
8. [Emotional Transitions](#8-emotional-transitions)
9. [Movement System](#9-movement-system)
10. [Environment Interaction System](#10-environment-interaction-system)
11. [Camera & UI Feedback Layer](#11-camera--ui-feedback-layer)
12. [Progression Visual Evolution](#12-progression-visual-evolution)
13. [Audio Layer](#13-audio-layer)
14. [Asset List & Naming Convention](#14-asset-list--naming-convention)
15. [AI Asset Generation Prompts](#15-ai-asset-generation-prompts)
16. [Android Implementation Guide](#16-android-implementation-guide)

---

## 1. Core Design Principles

### The Living Cat Rule
The cat is never static. At any point in time it is either:
- executing an **action animation** (player-triggered)
- running an **idle loop** (autonomous behavior)
- displaying a **micro-reaction** (environmental/touch response)

### Response Timing Contract
| Event type | Max latency |
|---|---|
| Touch micro-reaction | 0.1 s |
| Action animation start | 0.2 s |
| Emotional state change visible | 0.3 s |
| Idle loop transition | 0.5 s |

### Layered Animation Model
The cat renders **three simultaneous animation layers**:

```
Layer 3 — EMOTION OVERLAY     (face, ears, tail posture — always visible)
Layer 2 — ACTION ANIMATION    (full-body action sequence — player triggered)
Layer 1 — IDLE BASE LOOP      (breathing, subtle sway — always running)
```

Upper layers override lower layers. When an action ends, layers below resume seamlessly.

### Personality Affects Everything
Cat personality (NEUTRAL, AFFECTIONATE, PLAYFUL, STUBBORN, LAZY, INDEPENDENT) modifies animation timing, expression intensity, and interaction response. See §12 for details.

---

## 2. Cat Visual Architecture

### 2.1 Body Structure (Sprite Layers)

For 2D sprite implementation, the cat is composed of **7 independently animated regions**:

```
┌─────────────────────────────────────┐
│  [EARS L] [HEAD] [EARS R]           │  ← Region A: Head group
│       [EYES] [NOSE] [MOUTH]         │  ← Region B: Face group
│         [BODY / TORSO]              │  ← Region C: Body
│      [FRONT PAWS L + R]             │  ← Region D: Front limbs
│      [BACK PAWS L + R]              │  ← Region E: Back limbs
│           [TAIL]                    │  ← Region F: Tail
│         [FUR OVERLAY]               │  ← Region G: Surface detail
└─────────────────────────────────────┘
```

Each region animates **independently**, enabling:
- ears twitching while body is still
- tail moving while cat sleeps
- eyes blinking during any other animation

### 2.2 Sprite Sheet Layout (per growth stage)

Each growth stage needs its own sprite sheet. Recommended sheet resolution: **2048 × 2048 px**, 16-frame grid (4×4).

```
Growth stages: KITTEN | TEEN | ADULT | SENIOR
```

One sheet per stage, one sheet per action category (see §14 for full list).

### 2.3 Color Palette — Ginger Cat

| Area | Base color | Shadow | Highlight |
|---|---|---|---|
| Fur — main | #E8863A | #C05A10 | #FFC070 |
| Fur — chest/belly | #FDE8C8 | #E0C098 | #FFF8F0 |
| Stripes | #A0440A | — | — |
| Eyes | #4AAD52 (green) | #2A7A30 | #AAFFA0 |
| Nose | #E07080 | — | — |
| Paw pads | #CC8090 | — | — |
| Inner ear | #FFB0B0 | — | — |

---

## 3. Emotion State System

### 3.1 The Ten Emotion States

Each state affects four visual channels simultaneously:

#### HAPPY
- **Eyes:** Wide open, slightly curved upward (smile-eye shape), pupils normal
- **Ears:** Upright, tilted 10° outward — alert and open
- **Tail:** High arc, tip curling, slow rhythmic sway left-right
- **Body posture:** Upright, chest forward, weight evenly distributed
- **Breathing rhythm:** Medium pace, visible chest rise
- **Idle intensity:** Active — stretches, looks around, occasional excited bounce

#### CALM
- **Eyes:** Half-lidded (60% open), relaxed oval shape
- **Ears:** Upright, slightly relaxed, no tension
- **Tail:** Slow gentle sweep, low amplitude
- **Body posture:** Settled, relaxed weight, may be lying down
- **Breathing rhythm:** Slow, deep, very visible chest movement
- **Idle intensity:** Low — mostly still with occasional ear twitch

#### CURIOUS
- **Eyes:** Wide, round pupils enlarged, one ear tilted forward
- **Ears:** Asymmetric — one forward, one sideways
- **Tail:** Upright with question-mark curl, tip twitching rapidly
- **Body posture:** Weight shifted forward, head tilted 15°
- **Breathing rhythm:** Slightly faster
- **Idle intensity:** Focused stillness broken by sudden head turns

#### HUNGRY
- **Eyes:** Slightly narrowed, pupils neutral, occasional longing glance downward
- **Ears:** Flat-ish, drooping 20° outward
- **Tail:** Slow, low swishing — impatient
- **Body posture:** Slouched slightly, belly area visually emphasized
- **Breathing rhythm:** Normal but slightly exaggerated belly
- **Idle intensity:** Restless — pacing, sniffing, looking toward feeding area

#### ANGRY
- **Eyes:** Narrowed slit pupils, pupils contracted, brow furrowed (fur raised above eyes)
- **Ears:** Flat and rotated fully backward against head
- **Tail:** Puffed, rapid aggressive side-to-side lash
- **Body posture:** Arched back, fur standing slightly, weight back on haunches
- **Breathing rhythm:** Rapid, shallow
- **Idle intensity:** Tense stillness — occasional hiss-position lean

#### SAD
- **Eyes:** Half-closed, downcast gaze, pupils dilated softly
- **Ears:** Drooped downward 45°, wide apart
- **Tail:** Wrapped around body or dragging low
- **Body posture:** Curled inward, head lowered, hunched
- **Breathing rhythm:** Slow, uneven — occasional "sigh" visible
- **Idle intensity:** Near-still, slow blink, minimal movement

#### TIRED
- **Eyes:** Heavy-lidded, 30% open, slow blinking with struggle
- **Ears:** Relaxed, slightly drooped
- **Tail:** Limp, dragging, occasional slow flick
- **Body posture:** Swaying slightly, weight unstable, may stumble
- **Breathing rhythm:** Slow, deep yawns visible
- **Idle intensity:** Yawning loop, eye-rubbing paw gesture, nodding off

#### PLAYFUL
- **Eyes:** Wide, bright, pupils large — tracking-mode
- **Ears:** Fully forward, rigid — on-point tracking
- **Tail:** Rapid tip-flick, horizontal, hunting-mode position
- **Body posture:** Low crouch, haunches raised, weight forward — pounce-ready
- **Breathing rhythm:** Excited, rapid
- **Idle intensity:** High — wiggling haunches, sudden dashes, toy-batting

#### SICK
- **Eyes:** Dull, half-closed, watery appearance
- **Ears:** Flat, unresponsive
- **Tail:** Limp, barely moves
- **Body posture:** Hunched, low to ground, slight shiver animation
- **Breathing rhythm:** Labored, visible shivering
- **Idle intensity:** Near-zero — slow blinking, occasional weak cough

#### AFFECTIONATE (high trust state, 80%+)
- **Eyes:** Slow blink — the "cat kiss," pupils softened
- **Ears:** Forward, slightly inward — gentle
- **Tail:** Straight up, tip curled forward — the "cat smile" posture
- **Body posture:** Leaning toward player, head-bump ready pose
- **Breathing rhythm:** Steady, calm — audible purr implied
- **Idle intensity:** Gentle — slow rub along imaginary surface, slow blink loops

---

### 3.2 Emotion Mapping to Game Stats

```
hunger < 20%          → HUNGRY (overrides calm/happy)
happiness < 20%       → SAD
happiness < 10%       → ANGRY (after prolonged sad)
energy < 15%          → TIRED
health < 30%          → SICK (overrides most states)
trust > 80%           → AFFECTIONATE modifier (adds to current emotion)
mood high + energy high → PLAYFUL or HAPPY
all stats balanced    → CALM
recent interaction    → CURIOUS
```

Priority order (highest wins): SICK > ANGRY > SAD > HUNGRY > TIRED > PLAYFUL > HAPPY > CURIOUS > CALM

---

## 4. Idle Animation Loop System

### 4.1 Loop Selection Logic

The idle system picks the next loop from a weighted pool. Weights shift based on current emotion state and energy level.

```
Every 4–8 seconds (randomized), select next idle loop.
Current loop finishes its cycle before switching.
Transition duration: 0.3–0.5 seconds (blend/crossfade).
```

### 4.2 Idle Animation Catalogue

| ID | Name | Duration | Trigger weight modifiers |
|---|---|---|---|
| `idle_blink` | Slow double blink | 1.5 s | Base: 30%; Tired: +40% |
| `idle_ear_twitch` | One ear flicks twice | 1.0 s | Curious: +30%; Calm: +20% |
| `idle_tail_wave` | Tail arcs side to side | 2.0 s | Happy: +30%; Playful: +20% |
| `idle_stretch` | Full-body stretch (front paws extended) | 2.5 s | After sleep: +60%; Low energy: -30% |
| `idle_yawn` | Mouth open, tongue curl, eye squeeze | 2.0 s | Tired: +60%; After long idle: +20% |
| `idle_groom` | Paw lick → face rub (3-stroke) | 3.5 s | After eating: +50%; Calm: +30% |
| `idle_look_player` | Head turns, makes eye contact, holds 1 sec | 2.0 s | Affectionate: +50%; Curious: +30% |
| `idle_lie_roll` | Flops to side, rolls belly-up briefly | 3.0 s | Playful: +40%; Happy: +30%; Angry: -100% |
| `idle_paw_tap` | One paw taps ground twice | 1.5 s | Playful: +40%; Hungry: +20% |
| `idle_sniff` | Nose-forward sniff sequence | 1.5 s | Hungry: +50%; Curious: +30% |
| `idle_shiver` | Slight body tremor | 1.0 s | Sick: +80% only |
| `idle_look_away` | Turns body 45° away, ignores | 2.0 s | Angry: +60%; Independent personality: +30% |
| `idle_headbump_air` | Butts head at empty air toward player | 1.5 s | Affectionate: +70%; Trust > 90% |

### 4.3 Energy-Based Idle Dampening

```
Energy 100–60%   → Full idle pool available
Energy 60–30%    → Slow-movement idles only; stretch, yawn, look player
Energy 30–10%    → Yawn, blink, lie_roll only
Energy < 10%     → Auto-transition to sleep animation
```

---

## 5. Touch Interaction System

### 5.1 Touch Zones

The cat sprite is divided into hitbox regions. Proportions are relative to the sprite's bounding box:

```
┌──────────────────────────────┐
│     [HEAD ZONE — 25%]        │  y: 0%–25% of sprite height
│  [BACK ZONE] [BELLY ZONE]    │  y: 25%–60%; left half = back, right half = belly
│     [PAWS ZONE — lower]      │  y: 60%–80%
│       [TAIL ZONE]            │  y: 80%–100%, extended right
└──────────────────────────────┘
```

### 5.2 Touch Response Matrix

#### HEAD ZONE
| Condition | Immediate micro-reaction | Full animation | Stat effect |
|---|---|---|---|
| Any trust level | Ear flick toward finger | `pet_head` — lean into touch, eye-close | +trust 1.5, +happiness 15 |
| Trust > 60% | Purr starts (frame 3) | Head-bump follow animation | +trust 2.0, +happiness 20 |
| Trust > 80% | Slow blink returned | `headbump` full sequence | +trust 3.0, +happiness 25 |

**`pet_head` sequence:**
1. Frame 0.0s: Ear nearest finger twitches toward it
2. Frame 0.1s: Head tilts 10° toward finger
3. Frame 0.3s: Eyes half-close (satisfaction)
4. Frame 0.5s: Full lean — head presses into virtual finger
5. Frame 0.8s: Purr loop begins (tail slow sway starts)
6. Frame 2.0s+: Loop hold until touch released
7. Release: Slow return to upright, eyes re-open, content expression

---

#### BACK ZONE
| Condition | Micro-reaction | Full animation | Stat effect |
|---|---|---|---|
| Trust > 20% | Tail lifts slightly | `pet_back` — back arch, slow lean | +happiness 10, +trust 0.5 |
| Trust < 20% | Slight step away | Side-step + look-back | No positive effect |
| Prolonged (>3s) | Tail raises full | `pet_back_long` — slow walk while being petted | +happiness 15, +trust 1.0 |

**`pet_back` sequence:**
1. Frame 0.0s: Spine visible rise (back arches slightly upward into touch)
2. Frame 0.2s: Tail lifts to vertical
3. Frame 0.4s: Slow forward lean
4. Frame 0.6s+: Slow walking-in-place loop (kneading), tail upright and curled

---

#### BELLY ZONE
| Condition | Micro-reaction | Full animation | Stat effect |
|---|---|---|---|
| Trust > 70% | Paws extend upward | `belly_happy` — exposed belly wiggle | +happiness 25, +trust 2.0 |
| Trust 40–70% | Tense twitch | `belly_neutral` — brief tolerance, then side-roll away | +happiness 5 |
| Trust < 40% | Instant recoil | `belly_attack` — paws grab finger, mock bite | -happiness 5, -trust 0.5 |

**`belly_attack` sequence (defensive grab):**
1. Frame 0.0s: Both back paws kick up
2. Frame 0.1s: Front paws grab (wrap around imaginary finger)
3. Frame 0.3s: Mock bite animation
4. Frame 0.5s: Release + roll away from player
5. Frame 0.8s: Sit up, ears back, irritated expression

---

#### TAIL ZONE
| Condition | Micro-reaction | Full animation | Stat effect |
|---|---|---|---|
| Always | Tail whip away from finger | `tail_annoy` — turn body away, flick tail | -happiness 5 |
| Anger state | Fast paw swipe at finger | `tail_swipe` — spinning irritation | -happiness 10 |
| Playful state | Tail chase play | `tail_play` — spins chasing own tail | +happiness 10 |

---

#### PAWS ZONE
| Condition | Micro-reaction | Full animation | Stat effect |
|---|---|---|---|
| Any | Paw lifts toward finger | `paw_highfive` — holds paw up | +happiness 10, +trust 1.0 |
| Playful state | Grab-and-hold | `paw_grab` — both paws wrap around finger area | +happiness 20 |
| Learned trick: "High Five" | Full trick animation | `trick_highfive` | +trust 2.0, +xp 10 |

---

### 5.3 Gesture Recognition

| Gesture | Detection | Cat Response |
|---|---|---|
| Single tap | Touch < 0.2s | Quick ear flick + eye-look at tap location |
| Long press (>1s) | Sustained single touch | Initiates zone-specific petting sequence |
| Rapid taps (>3 in 1s) | Multiple fast touches | Cat gets startled/annoyed: ears back, step away |
| Swipe across body | Moving touch | Ruffled fur particle, mild annoy |
| Gentle swipe (slow) | Slow moving touch | Petting-equivalent if on valid zone |
| Two-finger spread (zoom-like) | Dual touch expanding | Cat flattens (defensive), no positive effect |

---

## 6. Action Animation Sequences

### 6.1 FEEDING

**Trigger:** Player presses "Feed" button

```
Phase 1 — NOTICE (0.0–0.5s)
  - Cat head snaps toward food bowl
  - Nose extends forward — sniff animation
  - Pupils dilate (excitement)
  - Ears fully forward

Phase 2 — APPROACH (0.5–1.5s)
  - Walk or trot toward bowl (depends on hunger level)
  - Hunger > 50%: slow walk
  - Hunger < 30%: fast trot
  - Hunger < 10%: desperate run

Phase 3 — EAT LOOP (1.5–5.0s)
  - Head lowers into bowl
  - Rhythmic eating bob (head up-down, 2 beats/sec)
  - Tail curls upward and sways during eating
  - Ear flicks occasionally (contentment)

Phase 4 — SATISFACTION (5.0–7.0s)
  - Head raises, eyes close briefly
  - Sits upright
  - Paw-lick sequence: lick paw × 3, rub face × 2
  - Slow blink
  - Belly visually expands slightly (subtle morph)

Phase 5 — RETURN (7.0–8.0s)
  - Walk back to center
  - Settles into CALM or HAPPY idle
```

**Particle effects:** Food particle puffs during eating phase. Heart particle (+1) on satisfaction phase entry.

---

### 6.2 PLAY ACTION

**Trigger:** Player presses "Play" button

```
Phase 1 — ACTIVATION (0.0–0.5s)
  - Pupils dilate instantly to maximum
  - Haunches lower (pre-pounce position)
  - Tail switches to rapid horizontal flick
  - Ears rigid forward

Phase 2 — CHASE (0.5–3.0s)
  Vary per play type:
  ├─ Ball: Run left-right, bat with paw, follow physics arc
  ├─ Feather: Jump upward, swipe mid-air, spin landing
  └─ General: Zigzag run pattern, sudden stops, crouch-stalk

Phase 3 — PEAK EXCITEMENT (3.0–5.0s)
  - Maximum speed movement
  - Occasional "happy run" — tail up, bouncy gait
  - Playful vocalization implied (silent meow animation)

Phase 4 — WIND DOWN (5.0–7.0s)
  - Panting animation (mouth slightly open, flank heave)
  - Slowing movement
  - Sit → flop transition

Phase 5 — REST (7.0–9.0s)
  - Flop onto side
  - Heavy breathing loop
  - Tail thumps ground slowly
  - Satisfied expression despite tired body
```

**Energy drain visual:** As energy drops during play, movement speed and jump height visibly decrease.

---

### 6.3 CLEANING / WASH

**Trigger:** Player presses "Wash" button

**Pre-condition visual:** If cleanliness < 40%, cat has dirt/mess particles floating around it.

```
Phase 1 — RESISTANCE (0.0–1.5s)
  [If trust < 50%]
  - Cat backs away one step
  - Ears flatten
  - Irritated expression
  - Reluctant approach animation (slow, low body)

  [If trust > 50%]
  - Resigned sit
  - Ears back but body still

Phase 2 — WASHING (1.5–5.0s)
  - Scrubbing animation (body sway side to side)
  - Bubble particles appear
  - Eyes squeezed shut — misery expression
  - Occasional protest paw-swipe at nothing

Phase 3 — WATER SHAKE (5.0–6.0s)
  - Rapid full-body shake (high-frequency oscillation)
  - Water droplet particles fly outward
  - Ears flip out then back

Phase 4 — POST-WASH (6.0–8.0s)
  - Sitting up, slightly fluffed fur
  - Immediate self-grooming sequence (reclaiming dignity)
  - Gradual expression shift from annoyed → calm → slight satisfaction
  - Dirt particles removed; subtle clean sparkle
```

---

### 6.4 SLEEP

**Trigger:** Player presses "Sleep" button, or energy < 10%

```
Phase 1 — FIND SPOT (0.0–2.0s)
  - Cat walks slowly in small circle
  - Sniffs ground area
  - Kneading animation if on soft surface (cushion present)

Phase 2 — LIE DOWN (2.0–3.5s)
  - Front legs fold first
  - Body lowers
  - Back legs fold
  - Tail wraps around body
  - Head lowers onto paws

Phase 3 — SETTLING (3.5–5.0s)
  - Two adjustments (repositioning)
  - Eyes slowly close (heavy lid animation)
  - Breathing slows visibly

Phase 4 — DEEP SLEEP LOOP (5.0s+)
  - Very slow chest rise (1 breath per 3s)
  - Occasional ear twitch
  - Tail tip flick every 8–12s
  - Whisker twitch
  - "Z" particle every 15s (subtle, small)

UI: Screen edges darken slightly. Action buttons except Wake become semi-transparent.
```

---

### 6.5 TRAINING / TRICKS

**Trigger:** Player selects trick from teach dialog

```
Phase 1 — ATTENTION (0.0–1.0s)
  - Cat sits upright immediately
  - Head tilts — curious expression
  - Ears fully forward
  - Eye tracking follows player's implied gesture

Phase 2 — ATTEMPT (1.0–3.0s)
  - Animation specific to trick:
  ├─ SIT: Already sitting → sits even straighter, paw tap
  ├─ HIGH FIVE: Paw slowly raises (hesitant first time)
  ├─ ROLL OVER: Flops to side, rolls, returns upright
  ├─ SPIN: Short rotation animation, slight wobble
  ├─ FETCH: Runs to edge, returns with invisible item
  └─ WAVE: Paw wave — multiple if trust high

Phase 3 — OUTCOME (3.0–5.0s)
  Success (trust met):
  - Happy bounce (small jump)
  - Spin animation
  - Eyes wide and bright
  - Particle burst: stars + hearts

  Failure (trust insufficient):
  - Head tilt + confused blink
  - Sitting back down
  - Paw scratches head
```

---

## 7. Micro-Reaction System

Micro-reactions are **immediate** (< 0.1s), never blocking, and overlay any current animation.

### 7.1 Screen Touch Reactions

| Trigger | Cat Response | Duration |
|---|---|---|
| Single tap anywhere | Eyes look toward tap location | 0.8s hold + return |
| Tap near cat face | Quick blink + ear point at finger | 0.5s |
| Tap 3× in 2s | Ears flatten, annoyed look | 1.0s |
| Tap 6× in 3s | Full annoy — turn away, tail flick | 2.0s |
| No touch for 60s | Cat starts looking away | Gradual 10s |
| No touch for 180s | Cat yawns and begins lying down | Transition |
| No touch for 300s | Cat asleep | Sleep loop |

### 7.2 UI Button Press Reactions

| Button | Pre-animation micro-reaction |
|---|---|
| Feed button | Ears perk, nose lifts, sniff |
| Play button | Pupils dilate, crouch twitch |
| Sleep button | Yawn begins immediately |
| Wash button | Ears flatten slightly |
| Pet button | Head tilts toward player |

### 7.3 Passive Environmental Reactions

| Environment event | Cat reaction |
|---|---|
| Notification arrives | Ear twitch, brief look up |
| App comes to foreground | Cat looks at player, slow blink |
| Achievement unlocked | Excited bounce + spin |
| Event triggers | Start animation for event type |
| Room item placed | Cat sniffs new object, investigates |
| Long inactivity (hours) | Moves to sleeping spot autonomously |

---

## 8. Emotional Transitions

### 8.1 Transition Graph

```
                    ┌──────────────────────────────┐
                    │  TRANSITION RULES             │
                    │                               │
SICK ──────────────►│ → TIRED → CALM (after treat)  │
HUNGRY ────────────►│ → ANGRY → SAD → CALM (fed)    │
SAD ───────────────►│ → CALM → HAPPY (care)          │
ANGRY ─────────────►│ → SAD → NEUTRAL (time)         │
TIRED ─────────────►│ → CALM (after sleep)           │
PLAYFUL ───────────►│ → TIRED → CALM (after play)    │
CALM ──────────────►│ → HAPPY (positive action)       │
HAPPY ─────────────►│ → AFFECTIONATE (sustained care) │
                    └──────────────────────────────┘
```

### 8.2 Transition Animation Timing

Every emotion change goes through a **0.5-second morph phase**:

```
Frame 0.0s  → Current expression holds
Frame 0.1s  → Ears begin moving to new position
Frame 0.2s  → Body posture begins shifting
Frame 0.3s  → Eyes begin changing shape
Frame 0.4s  → Tail behavior switches
Frame 0.5s  → Full new emotion state reached
```

Never jump-cut between emotions. The morph must always be visible.

### 8.3 Key Transition Examples

**HUNGRY → ANGRY (prolonged hunger)**
1. Ear droop deepens to full-back
2. Eyes narrow progressively
3. Tail lash speed increases from slow to aggressive
4. Body posture shifts from slouch to tense arch
5. Duration of transition: 3 seconds

**ANGRY → SAD (after being ignored)**
1. Arched back relaxes
2. Ears go from flat-back to drooped-down
3. Tail lash slows to dragging
4. Eyes shift from slit to downcast
5. Head lowers
6. Duration: 4 seconds

**SAD → CALM (after feeding/care)**
1. Tail rises from floor
2. Head lifts
3. Eyes slowly open more
4. Ear position rises
5. One slow blink
6. Duration: 3 seconds

**CALM → AFFECTIONATE (sustained trust building)**
1. Cat moves slightly toward player
2. Slow blink begins
3. Tail rises to full vertical
4. Head-bump pose begins
5. Purr implied throughout
6. Duration: 5 seconds (gradual)

---

## 9. Movement System

### 9.1 Movement Gaits

| Gait | Speed (sprite-widths/sec) | Trigger | Visual character |
|---|---|---|---|
| `walk_slow` | 0.5 | Low energy, calm state | Even weight, slight waddle |
| `walk_casual` | 1.0 | Normal; approaching objects | Confident, tail up |
| `trot` | 2.0 | Interested; going to food | Bouncy, head bobbing |
| `run` | 4.0 | Playful, excited | Elongated body, ears back |
| `sprint` | 6.0 | Mini-game; high excitement | Full stretch, low profile |
| `sneak` | 0.8 | Hungry/curious; stalking | Low body, slow deliberate steps |
| `jump` | Vertical 3 body-heights | Action trigger | Pre-crouch → explosion → arc → land |
| `flop` | N/A | Tired, sad | Side-fall with thud, stays down |
| `stumble` | 0.4 | Energy < 10% | Irregular, almost-trip gait |

### 9.2 Gait Selection Logic

```
ENERGY determines gait speed cap:
  Energy > 80%  → Any gait available
  Energy 50–80% → Max: trot
  Energy 20–50% → Max: walk_casual
  Energy < 20%  → walk_slow only; stumble replaces run

MOOD modifies gait style:
  Happy/Playful → Bouncy variants of all gaits
  Sad/Sick      → Heavy, low-energy variants of all gaits
  Angry         → Stiff, deliberate variants
  Affectionate  → Slow, graceful variants

TRUST modifies response delay:
  Trust > 80%   → Instant movement response
  Trust 40–80%  → 0.2s hesitation before movement
  Trust < 40%   → 0.5s hesitation, may step away instead
```

### 9.3 Jump Physics

```
Pre-jump crouch:  3 frames (haunches lower, tail back)
Launch:           1 frame (full body extension)
Arc:              Parabolic path, 6–10 frames depending on distance
Land:             2 frames (front paws contact, back legs follow)
Land wobble:      1 frame (slight body bounce on landing)
```

---

## 10. Environment Interaction System

### 10.1 Object Interaction Catalogue

| Room object | Interaction animation | Trigger condition |
|---|---|---|
| **Cat cushion** | Kneading → curling → sleep | Energy < 50% or sleep action |
| **Scratcher post** | Front paw scratch × 5, arch back, satisfaction stretch | Idle; cleanliness not affecting this |
| **Cat tree** | Climb animation, perch at top, survey pose | Playful or curious state |
| **Window perch** | Jump up, settle, watch-outside idle | Calm or curious state |
| **Yarn ball** | Pat, roll, chase, bat, carry in mouth | Playful state; idle |
| **Toy mouse** | Stalk, pounce, bite, toss, repeat | Playful state |
| **Feather wand** | Jump-catch, mid-air swipe | Playful, energy > 40% |
| **Fish tank** | Sit beside, watch fish, paw-at-glass | Curious, calm |
| **Plant** | Sniff, sit beside, occasionally nibble | Curious; random idle |
| **Food bowl (empty)** | Sniff, look at player, sit and stare | Hunger < 40% |

### 10.2 Autonomous Object Approach

```
When cat is idle and energy > 30%:
  Every 30–90 seconds: select a random placed room object
  Walk toward it using walk_casual gait
  Execute object's interaction animation
  Return to previous position or stay near object
```

### 10.3 Wallpaper / Room Color Mood Effect

| Wallpaper | Passive idle effect |
|---|---|
| Sunset (#FFD6A5) | Warmer expression, slower tail sway, calm bias |
| Starry Night (#1A1A3E) | Slightly sleepier, yawn frequency +20% |
| Garden (#D4EDDA) | More active idle, curious bias, plant-sniff more frequent |
| Ocean (#CCE5FF) | Calmer, slow-blink frequency +30%, sitting poses preferred |

---

## 11. Camera & UI Feedback Layer

### 11.1 Camera Behavior

| Event | Camera behavior | Duration |
|---|---|---|
| Cat walks to edge | Gentle pan to follow (spring-follow, lag 0.3s) | Continuous |
| Jump | Quick zoom-out 10% during arc | Arc duration |
| Landing | Brief shake: amplitude 4px, frequency 20hz | 0.2s |
| Emotional event (sad, angry) | Slow zoom-in 5% | 1.0s ease-in |
| Achievement unlocked | Zoom-out 15% → zoom-in 5% | 0.5s + 0.5s |
| Sleep | Very slow zoom-in 5% | 3.0s ease-in, holds |
| Death event | Slow dolly back, desaturate | 2.0s |

All camera movements use ease-in-out curves. Maximum shake amplitude: 8px.

### 11.2 Particle Effects

| Event | Particle | Color | Duration | Count |
|---|---|---|---|---|
| Trust increase | Floating hearts ♥ | #FF6B8A | 1.5s | 1–3 |
| High happiness | Stars ★ burst | #FFD700 | 1.0s | 5–8 |
| XP gain | +XP number floats up | #FFA500 | 1.0s | 1 |
| Achievement unlock | Star burst + glow ring | Gold | 2.0s | 10–15 |
| Sadness | Falling droplet | #6BAED6 | 2.0s | 2–4 |
| Sickness | Swirling sick spirals | #88CC44 | Continuous | 3–5 |
| After wash | Clean sparkle ✦ | #FFFFFF | 1.5s | 6–10 |
| Death | Dark particles fall | #333333 | 3.0s | 20–30 |
| Sleep | Small Z floats | #CCCCFF | Every 15s | 1 |
| Event trigger | Exclamation pop ! | #FF8800 | 0.8s | 1 |

### 11.3 Screen Overlay Effects

| State | Screen overlay | Opacity |
|---|---|---|
| Sleeping | Dark vignette fade from edges | 40% |
| Sick | Slight desaturation + warm tint | 20% |
| Angry | Brief red flash pulse | 10%, 0.3s |
| Death | Full desaturate → fade to black | 0–100%, 3s |
| Achievement | Brief golden glow | 30%, 0.5s |
| High happiness | Soft warm bloom | 15%, continuous |

### 11.4 Action Button Visual Feedback

| Condition | Button appearance |
|---|---|
| Unavailable (cat running away, dead) | 40% opacity, no press animation |
| Low stat (e.g., hunger < 20%) | Pulsing glow on Feed button |
| Cat asleep | Sleep button shows "Wake" text, others dim |
| Trust too low for trick | Trick button has lock icon |
| Event active (sick) | Medicine button pulses red |

---

## 12. Progression Visual Evolution

### 12.1 Growth Stage Appearance Changes

#### KITTEN (Level 1–4)
- **Body:** Round, compact; head proportionally large (40% of body height)
- **Eyes:** Very large relative to face — 35% of face width; round pupils
- **Limbs:** Short, stubby; wide paws relative to body
- **Tail:** Short, thin, held low (still developing)
- **Movement:** Clumsy; stumble animation plays occasionally even at full energy
- **Ear size:** Large relative to head — floppy at tips
- **Animation speed:** Slightly slower, more hesitant

#### TEEN (Level 5–14)
- **Body:** Limbs elongate; begins to lose roundness
- **Eyes:** Still large but more oval; gaining expression range
- **Limbs:** Longer, slight awkward gangly quality
- **Tail:** Medium length, increasingly confident sway
- **Movement:** Occasional burst of energy → stop (inconsistent); personality begins affecting gait
- **Ear size:** Proportionate, upright fully now
- **Animation speed:** Normal; personality modifiers begin applying

#### ADULT (Level 15–29)
- **Body:** Full proportions achieved; defined muscle suggestion in shoulders
- **Eyes:** Refined, expressive; full emotion range visible
- **Limbs:** Confident, controlled
- **Tail:** Full length, expressive; reads personality clearly
- **Movement:** Fluid, intentional; gait fully reflects personality
- **Fur:** Stripe detail more visible; slight sheen
- **Animation speed:** Full speed; fastest reactions

#### SENIOR (Level 30+)
- **Body:** Slight softening of posture; sits more often
- **Eyes:** Softer expression default; slightly narrower; wisdom look
- **Limbs:** Slower, deliberate; no running gait unless excited
- **Tail:** Moves less frequently; carries lower
- **Movement:** walk_slow becomes default; trot maximum unless very excited
- **Fur:** Slightly grayer around muzzle; fluffier chest
- **Animation speed:** 80% of adult; pause before movements (+0.3s delay)
- **Special:** Memory animations — occasional nostalgic poses, looking at old items

### 12.2 Trust Level Visual Modifiers

| Trust % | Eye expression | Tail confidence | Animation delay | Response to touch |
|---|---|---|---|---|
| 0–20% | Wary; narrow; avoids eye contact | Low, uncertain | +0.5s | Backs away |
| 20–40% | Neutral; looks occasionally | Neutral sway | +0.3s | Tolerates |
| 40–60% | Interested; holds eye contact | Mid-height sway | +0.1s | Leans toward |
| 60–80% | Warm; slow blink available | High confident sway | No delay | Seeks touch |
| 80–100% | Soft; constant slow blink; love-eyes | Fully upright, curl tip | Anticipatory | Initiates contact |

### 12.3 Personality Visual Signature

| Personality | Movement modifier | Idle bias | Touch response |
|---|---|---|---|
| AFFECTIONATE | Leans toward player | `idle_headbump_air`, `idle_look_player` | Always positive |
| PLAYFUL | Bouncy gait variant; extra pounce twitch | `idle_paw_tap`, `idle_lie_roll` | Grabs at finger |
| INDEPENDENT | Slight delay before responding | `idle_look_away`, self-groom heavy | Tolerates briefly, moves away |
| STUBBORN | Resists animations slightly (slower transitions) | Sits facing away | Back-turns after 2s pet |
| LAZY | Everything at 70% speed | `idle_yawn` heavy; prefers lying poses | Barely reacts |
| NEUTRAL | Standard | Even distribution | Standard matrix |

---

## 13. Audio Layer

### 13.1 Vocalization Map

| Sound | Trigger | Pitch modifier |
|---|---|---|
| Purr loop | Trust > 60% + being petted | Higher pitch with higher trust |
| Soft meow | Hunger 30–60% | Normal |
| Loud meow | Hunger < 20% | Lower, more urgent |
| Chirp | Curious; sees something | High, short |
| Hiss | Belly attack; anger peak | Low, harsh |
| Trill (brrrp) | Affectionate greeting | High, musical |
| Yowl | Anger sustained | Low, loud |
| Sneeze | Sick state; random idle | Normal |
| Content sigh | After feeding satisfaction | Breathy, mid |
| Silent meow | Playful peak | (visual only — mouth opens, no sound) |

### 13.2 Ambient Sound Layers

| Room object present | Ambient sound added |
|---|---|
| Fish tank | Gentle water bubble loop |
| Window perch | Soft outdoor ambience (birds, breeze) |
| Cat tree | Faint scratch sounds during climb |
| Cushion (sleeping) | Slight white noise |

### 13.3 UI Sound Feedback

| Event | Sound |
|---|---|
| Button press | Soft pop |
| Achievement unlock | Ascending chime sequence |
| Stat warning (< 20%) | Soft pulse tone |
| Event trigger | Short meow + paper rustling |
| Death | Soft, mournful single piano note |

---

## 14. Asset List & Naming Convention

### 14.1 Naming Convention

```
{stage}_{action}_{frame}.png

stage:  kitten | teen | adult | senior
action: full action name from list below
frame:  001 → NNN (zero-padded)

Example: adult_eat_001.png, kitten_idle_blink_003.png
```

### 14.2 Required Animation Assets

#### IDLE ANIMATIONS (per stage × 4 stages)
```
idle_blink          8 frames
idle_ear_twitch     6 frames
idle_tail_wave      12 frames
idle_stretch        16 frames
idle_yawn           14 frames
idle_groom          20 frames
idle_look_player    10 frames
idle_lie_roll       18 frames
idle_paw_tap        8 frames
idle_sniff          10 frames
idle_shiver         6 frames  (sick only)
idle_look_away      8 frames
idle_headbump_air   10 frames
```

#### ACTION ANIMATIONS (per stage × 4 stages)
```
eat_notice          6 frames
eat_approach        8 frames
eat_loop            12 frames  (looping)
eat_satisfaction    16 frames

play_activate       4 frames
play_chase_ball     16 frames  (looping)
play_jump_catch     12 frames
play_winddown       10 frames
play_exhausted      8 frames

wash_resist         8 frames
wash_scrub          12 frames  (looping)
wash_shake          8 frames
wash_groom_after    14 frames

sleep_findspot      12 frames
sleep_liedown       16 frames
sleep_settling      8 frames
sleep_loop          20 frames  (looping, very slow)

trick_attention     6 frames
trick_highfive      12 frames
trick_rollover      16 frames
trick_spin          12 frames
trick_wave          10 frames
trick_success       8 frames
trick_confused      10 frames
```

#### EMOTION OVERLAYS (single stage-agnostic set, or per stage)
```
emotion_happy
emotion_calm
emotion_curious
emotion_hungry
emotion_angry
emotion_sad
emotion_tired
emotion_playful
emotion_sick
emotion_affectionate
```

#### TOUCH REACTIONS
```
pet_head            10 frames
pet_back            10 frames
belly_happy         12 frames
belly_neutral       8 frames
belly_attack        12 frames
tail_annoy          8 frames
tail_swipe          8 frames
tail_play           14 frames
paw_highfive        10 frames
paw_grab            10 frames
```

#### MOVEMENT (per stage)
```
walk_slow           8 frames  (looping)
walk_casual         8 frames  (looping)
trot                8 frames  (looping)
run                 8 frames  (looping)
sprint              8 frames  (looping)
sneak               10 frames (looping)
jump_launch         4 frames
jump_arc            6 frames
jump_land           4 frames
flop                8 frames
stumble_walk        10 frames (looping)
```

**Total estimated assets:** ~4 stages × ~60 animations × average 10 frames = **~2,400 sprite frames**

---

## 15. AI Asset Generation Prompts

Use these prompts with Midjourney, DALL-E 3, Stable Diffusion, or similar tools. Always maintain visual consistency by using the same style prompt as a prefix.

### 15.1 Style Prefix (include with every prompt)
```
"pixel art style, 2D game sprite, transparent background, 
orange tabby cat character, clean black outlines, 
warm color palette, mobile game aesthetic, 
chibi proportions, expressive face, no background, --ar 1:1"
```

### 15.2 Emotion State Sprites

**HAPPY**
```
[STYLE PREFIX] orange tabby cat sitting upright, 
wide open curved eyes with slight upward arc, ears fully upright 
tilted slightly outward, tail in high arc curling at tip, 
chest forward, content smile expression
```

**ANGRY**
```
[STYLE PREFIX] orange tabby cat arched back, ears fully flattened 
against skull pointing backward, narrow slit pupils, 
brow fur raised in aggressive V-shape, tail puffed and lashing sideways, 
weight on haunches
```

**SAD**
```
[STYLE PREFIX] orange tabby cat curled inward, head lowered 
resting on front paws, ears drooped down at 45 degrees pointing outward, 
eyes half-closed downcast with dilated round pupils, 
tail wrapped around body, hunched posture
```

**AFFECTIONATE**
```
[STYLE PREFIX] orange tabby cat in slow-blink pose, 
eyes 60% closed with soft curved shape, tail straight up with 
curled tip, leaning slightly forward, weight shifted toward viewer, 
head slightly tilted in invitation, subtle smile
```

**SICK**
```
[STYLE PREFIX] orange tabby cat huddled low, 
dull half-closed eyes, slightly watery, ears flat and unresponsive, 
tail limp and dragging, body slightly hunched with visible shiver posture, 
pale/dull coloring on face
```

### 15.3 Action Scene Sprites

**EATING**
```
[STYLE PREFIX] orange tabby cat head lowered into food bowl, 
rhythmic eating pose, tail curled upward in contentment, 
one ear flicked back, mouth at bowl level
```

**PLAYFUL POUNCE (pre-launch)**
```
[STYLE PREFIX] orange tabby cat in hunting crouch, 
haunches raised, front low, pupils fully dilated, 
ears rigid and forward, tail in horizontal rapid-flick position, 
weight shifted to back legs, intense focus
```

**SLEEPING CURL**
```
[STYLE PREFIX] orange tabby cat tightly curled in circle, 
tail wrapped around nose, eyes fully closed, 
visible slow chest rise, one ear slightly relaxed, 
peaceful expression, soft fur detail
```

**BELLY DEFENSIVE ROLL**
```
[STYLE PREFIX] orange tabby cat on back, 
all four paws in air, ears pinned back, 
mock-grabbing pose with front paws, open mouth showing teeth (playful), 
tail curled, slightly dramatic exaggerated expression
```

### 15.4 Growth Stage Comparison Sheet

**KITTEN**
```
[STYLE PREFIX] orange tabby kitten, very round body, 
oversized head (40% of total height), enormous round eyes, 
short stubby legs, tiny tail, large floppy-tipped ears, 
slightly clumsy sitting pose
```

**TEEN**
```
[STYLE PREFIX] orange tabby adolescent cat, 
slightly elongated body with awkward proportions, 
legs longer than body suggests, medium tail, 
more defined facial expression than kitten, 
ears fully upright, mix of kitten roundness and adult form
```

**ADULT**
```
[STYLE PREFIX] orange tabby adult cat, 
balanced proportions, confident posture, 
defined shoulder muscles suggested, full-length expressive tail, 
refined face with full emotion range, 
strong clear stripe pattern
```

**SENIOR**
```
[STYLE PREFIX] orange tabby senior cat, 
slightly softer posture, subtle gray around muzzle, 
fluffier chest and belly, wisdom expression in eyes, 
tail carried lower, dignified seated pose, 
gentler overall appearance than adult
```

---

## 16. Android Implementation Guide

> For developers working with the existing Imbir codebase (`com.imbir.game`).

### 16.1 Current Code Architecture

```
AnimationManager.kt         ← Handles sprite selection and display
  resolveDrawable()         ← Fallback chain: stage-specific → generic → cat_idle
  showMoodIdle()            ← Selects animation based on happiness + sick flag
  updateAnimationForAction() ← Plays action animation, schedules idle return

GameManager.kt              ← All state; emotion determined by stat thresholds
  state.getMoodEmoji()      ← Quick emotion indicator (extend to drive animations)
  tick()                    ← Called every 5s; update emotions here

HomeFragment.kt             ← UI; connects buttons to GameManager + AnimationManager
```

### 16.2 Implementing the Layered Animation System

The current system uses single `ImageView` resource swapping. For layered animations, upgrade path:

**Option A — Sprite Sheet + Custom View (Recommended)**
```kotlin
class CatAnimationView(ctx: Context) : View(ctx) {
    // Layer 1: idle base bitmap (current frame from loop)
    // Layer 2: action animation bitmap (null when idle)
    // Layer 3: emotion overlay bitmap (always drawn on top)

    override fun onDraw(canvas: Canvas) {
        baseLayer?.let  { canvas.drawBitmap(it, matrix, null) }
        actionLayer?.let { canvas.drawBitmap(it, matrix, null) }
        emotionLayer?.let { canvas.drawBitmap(it, matrix, null) }
    }
}
```

**Option B — Multiple ImageView layers (simpler, less efficient)**
```xml
<FrameLayout android:id="@+id/catContainer">
    <ImageView android:id="@+id/catBaseLayer" />     <!-- idle loop -->
    <ImageView android:id="@+id/catActionLayer" />   <!-- action anim -->
    <ImageView android:id="@+id/catEmotionLayer" />  <!-- emotion overlay -->
</FrameLayout>
```

**Option C — Lottie (if assets are vector animations)**
```gradle
implementation("com.airbnb.android:lottie:6.1.0")
```
```kotlin
val animationView = LottieAnimationView(context)
animationView.setAnimation("cat_adult_eat.json")
animationView.playAnimation()
```

### 16.3 Touch Zone Implementation

```kotlin
class CatTouchHandler(
    private val catBounds: RectF,  // cat sprite screen rect
    private val onZoneTouched: (TouchZone, MotionEvent.ACTION) -> Unit
) : View.OnTouchListener {

    enum class TouchZone { HEAD, BACK, BELLY, PAWS, TAIL }

    override fun onTouch(v: View, e: MotionEvent): Boolean {
        val relX = (e.x - catBounds.left) / catBounds.width()
        val relY = (e.y - catBounds.top) / catBounds.height()

        val zone = when {
            relY < 0.25f -> TouchZone.HEAD
            relY < 0.60f && relX < 0.50f -> TouchZone.BACK
            relY < 0.60f && relX >= 0.50f -> TouchZone.BELLY
            relY < 0.80f -> TouchZone.PAWS
            else -> TouchZone.TAIL
        }
        onZoneTouched(zone, e.action)
        return true
    }
}
```

### 16.4 Idle Loop Manager

```kotlin
class IdleLoopManager(
    private val getState: () -> CatState,
    private val playAnimation: (String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    fun start() {
        running = true
        scheduleNext()
    }

    fun stop() {
        running = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun scheduleNext() {
        val delay = (4000L..8000L).random()
        handler.postDelayed({
            if (!running) return@postDelayed
            val anim = selectIdleAnimation(getState())
            playAnimation(anim)
            scheduleNext()
        }, delay)
    }

    private fun selectIdleAnimation(state: CatState): String {
        val pool = buildWeightedPool(state)
        return pool.random()
    }

    private fun buildWeightedPool(state: CatState): List<String> {
        val pool = mutableListOf<String>()
        // Base weight 1 = appears once; increase for higher weight
        repeat(3) { pool.add("idle_blink") }
        repeat(2) { pool.add("idle_ear_twitch") }
        repeat(2) { pool.add("idle_tail_wave") }
        if (state.energy < 40) repeat(4) { pool.add("idle_yawn") }
        if (state.happiness > 70) repeat(3) { pool.add("idle_groom") }
        if (state.trust > 60) repeat(3) { pool.add("idle_look_player") }
        if (state.isSick) repeat(5) { pool.add("idle_shiver") }
        // ... extend with personality modifiers
        return pool
    }
}
```

### 16.5 Emotion → Animation Resolver

```kotlin
fun CatState.toIdleAnimation(): CatAnimation {
    return when {
        !isAlive       -> CatAnimation.DEATH
        isSick         -> CatAnimation.SICK
        isRunningAway  -> CatAnimation.WALK_RIGHT
        happiness < 10 -> CatAnimation.ANGRY
        happiness < 25 -> CatAnimation.SAD
        hunger < 15    -> CatAnimation.BEG
        energy < 15    -> CatAnimation.SLEEPY
        trust > 80 && happiness > 70 -> CatAnimation.LIE_BACK
        happiness > 80 -> CatAnimation.HAPPY
        personality == Personality.PLAYFUL -> CatAnimation.EXCITED
        else           -> CatAnimation.IDLE_BLINK
    }
}
```

### 16.6 Particle System (Lightweight)

For hearts, stars, and XP floaters, use a simple `Canvas`-based overlay:

```kotlin
data class Particle(
    var x: Float, var y: Float,
    val text: String,
    var alpha: Float = 1f,
    var vy: Float = -4f  // upward drift
)

class ParticleOverlayView(ctx: Context) : View(ctx) {
    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply { textSize = 48f; textAlign = Paint.Align.CENTER }

    fun emit(x: Float, y: Float, text: String) {
        particles.add(Particle(x, y, text))
        if (!isAnimating) startAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            paint.alpha = (p.alpha * 255).toInt()
            canvas.drawText(p.text, p.x, p.y, paint)
            p.y += p.vy
            p.alpha -= 0.03f
            if (p.alpha <= 0) iter.remove()
        }
        if (particles.isNotEmpty()) invalidate()
    }
}
```

---

*End of Imbir UI/UX & Animation System Specification v1.0*