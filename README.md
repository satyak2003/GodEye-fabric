# The GodEye 👁️
## **Nothing escapes the God's vision**

**The GodEye** is a cinematic, story-driven horror and boss-fight mod for Minecraft (Fabric 1.20.1). It transforms the standard Minecraft experience into a psychological slow-burn that culminates in a massive, custom-coded arena boss battle and a frantic dimension-collapsing escape.

---

## ⚠️ Health Advisory
**This mod contains intense visual and audio elements.**
Upon joining a world with this mod for the first time, players will receive a health warning. The sequence includes:
* Severe flashing lights and screen distortions.
* Sudden, loud jumpscares.
* Disorienting visual effects (Nausea, Darkness, Field of View warping).

*Note: For the intended cinematic experience, players MUST have their Master and Music volumes turned up to hear the custom BGM and AI-generated voice lines.*

<img width="1920" height="1080" alt="GODEYE_banner" src="https://github.com/user-attachments/assets/b29cabfc-05ee-42c7-9f0c-ea3e77932f36" />

---

## ✨ Features
* **Dynamic World Decay:** Watch the Overworld rot around you as the environment violently degrades in real-time.
* **Psychological Horror:** A custom stalker entity that watches from the edge of your vision, paired with UI glitches and chat distortions.
* **The Nightfall Staff:** A custom weapon that vacuums the souls of your enemies to charge a devastating, cinematic Orbital Strike.
* **Cinematic Dimensions:** Custom structures, dynamic arena generation, and a dimension that violently collapses beneath your feet.

### The Final Boss
**Defeat him to take back what's yours.**

<img width="1920" height="1009" alt="2026-07-23_16 04 25" src="https://github.com/user-attachments/assets/953daba8-aeda-4be6-ab29-2e6832d17fc3" />

---

## 🛠️ Developer Notes

### Custom Audio Setup
To fully utilize this mod, you must provide your own `.ogg` files in the `src/main/resources/assets/godeye/sounds/` directory:
* `jumpscare.ogg` - Custom jumpscare audio.
* `boss_chance.ogg` - Voice line: "Now is your chance..."
* `boss_run.ogg` - Voice line: "Run, use the portal..."
* `your_world.ogg` - Voice line: "This world is yours now."
* `bgm_domain.ogg` - Creepy exploration music.
* `bgm_boss.ogg` - Heavy boss combat music.

### Testing & Debugging
To bypass the cinematic build-up during development, toggle the debug mode inside `Godeye.java`:
```java
public static boolean DEBUG_MODE = true;
```

<img width="1080" height="100" alt="footer_git" src="https://github.com/user-attachments/assets/9ddf1553-7bb7-42a7-a2d8-199c79fb392b" />
