package com.gcirl.msmhelper.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.gcirl.msmhelper.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.gcirl.msmhelper.sync.GoogleDriveSyncManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MSMHelperViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("msm_helper_prefs", Context.MODE_PRIVATE)
    val googleDriveSyncManager = GoogleDriveSyncManager(application)

    private val _accounts = MutableStateFlow<List<com.gcirl.msmhelper.data.Account>>(emptyList())
    val accounts: StateFlow<List<com.gcirl.msmhelper.data.Account>> = _accounts.asStateFlow()

    private val _activeAccountIndex = MutableStateFlow(0)
    val activeAccountIndex: StateFlow<Int> = _activeAccountIndex.asStateFlow()

    // Main states
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _activeCharIndex = MutableStateFlow(0)
    val activeCharIndex: StateFlow<Int> = _activeCharIndex.asStateFlow()

    private val _lastResetDate = MutableStateFlow("")
    val lastResetDate: StateFlow<String> = _lastResetDate.asStateFlow()

    private val _necroHistory = MutableStateFlow<List<NecroAction>>(emptyList())
    val necroHistory: StateFlow<List<NecroAction>> = _necroHistory.asStateFlow()

    private val _mastercraftHistory = MutableStateFlow<List<MastercraftAttempt>>(emptyList())
    val mastercraftHistory: StateFlow<List<MastercraftAttempt>> = _mastercraftHistory.asStateFlow()

    private val _bossAccessoryHistory = MutableStateFlow<List<BossAccessoryAttempt>>(emptyList())
    val bossAccessoryHistory: StateFlow<List<BossAccessoryAttempt>> = _bossAccessoryHistory.asStateFlow()

    private val _calcPresets = MutableStateFlow<List<CalcPreset>>(listOf(CalcPreset(name = "Default Preset")))
    val calcPresets: StateFlow<List<CalcPreset>> = _calcPresets.asStateFlow()

    private val _activePresetIndex = MutableStateFlow(0)
    val activePresetIndex: StateFlow<Int> = _activePresetIndex.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun setSelectedTab(value: Int) {
        _selectedTab.value = value
        saveData()
    }

    // App Settings
    private val _nextCharStoneMode = MutableStateFlow("armor") // "armor", "weapon", "keep"
    val nextCharStoneMode: StateFlow<String> = _nextCharStoneMode.asStateFlow()

    private val _reorderModeEnabled = MutableStateFlow(false)
    val reorderModeEnabled: StateFlow<Boolean> = _reorderModeEnabled.asStateFlow()

    fun setNextCharStoneMode(mode: String) {
        if (mode in listOf("armor", "weapon", "keep")) {
            _nextCharStoneMode.value = mode
            sharedPrefs.edit().putString("next_char_stone_mode", mode).apply()
            saveData()
        }
    }

    fun setReorderModeEnabled(enabled: Boolean) {
        _reorderModeEnabled.value = enabled
        sharedPrefs.edit().putBoolean("reorder_mode_enabled", enabled).apply()
        saveData()
    }

    // Persistent calculator pool inputs
    private val _weaponPoolInput = MutableStateFlow("")
    val weaponPoolInput = _weaponPoolInput.asStateFlow()

    private val _armorPoolInput = MutableStateFlow("")
    val armorPoolInput = _armorPoolInput.asStateFlow()

    private val _sharedPoolInput = MutableStateFlow("")
    val sharedPoolInput = _sharedPoolInput.asStateFlow()

    private val _jointCalcResult = MutableStateFlow<com.gcirl.msmhelper.data.StoneOptimizer.JointCalculatorResult?>(null)
    val jointCalcResult = _jointCalcResult.asStateFlow()

    fun setWeaponPoolInput(value: String) { _weaponPoolInput.value = value }
    fun setArmorPoolInput(value: String) { _armorPoolInput.value = value }
    fun setSharedPoolInput(value: String) { _sharedPoolInput.value = value }
    fun setJointCalcResult(result: com.gcirl.msmhelper.data.StoneOptimizer.JointCalculatorResult?) { _jointCalcResult.value = result }

    // Transient UI selection states for Necro Tracker
    private val _currentBase = MutableStateFlow(0)
    val currentBase: StateFlow<Int> = _currentBase.asStateFlow()

    private val _currentCluster = MutableStateFlow(0)
    val currentCluster: StateFlow<Int> = _currentCluster.asStateFlow()

    // Override for weekend tracking
    private val _trackedTypeOverride = MutableStateFlow<String?>(null)
    val trackedTypeOverride: StateFlow<String?> = _trackedTypeOverride.asStateFlow()

    // Google Cloud Sync States
    private val _googleUserEmail = MutableStateFlow<String?>(null)
    val googleUserEmail: StateFlow<String?> = _googleUserEmail.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastCloudSyncTime = MutableStateFlow<String?>(null)
    val lastCloudSyncTime: StateFlow<String?> = _lastCloudSyncTime.asStateFlow()

    private val _autoSyncToCloud = MutableStateFlow(false)
    val autoSyncToCloud: StateFlow<Boolean> = _autoSyncToCloud.asStateFlow()

    private val _recoverableAuthIntent = MutableStateFlow<Intent?>(null)
    val recoverableAuthIntent: StateFlow<Intent?> = _recoverableAuthIntent.asStateFlow()

    fun clearRecoverableAuthIntent() {
        _recoverableAuthIntent.value = null
    }

    private val _syncErrorMessage = MutableStateFlow<String?>(null)
    val syncErrorMessage: StateFlow<String?> = _syncErrorMessage.asStateFlow()

    fun clearSyncErrorMessage() {
        _syncErrorMessage.value = null
    }

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    init {
        loadData()
        checkAndApplyDailyReset()
        startDailyResetWatcher()
        checkGoogleSignInSilent()
    }

    private fun startDailyResetWatcher() {
        viewModelScope.launch {
            while (isActive) {
                checkAndApplyDailyReset()
                delay(30_000)
            }
        }
    }

    // --- Persist Data ---
    private fun saveCurrentAccountStateToStateList() {
        val list = _accounts.value.toMutableList()
        val index = _activeAccountIndex.value
        if (list.isEmpty()) {
            list.add(
                com.gcirl.msmhelper.data.Account(
                    name = "Account 1",
                    characters = _characters.value,
                    activeCharIndex = _activeCharIndex.value,
                    necroHistory = _necroHistory.value,
                    weaponPoolInput = _weaponPoolInput.value,
                    armorPoolInput = _armorPoolInput.value,
                    sharedPoolInput = _sharedPoolInput.value,
                    lastResetDate = _lastResetDate.value
                )
            )
        } else if (index in list.indices) {
            val original = list[index]
            list[index] = original.copy(
                characters = _characters.value,
                activeCharIndex = _activeCharIndex.value,
                necroHistory = _necroHistory.value,
                weaponPoolInput = _weaponPoolInput.value,
                armorPoolInput = _armorPoolInput.value,
                sharedPoolInput = _sharedPoolInput.value,
                lastResetDate = _lastResetDate.value
            )
        }
        _accounts.value = list
    }

    private fun saveData() {
        saveCurrentAccountStateToStateList()
        val state = MSMAppState(
            characters = _characters.value,
            activeCharIndex = _activeCharIndex.value,
            necroHistory = _necroHistory.value,
            mastercraftHistory = _mastercraftHistory.value,
            bossAccessoryHistory = _bossAccessoryHistory.value,
            calcPresets = _calcPresets.value,
            activePresetIndex = _activePresetIndex.value,
            selectedTab = _selectedTab.value,
            accounts = _accounts.value,
            activeAccountIndex = _activeAccountIndex.value,
            lastResetDate = _lastResetDate.value,
            nextCharStoneMode = _nextCharStoneMode.value,
            reorderModeEnabled = _reorderModeEnabled.value
        )
        try {
            val jsonStr = jsonParser.encodeToString(state)
            sharedPrefs.edit().putString("app_state", jsonStr).apply()
            
            // Auto-backup to a local file in external files directory for disaster recovery
            val backupFile = java.io.File(getApplication<Application>().getExternalFilesDir(null), "msm_helper_autobackup.json")
            backupFile.writeText(jsonStr)

            // Trigger silent cloud auto-sync if logged in
            val account = googleDriveSyncManager.getSignedInAccount()
            if (account != null && _autoSyncToCloud.value) {
                cloudBackupNow(silent = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadData() {
        _autoSyncToCloud.value = sharedPrefs.getBoolean("auto_sync_cloud", false)
        _lastCloudSyncTime.value = sharedPrefs.getString("last_cloud_sync", null)

        var jsonStr = sharedPrefs.getString("app_state", null)
        if (jsonStr == null) {
            // Attempt disaster recovery from external storage file backup
            try {
                val backupFile = java.io.File(getApplication<Application>().getExternalFilesDir(null), "msm_helper_autobackup.json")
                if (backupFile.exists()) {
                    jsonStr = backupFile.readText()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        if (jsonStr != null) {
            try {
                val state = jsonParser.decodeFromString<MSMAppState>(jsonStr)
                _mastercraftHistory.value = state.mastercraftHistory
                _bossAccessoryHistory.value = state.bossAccessoryHistory
                _calcPresets.value = if (state.calcPresets.isEmpty()) {
                    listOf(CalcPreset(name = "Default Preset"))
                } else {
                    state.calcPresets
                }
                _activePresetIndex.value = state.activePresetIndex
                _selectedTab.value = state.selectedTab

                val loadedAccounts = state.accounts
                if (loadedAccounts.isEmpty()) {
                    val defaultAcc = com.gcirl.msmhelper.data.Account(
                        name = "Account 1",
                        characters = state.characters,
                        activeCharIndex = state.activeCharIndex,
                        necroHistory = state.necroHistory,
                        lastResetDate = state.lastResetDate
                    )
                    _accounts.value = listOf(defaultAcc)
                    _activeAccountIndex.value = 0
                    
                    _characters.value = state.characters
                    _activeCharIndex.value = state.activeCharIndex
                    _necroHistory.value = state.necroHistory
                    _weaponPoolInput.value = ""
                    _armorPoolInput.value = ""
                    _sharedPoolInput.value = ""
                    _lastResetDate.value = state.lastResetDate
                } else {
                    _accounts.value = loadedAccounts
                    val activeIdx = if (state.activeAccountIndex in loadedAccounts.indices) state.activeAccountIndex else 0
                    _activeAccountIndex.value = activeIdx
                    
                    val activeAcc = loadedAccounts[activeIdx]
                    _characters.value = activeAcc.characters
                    _activeCharIndex.value = activeAcc.activeCharIndex
                    _necroHistory.value = activeAcc.necroHistory
                    _weaponPoolInput.value = activeAcc.weaponPoolInput
                    _armorPoolInput.value = activeAcc.armorPoolInput
                    _sharedPoolInput.value = activeAcc.sharedPoolInput
                    _lastResetDate.value = if (state.lastResetDate.isNotEmpty()) state.lastResetDate else activeAcc.lastResetDate
                }
                _nextCharStoneMode.value = state.nextCharStoneMode.ifEmpty {
                    sharedPrefs.getString("next_char_stone_mode", "armor") ?: "armor"
                }
                _reorderModeEnabled.value = state.reorderModeEnabled || sharedPrefs.getBoolean("reorder_mode_enabled", false)
                checkAndApplyDailyReset()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val defaultAcc = com.gcirl.msmhelper.data.Account(
                name = "Account 1",
                characters = emptyList(),
                activeCharIndex = 0,
                necroHistory = emptyList(),
                lastResetDate = getCurrentServerDate()
            )
            _accounts.value = listOf(defaultAcc)
            _activeAccountIndex.value = 0
            
            _characters.value = emptyList()
            _activeCharIndex.value = 0
            _necroHistory.value = emptyList()
            _weaponPoolInput.value = ""
            _armorPoolInput.value = ""
            _sharedPoolInput.value = ""
            _lastResetDate.value = getCurrentServerDate()
            _nextCharStoneMode.value = sharedPrefs.getString("next_char_stone_mode", "armor") ?: "armor"
            _reorderModeEnabled.value = sharedPrefs.getBoolean("reorder_mode_enabled", false)
        }
    }

    // --- Google Drive Sync Actions ---
    private fun checkGoogleSignInSilent() {
        viewModelScope.launch {
            val account = googleDriveSyncManager.silentSignIn()
            handleGoogleSignInResult(account)
        }
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            _googleUserEmail.value = account.email
            _autoSyncToCloud.value = sharedPrefs.getBoolean("auto_sync_cloud", false)
            _lastCloudSyncTime.value = sharedPrefs.getString("last_cloud_sync", null)
        } else {
            _googleUserEmail.value = null
        }
    }

    fun setAutoSyncToCloud(enabled: Boolean) {
        _autoSyncToCloud.value = enabled
        sharedPrefs.edit().putBoolean("auto_sync_cloud", enabled).apply()
        if (enabled) {
            cloudBackupNow(silent = true)
        }
    }

    fun performGoogleSignOut() {
        viewModelScope.launch {
            val success = googleDriveSyncManager.signOut()
            if (success) {
                _googleUserEmail.value = null
                _lastCloudSyncTime.value = null
                _autoSyncToCloud.value = false
                sharedPrefs.edit()
                    .remove("auto_sync_cloud")
                    .remove("last_cloud_sync")
                    .apply()
            }
        }
    }

    fun cloudBackupNow(silent: Boolean = false, onComplete: ((Boolean) -> Unit)? = null) {
        val account = googleDriveSyncManager.getSignedInAccount() ?: return
        viewModelScope.launch {
            if (!silent) _isCloudSyncing.value = true
            val state = MSMAppState(
                characters = _characters.value,
                activeCharIndex = _activeCharIndex.value,
                necroHistory = _necroHistory.value
            )
            val jsonStr = jsonParser.encodeToString(state)
            val success = googleDriveSyncManager.uploadBackup(account, jsonStr)
            
            if (success) {
                val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                _lastCloudSyncTime.value = timeStamp
                sharedPrefs.edit().putString("last_cloud_sync", timeStamp).apply()
                _syncErrorMessage.value = null
            } else {
                _syncErrorMessage.value = googleDriveSyncManager.lastError ?: "Unknown backup error"
                googleDriveSyncManager.recoverableIntent?.let {
                    _recoverableAuthIntent.value = it
                    googleDriveSyncManager.recoverableIntent = null
                }
            }
            if (!silent) _isCloudSyncing.value = false
            onComplete?.invoke(success)
        }
    }

    fun cloudRestoreNow(silent: Boolean = false, onComplete: ((Boolean) -> Unit)? = null) {
        val account = googleDriveSyncManager.getSignedInAccount() ?: return
        viewModelScope.launch {
            if (!silent) _isCloudSyncing.value = true
            val jsonStr = googleDriveSyncManager.downloadBackup(account)
            var success = false
            if (jsonStr != null) {
                success = importBackupJson(jsonStr)
                if (success) {
                    val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    _lastCloudSyncTime.value = timeStamp
                    sharedPrefs.edit().putString("last_cloud_sync", timeStamp).apply()
                    _syncErrorMessage.value = null
                }
            } else {
                _syncErrorMessage.value = googleDriveSyncManager.lastError ?: "Unknown download error"
                googleDriveSyncManager.recoverableIntent?.let {
                    _recoverableAuthIntent.value = it
                    googleDriveSyncManager.recoverableIntent = null
                }
            }
            if (!silent) _isCloudSyncing.value = false
            onComplete?.invoke(success)
        }
    }

    // --- Import / Export ---
    fun exportBackupJson(): String {
        saveCurrentAccountStateToStateList()
        val state = MSMAppState(
            characters = _characters.value,
            activeCharIndex = _activeCharIndex.value,
            necroHistory = _necroHistory.value,
            mastercraftHistory = _mastercraftHistory.value,
            bossAccessoryHistory = _bossAccessoryHistory.value,
            calcPresets = _calcPresets.value,
            activePresetIndex = _activePresetIndex.value,
            selectedTab = _selectedTab.value,
            accounts = _accounts.value,
            activeAccountIndex = _activeAccountIndex.value
        )
        return try {
            jsonParser.encodeToString(state)
        } catch (e: Exception) {
            ""
        }
    }

    fun importBackupJson(jsonStr: String): Boolean {
        val trimmed = jsonStr.trim()
        return try {
            val state = jsonParser.decodeFromString<MSMAppState>(trimmed)
            _mastercraftHistory.value = state.mastercraftHistory
            _bossAccessoryHistory.value = state.bossAccessoryHistory
            _calcPresets.value = if (state.calcPresets.isEmpty()) {
                listOf(CalcPreset(name = "Default Preset"))
            } else {
                state.calcPresets
            }
            _activePresetIndex.value = state.activePresetIndex
            _selectedTab.value = state.selectedTab

            val loadedAccounts = state.accounts
            if (loadedAccounts.isEmpty()) {
                val defaultAcc = com.gcirl.msmhelper.data.Account(
                    name = "Account 1",
                    characters = state.characters,
                    activeCharIndex = state.activeCharIndex,
                    necroHistory = state.necroHistory,
                    lastResetDate = state.lastResetDate
                )
                _accounts.value = listOf(defaultAcc)
                _activeAccountIndex.value = 0
                
                _characters.value = state.characters
                _activeCharIndex.value = state.activeCharIndex
                _necroHistory.value = state.necroHistory
                _weaponPoolInput.value = ""
                _armorPoolInput.value = ""
                _sharedPoolInput.value = ""
                _lastResetDate.value = state.lastResetDate
            } else {
                _accounts.value = loadedAccounts
                val activeIdx = if (state.activeAccountIndex in loadedAccounts.indices) state.activeAccountIndex else 0
                _activeAccountIndex.value = activeIdx
                
                val activeAcc = loadedAccounts[activeIdx]
                _characters.value = activeAcc.characters
                _activeCharIndex.value = activeAcc.activeCharIndex
                _necroHistory.value = activeAcc.necroHistory
                _weaponPoolInput.value = activeAcc.weaponPoolInput
                _armorPoolInput.value = activeAcc.armorPoolInput
                _sharedPoolInput.value = activeAcc.sharedPoolInput
                _lastResetDate.value = if (state.lastResetDate.isNotEmpty()) state.lastResetDate else activeAcc.lastResetDate
            }
            _nextCharStoneMode.value = state.nextCharStoneMode
            _reorderModeEnabled.value = state.reorderModeEnabled
            checkAndApplyDailyReset()
            saveData()
            true
        } catch (e1: Exception) {
            try {
                val charactersList = jsonParser.decodeFromString<List<Character>>(trimmed)
                _characters.value = charactersList
                _activeCharIndex.value = 0
                _necroHistory.value = emptyList()
                _mastercraftHistory.value = emptyList()
                _bossAccessoryHistory.value = emptyList()
                _calcPresets.value = listOf(CalcPreset(name = "Default Preset"))
                _activePresetIndex.value = 0
                
                val defaultAcc = com.gcirl.msmhelper.data.Account(
                    name = "Account 1",
                    characters = charactersList,
                    activeCharIndex = 0,
                    necroHistory = emptyList(),
                    lastResetDate = getCurrentServerDate()
                )
                _accounts.value = listOf(defaultAcc)
                _activeAccountIndex.value = 0
                _lastResetDate.value = getCurrentServerDate()
                
                saveData()
                true
            } catch (e2: Exception) {
                e2.printStackTrace()
                false
            }
        }
    }

    // --- Daily Reset Logic (Server Time: GMT-8, Midnight 00:00) ---
    fun getCurrentServerDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT-8")
        return sdf.format(Date())
    }

    fun checkAndApplyDailyReset() {
        val currentServerDate = getCurrentServerDate()
        val lastDate = _lastResetDate.value

        if (lastDate.isEmpty()) {
            val lastAction = _necroHistory.value.firstOrNull()
            var shouldReset = false
            if (lastAction != null) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val actionDate = inputFormat.parse(lastAction.timestamp)
                    val serverFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("GMT-8")
                    }
                    val lastActionServerDate = serverFormat.format(actionDate ?: Date())
                    if (lastActionServerDate != currentServerDate) {
                        shouldReset = true
                    }
                } catch (e: Exception) {
                    shouldReset = false
                }
            }
            if (shouldReset) {
                restartToTopOfList(currentServerDate)
            } else {
                _lastResetDate.value = currentServerDate
                saveCurrentAccountStateToStateList()
                saveData()
            }
        } else if (lastDate != currentServerDate) {
            restartToTopOfList(currentServerDate)
        }
    }

    fun restartToTopOfList(newDate: String = getCurrentServerDate()) {
        _lastResetDate.value = newDate
        _activeCharIndex.value = 0
        _currentBase.value = 0
        _currentCluster.value = 0
        when (_nextCharStoneMode.value) {
            "weapon" -> _trackedTypeOverride.value = "weapon"
            else -> _trackedTypeOverride.value = null
        }

        val currentAccounts = _accounts.value
        if (currentAccounts.isNotEmpty()) {
            _accounts.value = currentAccounts.map { acc ->
                acc.copy(activeCharIndex = 0, lastResetDate = newDate)
            }
        }
        saveData()
    }

    // --- Daily Auto-Detection Logic ---
    fun getTrackedType(): String {
        // If manual override is active, use it
        _trackedTypeOverride.value?.let { return it }

        // Default to armor pieces for all days
        return "armor"
    }

    fun isWeekend(): Boolean {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    fun handleTypeClick(type: String) {
        if (_trackedTypeOverride.value == type) {
            _trackedTypeOverride.value = null
        } else {
            _trackedTypeOverride.value = type
        }
    }

    fun resetTrackedTypeOverride() {
        _trackedTypeOverride.value = null
    }

    fun getDayLabel(): String {
        val isAuto = _trackedTypeOverride.value == null
        val typeLabel = if (getTrackedType() == "weapon") "WEAPON" else "ARMOR"
        return when {
            !isAuto -> "Manual Override: Tracking $typeLabel Pieces"
            isWeekend() -> "Today is Weekend (Server Time): Tracking $typeLabel Pieces"
            else -> "Today (Server Time): Tracking $typeLabel Pieces"
        }
    }

    // --- Necro Tracker Actions ---
    fun setBase(base: Int) {
        _currentBase.value = base
    }

    fun setCluster(cluster: Int) {
        if (_currentCluster.value == cluster) {
            _currentCluster.value = 0
        } else {
            _currentCluster.value = cluster
        }
    }

    fun commitDrop() {
        val chars = _characters.value.toMutableList()
        if (chars.isEmpty()) return

        val total = _currentBase.value + _currentCluster.value
        val type = getTrackedType()
        val activeIndex = _activeCharIndex.value

        if (activeIndex in chars.indices) {
            val original = chars[activeIndex]
            val oldPieces = if (type == "weapon") original.weapon else original.armor
            val newPieces = oldPieces + total
            chars[activeIndex] = if (type == "weapon") {
                original.copy(weapon = newPieces)
            } else {
                original.copy(armor = newPieces)
            }
            _characters.value = chars

            // Log drop action in history
            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val action = NecroAction(
                actionType = "add_drop",
                timestamp = timeStamp,
                affected = listOf(AffectedCharacter(original.name, type, oldPieces, newPieces)),
                base = _currentBase.value,
                cluster = _currentCluster.value
            )
            _necroHistory.value = listOf(action) + _necroHistory.value
        }

        // Reset base and cluster, advance character (defaulting back to armor)
        _currentBase.value = 0
        _currentCluster.value = 0
        nextCharacter()
    }

    fun nextCharacter() {
        val chars = _characters.value
        if (chars.isEmpty()) return
        _activeCharIndex.value = (_activeCharIndex.value + 1) % chars.size
        _currentBase.value = 0
        _currentCluster.value = 0
        when (_nextCharStoneMode.value) {
            "weapon" -> _trackedTypeOverride.value = "weapon"
            "keep" -> { /* Keep whatever stone type is currently selected */ }
            else -> _trackedTypeOverride.value = null // defaults to "armor"
        }
        saveData()
    }

    // --- Character List Overview ---
    fun addCharacter(name: String, initialWeapon: Int, initialArmor: Int) {
        if (name.isBlank()) return
        val newChar = Character(name.trim(), initialWeapon, initialArmor)
        _characters.value = _characters.value + newChar
        saveData()
    }

    fun deleteCharacter(index: Int) {
        val chars = _characters.value.toMutableList()
        if (index in chars.indices) {
            chars.removeAt(index)
            _characters.value = chars
            _activeCharIndex.value = 0
            saveData()
        }
    }

    fun updateCharacterStat(index: Int, type: String, value: Int) {
        val chars = _characters.value.toMutableList()
        if (index in chars.indices) {
            val original = chars[index]
            val clappedValue = maxOf(0, value)
            chars[index] = if (type == "weapon") {
                original.copy(weapon = clappedValue)
            } else {
                original.copy(armor = clappedValue)
            }
            _characters.value = chars
            saveData()
        }
    }

    fun useStone(index: Int, type: String) {
        val chars = _characters.value.toMutableList()
        if (index in chars.indices) {
            val original = chars[index]
            val currentAmount = if (type == "weapon") original.weapon else original.armor
            if (currentAmount >= 150) {
                val newPieces = currentAmount - 150
                chars[index] = if (type == "weapon") {
                    original.copy(weapon = newPieces)
                } else {
                    original.copy(armor = newPieces)
                }
                _characters.value = chars

                // Log stone craft action
                val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val action = NecroAction(
                    actionType = "craft_stone",
                    timestamp = timeStamp,
                    affected = listOf(AffectedCharacter(original.name, type, currentAmount, newPieces))
                )
                _necroHistory.value = listOf(action) + _necroHistory.value

                saveData()
            }
        }
    }

    // --- Optimization Calculator Logic ---
    fun calculateOptimalDistribution(totalPool: Int, type: String): StoneOptimizer.CalculatorResult? {
        return StoneOptimizer.calculateOptimalDistribution(_characters.value, totalPool, type)
    }

    fun calculateJointOptimalDistribution(
        weaponPool: Int,
        armorPool: Int,
        sharedPool: Int
    ): StoneOptimizer.JointCalculatorResult? {
        return StoneOptimizer.calculateJointOptimalDistribution(_characters.value, weaponPool, armorPool, sharedPool)
    }

    fun craftStonesFromJointResult(
        result: StoneOptimizer.JointCalculatorResult,
        weaponPool: Int,
        armorPool: Int,
        sharedPool: Int,
        onComplete: (leftoverW: Int, leftoverA: Int, leftoverS: Int) -> Unit
    ) {
        val chars = _characters.value.toMutableList()
        val affected = mutableListOf<AffectedCharacter>()

        // Apply weapon allocations
        result.weaponDistributions.forEach { row ->
            val idx = chars.indexOfFirst { it.name == row.charName }
            if (idx != -1) {
                val original = chars[idx]
                val finalPieces = row.currentPieces - (row.stonesAdded * 150)
                affected.add(AffectedCharacter(row.charName, "weapon", original.weapon, finalPieces))
                chars[idx] = original.copy(weapon = maxOf(0, finalPieces))
            }
        }

        // Apply armor allocations
        result.armorDistributions.forEach { row ->
            val idx = chars.indexOfFirst { it.name == row.charName }
            if (idx != -1) {
                val original = chars[idx]
                val finalPieces = row.currentPieces - (row.stonesAdded * 150)
                affected.add(AffectedCharacter(row.charName, "armor", original.armor, finalPieces))
                chars[idx] = original.copy(armor = maxOf(0, finalPieces))
            }
        }

        _characters.value = chars

        // Record action in history
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val action = NecroAction(
            actionType = "batch_craft",
            timestamp = timeStamp,
            affected = affected,
            oldWeaponPool = if (weaponPool > 0) weaponPool.toString() else "",
            oldArmorPool = if (armorPool > 0) armorPool.toString() else "",
            oldSharedPool = if (sharedPool > 0) sharedPool.toString() else ""
        )
        _necroHistory.value = listOf(action) + _necroHistory.value

        saveData()

        // Calculate leftovers
        val totalWeaponAllocated = result.weaponDistributions.sumOf { it.givenPieces }
        val totalArmorAllocated = result.armorDistributions.sumOf { it.givenPieces }

        val leftoverW = maxOf(0, weaponPool - (totalWeaponAllocated - result.sharedUsedForWeapon))
        val leftoverA = maxOf(0, armorPool - (totalArmorAllocated - result.sharedUsedForArmor))
        val leftoverS = maxOf(0, sharedPool - result.sharedUsedForWeapon - result.sharedUsedForArmor)

        onComplete(leftoverW, leftoverA, leftoverS)
    }

    fun craftIndividualStone(
        charName: String,
        givenPieces: Int,
        currentPieces: Int,
        stonesAdded: Int,
        type: String,
        weaponPool: Int,
        armorPool: Int,
        sharedPool: Int,
        onComplete: (leftoverW: Int, leftoverA: Int, leftoverS: Int) -> Unit
    ) {
        val chars = _characters.value.toMutableList()
        val affected = mutableListOf<AffectedCharacter>()
        val idx = chars.indexOfFirst { it.name == charName }
        if (idx != -1) {
            val original = chars[idx]
            val finalPieces = currentPieces - (stonesAdded * 150)
            val oldPieces = if (type == "weapon") original.weapon else original.armor
            affected.add(AffectedCharacter(charName, type, oldPieces, finalPieces))
            chars[idx] = if (type == "weapon") {
                original.copy(weapon = maxOf(0, finalPieces))
            } else {
                original.copy(armor = maxOf(0, finalPieces))
            }
            _characters.value = chars
        }

        // Record action in history
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val action = NecroAction(
            actionType = "craft_stone",
            timestamp = timeStamp,
            affected = affected,
            oldWeaponPool = if (weaponPool > 0) weaponPool.toString() else "",
            oldArmorPool = if (armorPool > 0) armorPool.toString() else "",
            oldSharedPool = if (sharedPool > 0) sharedPool.toString() else ""
        )
        _necroHistory.value = listOf(action) + _necroHistory.value

        saveData()

        // Calculate leftovers using our heuristic
        var leftoverW = weaponPool
        var leftoverA = armorPool
        var leftoverS = sharedPool

        if (type == "weapon") {
            val usedFromW = minOf(weaponPool, givenPieces)
            val usedFromS = givenPieces - usedFromW
            leftoverW = maxOf(0, weaponPool - usedFromW)
            leftoverS = maxOf(0, sharedPool - usedFromS)
        } else {
            val usedFromA = minOf(armorPool, givenPieces)
            val usedFromS = givenPieces - usedFromA
            leftoverA = maxOf(0, armorPool - usedFromA)
            leftoverS = maxOf(0, sharedPool - usedFromS)
        }

        onComplete(leftoverW, leftoverA, leftoverS)
    }

    fun undoLastNecroAction(): String? {
        val history = _necroHistory.value
        if (history.isEmpty()) return null

        val lastAction = history.first()
        val chars = _characters.value.toMutableList()

        lastAction.affected.forEach { affectedChar ->
            val idx = chars.indexOfFirst { it.name == affectedChar.charName }
            if (idx != -1) {
                val original = chars[idx]
                chars[idx] = if (affectedChar.statType == "weapon") {
                    original.copy(weapon = affectedChar.oldPieces)
                } else {
                    original.copy(armor = affectedChar.oldPieces)
                }
            }
        }

        _characters.value = chars
        _necroHistory.value = history.drop(1)

        // Revert active character index if this was a drop addition
        if (lastAction.actionType == "add_drop") {
            lastAction.affected.firstOrNull()?.let { affectedChar ->
                val idx = chars.indexOfFirst { it.name == affectedChar.charName }
                if (idx != -1) {
                    _activeCharIndex.value = idx
                }
            }
        }

        // Restore pool values if present in the history item
        _weaponPoolInput.value = lastAction.oldWeaponPool
        _armorPoolInput.value = lastAction.oldArmorPool
        _sharedPoolInput.value = lastAction.oldSharedPool
        _jointCalcResult.value = null // Reset calculator output

        saveData()

        return when (lastAction.actionType) {
            "add_drop" -> {
                val totalAdded = lastAction.base + lastAction.cluster
                "Undid drop (+${totalAdded} pieces) for ${lastAction.affected.firstOrNull()?.charName}"
            }
            "craft_stone" -> "Undid stone craft for ${lastAction.affected.firstOrNull()?.charName}"
            "batch_craft" -> "Undid batch stone craft for ${lastAction.affected.size} character(s)"
            else -> "Undid last action"
        }
    }

    fun clearNecroHistory() {
        _necroHistory.value = emptyList()
        saveData()
    }

    fun logMastercraftAttempt(
        gearType: String,
        isRefined: Boolean,
        luckyScrolls: List<Int>,
        totalRate: Int,
        isSuccess: Boolean
    ) {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val attempt = MastercraftAttempt(
            timestamp = timeStamp,
            gearType = gearType,
            isRefined = isRefined,
            luckyScrolls = luckyScrolls,
            totalRate = totalRate,
            isSuccess = isSuccess
        )
        _mastercraftHistory.value = listOf(attempt) + _mastercraftHistory.value
        saveData()
    }

    fun undoLastMastercraftAttempt(): String? {
        val history = _mastercraftHistory.value
        if (history.isEmpty()) return null

        val lastAttempt = history.first()
        _mastercraftHistory.value = history.drop(1)
        saveData()

        val outcome = if (lastAttempt.isSuccess) "Success" else "Failure"
        val gearFormatted = lastAttempt.gearType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        return "Undid $gearFormatted Mastercraft $outcome"
    }

    fun clearMastercraftHistory() {
        _mastercraftHistory.value = emptyList()
        saveData()
    }

    // --- Boss Accessory Actions ---
    fun logBossAccessoryAttempt(isSuccess: Boolean) {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val attempt = BossAccessoryAttempt(timestamp = timeStamp, isSuccess = isSuccess)
        _bossAccessoryHistory.value = listOf(attempt) + _bossAccessoryHistory.value
        saveData()
    }

    fun undoLastBossAccessoryAttempt(): String? {
        val history = _bossAccessoryHistory.value
        if (history.isEmpty()) return null

        val lastAttempt = history.first()
        _bossAccessoryHistory.value = history.drop(1)
        saveData()

        val outcome = if (lastAttempt.isSuccess) "Drop" else "No Drop"
        return "Undid Boss Accessory $outcome"
    }

    fun clearBossAccessoryHistory() {
        _bossAccessoryHistory.value = emptyList()
        saveData()
    }

    // --- Calculator Preset Actions ---
    fun addCalcPreset(name: String) {
        val currentPreset = _calcPresets.value.getOrNull(_activePresetIndex.value) ?: CalcPreset(name = name)
        val newPreset = currentPreset.copy(name = name)
        val updatedList = _calcPresets.value + newPreset
        _calcPresets.value = updatedList
        _activePresetIndex.value = updatedList.lastIndex
        saveData()
    }

    fun renameActivePreset(newName: String) {
        val list = _calcPresets.value.toMutableList()
        val index = _activePresetIndex.value
        if (index in list.indices) {
            list[index] = list[index].copy(name = newName)
            _calcPresets.value = list
            saveData()
        }
    }

    fun updateActivePreset(preset: CalcPreset) {
        val list = _calcPresets.value.toMutableList()
        val index = _activePresetIndex.value
        if (index in list.indices) {
            list[index] = preset
            _calcPresets.value = list
            saveData()
        }
    }

    fun deleteActivePreset() {
        val list = _calcPresets.value.toMutableList()
        val index = _activePresetIndex.value
        if (index in list.indices) {
            list.removeAt(index)
            if (list.isEmpty()) {
                list.add(CalcPreset(name = "Default Preset"))
            }
            _calcPresets.value = list
            _activePresetIndex.value = (index - 1).coerceAtLeast(0)
            saveData()
        }
    }

    fun setActivePresetIndex(index: Int) {
        if (index in _calcPresets.value.indices) {
            _activePresetIndex.value = index
            saveData()
        }
    }

    // --- Mastercraft History Logging Actions ---
    fun deleteMastercraftAttempt(timestamp: String) {
        val history = _mastercraftHistory.value.toMutableList()
        val index = history.indexOfFirst { it.timestamp == timestamp }
        if (index != -1) {
            history.removeAt(index)
            _mastercraftHistory.value = history
            saveData()
        }
    }

    // --- Multiple Accounts Actions ---
    fun selectAccount(index: Int) {
        val list = _accounts.value
        if (index in list.indices) {
            saveCurrentAccountStateToStateList()
            
            _activeAccountIndex.value = index
            val nextAccount = list[index]
            _characters.value = nextAccount.characters
            _activeCharIndex.value = nextAccount.activeCharIndex
            _necroHistory.value = nextAccount.necroHistory
            _weaponPoolInput.value = nextAccount.weaponPoolInput
            _armorPoolInput.value = nextAccount.armorPoolInput
            _sharedPoolInput.value = nextAccount.sharedPoolInput
            _lastResetDate.value = nextAccount.lastResetDate
            
            checkAndApplyDailyReset()
            saveData()
        }
    }

    fun addAccount(name: String) {
        if (name.isBlank()) return
        saveCurrentAccountStateToStateList()
        
        val newAcc = com.gcirl.msmhelper.data.Account(
            name = name.trim(),
            characters = emptyList(),
            activeCharIndex = 0,
            necroHistory = emptyList(),
            weaponPoolInput = "",
            armorPoolInput = "",
            sharedPoolInput = "",
            lastResetDate = getCurrentServerDate()
        )
        
        val newList = _accounts.value + newAcc
        _accounts.value = newList
        selectAccount(newList.lastIndex)
    }

    fun renameAccount(index: Int, newName: String) {
        if (newName.isBlank()) return
        val list = _accounts.value.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(name = newName.trim())
            _accounts.value = list
            saveData()
        }
    }

    fun deleteAccount(index: Int) {
        val list = _accounts.value.toMutableList()
        if (list.size > 1 && index in list.indices) {
            list.removeAt(index)
            _accounts.value = list
            val nextActiveIndex = if (_activeAccountIndex.value >= list.size) 0 else _activeAccountIndex.value
            _activeAccountIndex.value = nextActiveIndex
            
            val nextAccount = list[nextActiveIndex]
            _characters.value = nextAccount.characters
            _activeCharIndex.value = nextAccount.activeCharIndex
            _necroHistory.value = nextAccount.necroHistory
            _weaponPoolInput.value = nextAccount.weaponPoolInput
            _armorPoolInput.value = nextAccount.armorPoolInput
            _sharedPoolInput.value = nextAccount.sharedPoolInput
            
            saveData()
        }
    }

    fun moveCharacter(index: Int, direction: String) {
        val list = _characters.value.toMutableList()
        if (index in list.indices) {
            val targetIndex = if (direction == "up") index - 1 else index + 1
            if (targetIndex in list.indices) {
                val temp = list[index]
                list[index] = list[targetIndex]
                list[targetIndex] = temp
                _characters.value = list
                
                val currentActive = _activeCharIndex.value
                if (currentActive == index) {
                    _activeCharIndex.value = targetIndex
                } else if (currentActive == targetIndex) {
                    _activeCharIndex.value = index
                }
                
                saveData()
            }
        }
    }
}
