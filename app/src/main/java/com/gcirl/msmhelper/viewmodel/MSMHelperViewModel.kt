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
import com.gcirl.msmhelper.sync.GoogleDriveSyncManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MSMHelperViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("msm_helper_prefs", Context.MODE_PRIVATE)
    val googleDriveSyncManager = GoogleDriveSyncManager(application)

    // Main states
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()

    private val _activeCharIndex = MutableStateFlow(0)
    val activeCharIndex: StateFlow<Int> = _activeCharIndex.asStateFlow()

    private val _currentSf = MutableStateFlow(10)
    val currentSf: StateFlow<Int> = _currentSf.asStateFlow()

    private val _sfStats = MutableStateFlow<Map<Int, SfLevelStats>>(emptyMap())
    val sfStats: StateFlow<Map<Int, SfLevelStats>> = _sfStats.asStateFlow()

    private val _sfHistory = MutableStateFlow<List<SfHistoryItem>>(emptyList())
    val sfHistory: StateFlow<List<SfHistoryItem>> = _sfHistory.asStateFlow()

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

    init {
        loadData()
        checkGoogleSignInSilent()
    }

    // --- Persist Data ---
    private fun saveData() {
        val state = MSMAppState(
            characters = _characters.value,
            activeCharIndex = _activeCharIndex.value,
            currentSf = _currentSf.value,
            sfStats = _sfStats.value,
            sfHistory = _sfHistory.value
        )
        try {
            val jsonStr = Json.encodeToString(state)
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
                val state = Json.decodeFromString<MSMAppState>(jsonStr)
                _characters.value = state.characters
                _activeCharIndex.value = state.activeCharIndex
                _currentSf.value = state.currentSf
                _sfStats.value = state.sfStats
                _sfHistory.value = state.sfHistory
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                currentSf = _currentSf.value,
                sfStats = _sfStats.value,
                sfHistory = _sfHistory.value
            )
            val jsonStr = Json.encodeToString(state)
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
        val state = MSMAppState(
            characters = _characters.value,
            activeCharIndex = _activeCharIndex.value,
            currentSf = _currentSf.value,
            sfStats = _sfStats.value,
            sfHistory = _sfHistory.value
        )
        return try {
            Json.encodeToString(state)
        } catch (e: Exception) {
            ""
        }
    }

    fun importBackupJson(jsonStr: String): Boolean {
        val trimmed = jsonStr.trim()
        return try {
            // 1. Try decoding as full MSMAppState
            val state = Json.decodeFromString<MSMAppState>(trimmed)
            _characters.value = state.characters
            _activeCharIndex.value = state.activeCharIndex
            _currentSf.value = state.currentSf
            _sfStats.value = state.sfStats
            _sfHistory.value = state.sfHistory
            saveData()
            true
        } catch (e1: Exception) {
            try {
                // 2. Fallback to decoding as List<Character> (web backup format)
                val charactersList = Json.decodeFromString<List<Character>>(trimmed)
                _characters.value = charactersList
                _activeCharIndex.value = 0
                saveData()
                true
            } catch (e2: Exception) {
                e2.printStackTrace()
                false
            }
        }
    }

    // --- Daily Auto-Detection Logic ---
    fun getTrackedType(): String {
        // If manual override is active, use it
        _trackedTypeOverride.value?.let { return it }

        // Otherwise auto-detect: Tue/Thu is weapon, other days is armor
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return if (dayOfWeek == Calendar.TUESDAY || dayOfWeek == Calendar.THURSDAY) {
            "weapon"
        } else {
            "armor"
        }
    }

    fun isWeekend(): Boolean {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    fun handleTypeClick(type: String) {
        if (isWeekend()) {
            _trackedTypeOverride.value = type
        }
    }

    fun resetTrackedTypeOverride() {
        _trackedTypeOverride.value = null
    }

    fun getDayLabel(): String {
        val isAuto = _trackedTypeOverride.value == null
        val typeLabel = if (getTrackedType() == "weapon") "WEAPON" else "ARMOR"
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when {
            !isAuto -> "Manual Override: Tracking $typeLabel Pieces"
            isWeekend() -> "Today is Weekend (Server Time): Tracking $typeLabel Pieces"
            dayOfWeek in listOf(Calendar.TUESDAY, Calendar.THURSDAY) -> 
                "Today is Tuesday/Thursday (Server Time): Tracking $typeLabel Pieces"
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
            chars[activeIndex] = if (type == "weapon") {
                original.copy(weapon = original.weapon + total)
            } else {
                original.copy(armor = original.armor + total)
            }
            _characters.value = chars
        }

        // Reset base and cluster, advance character (keep day override active)
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
                chars[index] = if (type == "weapon") {
                    original.copy(weapon = currentAmount - 150)
                } else {
                    original.copy(armor = currentAmount - 150)
                }
                _characters.value = chars
                saveData()
            }
        }
    }

    // --- Star Force Tracker Actions ---
    fun setInitialSf(sf: Int) {
        _currentSf.value = sf.coerceIn(0, 30)
        saveData()
    }

    fun recordOutcome(outcome: String, isCatch: Boolean) {
        val sf = _currentSf.value
        val currentStats = _sfStats.value.toMutableMap()
        
        // 1. Get or create level stats
        val levelStats = currentStats[sf] ?: SfLevelStats()
        
        // 2. Record
        currentStats[sf] = levelStats.record(outcome, isCatch)
        _sfStats.value = currentStats

        // 3. History item
        val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newItem = SfHistoryItem(timeStamp, sf, outcome, isCatch)
        _sfHistory.value = listOf(newItem) + _sfHistory.value

        // 4. Update SF level
        if (outcome == "up") {
            _currentSf.value = sf + 1
        } else if (outcome == "derank") {
            _currentSf.value = maxOf(0, sf - 1)
        }

        saveData()
    }

    fun undoLastAction() {
        val hist = _sfHistory.value
        if (hist.isEmpty()) return

        val lastAction = hist.first()
        val sf = lastAction.fromSf
        val outcome = lastAction.outcome
        val isCatch = lastAction.isCatch

        // 1. Remove from history
        _sfHistory.value = hist.drop(1)

        // 2. Decrement stats
        val currentStats = _sfStats.value.toMutableMap()
        currentStats[sf]?.let { levelStats ->
            val updated = levelStats.undo(outcome, isCatch)
            if (updated.normal.total == 0 && updated.catchStats.total == 0) {
                currentStats.remove(sf)
            } else {
                currentStats[sf] = updated
            }
        }
        _sfStats.value = currentStats

        // 3. Revert SF level
        _currentSf.value = sf

        saveData()
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
}
