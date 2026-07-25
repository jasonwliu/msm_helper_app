# MapleStory M Helper App

A premium, feature-rich Android companion utility built with Jetpack Compose for MapleStory M players to track, calculate, and optimize their progression.

## Features

### 1. Necro Crystals & Stones Tracker
* **Daily Tracker**: Track your daily progress towards crystals and stones.
* **Character Overview**: Manage multiple characters and view status in one place.
* **Ready to Craft Status**: Visual indicator of whether you have enough materials to attempt a Necro upgrade.
* **Stone Maximizer**: Calculate the most optimal stone utilization pathways.
* **Necro Tracker**: Log Necro attempts and calculate success/failure statistics.

### 2. Mastercraft Tracker & Stats
* **Crafting Log**: Detailed mastercraft attempt tracking showing success/failure rates.
* **Scroll Selection**: Advanced scroll selector with inline count modifiers (add up to 2 scrolls of the same percentage for Necro/Inherit).
* **Mastercraft History Log**: Review past attempts showing gear type, scrolls used, outcome, and timestamp.
* **Smart Recalculation**: Delete incorrect entries directly from the log and watch your expected vs actual rates update automatically.

### 3. Boss Accessory Tracker
* **Drop Statistics**: Track drops with total runs, drop counts, and running drop rate percentage.
* **Quick Log**: Instant success/failure logging via primary action buttons.
* **Drop History**: Chronological view of recent runs.
* **Data Management**: Wipe tracking logs securely with validation popups.

### 4. Advanced Damage Calculator
* **Custom Presets**: Create, rename, switch, and delete multiple character/preset cards.
* **Comprehensive Stats Input**: Input base ATK, Skill %, DMG %, FD %, ATK %, Boss ATK %, Crit Dmg %, MDC, Boss Def %, IED %, Skill Mod %, and Arcane Force (AF).
* **Expandable Buffs Box**: Quick toggle for major buffs (Candy Basket, Chestnut, Fried Shrimp, Yogurt/Grape, Pork/Snail, Jellyfish, Boss Rush).
* **Custom Skill Modifiers list**: Add dynamic Skill DMG, FD, or IED modifiers (Node boost/Hyper boosts).
* **Arcane Force (AF) Scaling Tiers**: Accurate representation of MSM's AF system:
  * **Bonus Tiers**: Scaling from $\ge 1.1\text{x}$ up to $\ge 1.5\text{x}$ AF giving up to $+50\%$ ATK% and $+5.4\text{M}$ MDC.
  * **Penalty Tiers**: Under-requirement scaling starting at $50\%$ damage penalty and increasing by $10\%$ penalty for every $0.1$ drop, capping at $99\%$ penalty (under $0.5\text{x}$ ratio).
* **Multiplicative Individual IED Manager**: Input individual sources of Ignore Enemy Defense (IED) instead of a single total, compounding via the standard formula: $1 - \text{Product}(1 - S_i)$.
* **Compare Mode**: Toggle delta comparison to input increments (e.g., $+15\%$ FD, $+5,000$ ATK) and view side-by-side capped vs potential damage comparison.

### 5. System Sync & Backup
* **Local Backup**: Export and import your data securely as JSON files.
* **Google Drive Cloud Sync**: Link your Google Account to back up characters, presets, and history securely to your private AppData folder on Google Drive.

### 6. UX Polish
* **Tab Persistence**: Automatically remembers which tab you were on when switching apps or restarting, ensuring you resume right where you left off.
* **Form Enhancements**: Empty-field (hiding 0s) and decimal input formatting filters to make updating stats seamless.

---

## Technical Stack
* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3)
* **Architecture**: MVVM (Model-View-ViewModel) with Kotlin Coroutines and StateFlows
* **Serialization**: Kotlinx Serialization
* **Google Play Services**: Play Services Auth & Google Drive API Client

---

## Build and Installation
1. Clone this repository:
   ```bash
   git clone https://github.com/jasonwliu/msm_helper_app.git
   ```
2. Open the project in **Android Studio**.
3. To configure Google Cloud Sync, register your own OAuth client credential in the Google Cloud Console under your Package Name (`com.gcirl.msmhelper`) with your signing key's SHA-1 fingerprint.
4. Build and deploy to your Android device or emulator.
