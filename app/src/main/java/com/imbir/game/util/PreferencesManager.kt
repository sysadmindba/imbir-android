package com.imbir.game.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imbir.game.data.Achievement
import com.imbir.game.data.CatState
import com.imbir.game.data.RoomState

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("imbir_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveCatState(state: CatState) {
        prefs.edit().putString("cat_state", gson.toJson(state)).apply()
    }

    fun loadCatState(): CatState? {
        val json = prefs.getString("cat_state", null) ?: return null
        return try { gson.fromJson(json, CatState::class.java) } catch (e: Exception) { null }
    }

    fun saveRoomState(state: RoomState) {
        prefs.edit().putString("room_state", gson.toJson(state)).apply()
    }

    fun loadRoomState(): RoomState? {
        val json = prefs.getString("room_state", null) ?: return null
        return try { gson.fromJson(json, RoomState::class.java) } catch (e: Exception) { null }
    }

    fun saveAchievements(list: List<Achievement>) {
        prefs.edit().putString("achievements", gson.toJson(list)).apply()
    }

    fun loadAchievements(): List<Achievement>? {
        val json = prefs.getString("achievements", null) ?: return null
        return try {
            val type = object : TypeToken<List<Achievement>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { null }
    }

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun loadString(key: String, default: String = ""): String =
        prefs.getString(key, default) ?: default

    fun saveLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun loadLong(key: String, default: Long = 0L): Long =
        prefs.getLong(key, default)
}