# GodEye CH03 Finale Architecture Investigation

I have thoroughly audited the current CH03 state machine. Here are the precise findings detailing why the finale is not occurring as you expected.

### 1. CURRENT CH03 STATE MACHINE
1. Chapter03 starts and schedules CH03StartEncounterEvent.
2. CH03StartEncounterEvent spawns the initial Watcher and schedules CH03ObservationTickEvent to begin a 10-tick polling loop.
3. CH03ObservationTickEvent loops indefinitely. It tracks if the player is looking at the Watcher.
4. If the player looks away after observing, there is a 10% chance it schedules CH03WatcherRelocateEvent.
5. CH03WatcherRelocateEvent teleports the Watcher to a new hidden spot, sets ch03_has_relocated_once = true, and resumes the observation loop.
6. The loop is ONLY broken if the player physically walks within 8 blocks of the Watcher. This triggers CH03WatcherApproachEvent.
7. CH03WatcherApproachEvent deletes the Watcher. If a progression flag is met, it schedules CH03FinaleStartEvent. Otherwise, it sets ch03_has_approached_once and restarts the encounter from step 2.

### 2. NORMAL WATCHER ENCOUNTER FLOW
A single normal encounter consists of the Watcher spawning, the player spotting it, and the player turning away causing it to relocate to a new spot. This same continuous "encounter" persists until the player physically approaches the entity.

### 3. WATCHER OBSERVATION LOOP
The CH03ObservationTickEvent is a recursive, infinite loop. It checks distances and line-of-sight, then explicitly reschedules itself 10 ticks into the future. **There is no finite stop condition based on time or number of observations.** The loop will literally run forever as long as the player stays further than 8 blocks away from the Watcher.

### 4. WATCHER RELOCATION LOOP
When the Watcher relocates via CH03WatcherRelocateEvent, it finds a new position, teleports the Watcher, and instantly resumes the CH03ObservationTickEvent loop. It does not advance the chapter state other than setting a single boolean flag (ch03_has_relocated_once). The Watcher can relocate an infinite number of times.

### 5. CURRENT ENCOUNTER COUNT / LIMIT
**There is no integer encounter counter.**
The code does not track how many times the Watcher has relocated or been seen. The code contains a dead comment (int obsCount = pState.getFlag("ch03_obs_count_int") ? 1 : 0; // Using boolean flag as hacky int?), but no integer incrementing actually exists. 
The current code strictly does: observe -> relocate -> observe -> relocate -> repeat forever. It does not automatically end after N encounters.

### 6. CURRENT CH03 COMPLETION CONDITION
Chapter03.isComplete() returns 	rue exclusively when PlayerStoryState.getState(player).getFlag("ch03_completed") == true.
This flag is **only** set inside CH03FinaleCinematicEvent.java under the 	eleport payload case (which executes at the very end of the final cinematic).

### 7. CH03EndEvent FLOW
CH03EndEvent.java still exists in the codebase and is registered in Godeye.java, but **it is completely orphaned**. It is never scheduled by any event in the current architecture. It was replaced by CH03FinaleStartEvent.

### 8. FINAL WATCHER GROUP FLOW
The final group is spawned by CH03FinaleStartEvent.java.
- **Scheduled by**: CH03WatcherApproachEvent ONLY.
- **Condition to schedule**: The player must approach within 8 blocks of the Watcher, AND (ch03_has_relocated_once == true OR ch03_has_approached_once == true).
- **Execution**: When it runs, it spawns 7-9 Watchers, plays the custom music, and queues the 5 cinematic CH03FinaleCinematicEvent sequences.

### 9. CINEMATIC FLOW
Scheduled natively by CH03FinaleStartEvent using the EventScheduler with increasing delays:
- + 100 ticks: Sends BlackScreenPayload to client.
- + 140 ticks: Native Title packet: "you had all the time to run"
- + 240 ticks: Native Title packet: "But you did not"
- + 340 ticks: Native Title packet: "I see you now"
- + 440 ticks: Removes black screen, clears title, teleports to Plains village, and sets ch03_completed = true.

### 10. FAST MODE EFFECT ON CH03
Running /godeye debug chapter ch03 fast will compress the currently pending CH03ObservationTickEvent (shrinking its 10-tick delay). However, when that event executes, it immediately schedules a **brand new** event with a normal 10-tick delay. Because FAST mode only compresses events that are *already in the queue at the moment the command is run*, the observation loop instantly returns to normal speed. FAST mode can never reach the finale because FAST mode cannot force the player's physical avatar to walk within 8 blocks of the Watcher.

### 11. WHY THE FINALE DID NOT OCCUR
You were waiting for the chapter to progress automatically after a certain number of sightings. However, because there is no finite encounter counter, the observation/relocation loop will continue until the end of time. The only exit node in the entire state machine is CH03WatcherApproachEvent, which strictly requires you to physically run up and touch the Watcher. If you just watched it from a distance, the state machine was permanently deadlocked.

### 12. WHETHER THE FINALE IS CURRENTLY REACHABLE
**CONDITIONALLY.**
The exact condition that must become true is: The player must physically walk their character to within 8 blocks of the Watcher, and they must do this *after* the Watcher has already relocated at least once (or they must do it a second time).

### 13. ALL CH03 STATE FLAGS/COUNTERS
- ch03_initialized *(Global)*: Written by Chapter03.onStart(). Prevents double initialization.
- ch03_started *(Player)*: Written by Chapter03.startPlayerSequence().
- ch03_watcher_seen *(Player)*: Written by CH03ObservationTickEvent (true) and CH03WatcherRelocateEvent (false). Tracks if the player has looked at the current Watcher location.
- ch03_has_relocated_once *(Player)*: Written by CH03WatcherRelocateEvent. Progression milestone.
- ch03_has_approached_once *(Player)*: Written by CH03WatcherApproachEvent. Progression milestone.
- ch03_completed *(Player)*: Written by CH03FinaleCinematicEvent. Checked by Chapter03.isComplete().
- ch03_obs_count_int *(Player)*: Referenced in a dead comment. Does not actually exist or function.

### 14. RECOMMENDED ARCHITECTURAL CHANGE — NO CODE
Your proposed design is structurally perfect for the existing architecture. 
To implement it, we would:
1. Actually utilize an integer counter in PlayerStoryState (e.g., ch03_encounter_count).
2. Increment this counter inside CH03WatcherRelocateEvent.
3. In CH03ObservationTickEvent, check if ch03_encounter_count >= 3. If true, gracefully terminate the observation loop and schedule CH03FinaleStartEvent, completely removing the mandatory "approach" requirement.
