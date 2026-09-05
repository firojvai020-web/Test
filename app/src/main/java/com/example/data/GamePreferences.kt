package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("we_are_warriors_save", Context.MODE_PRIVATE)

    var coins: Long
        get() = prefs.getLong("coins", 50L)
        set(value) = prefs.edit().putLong("coins", value).apply()

    var gems: Int
        get() = prefs.getInt("gems", 30)
        set(value) = prefs.edit().putInt("gems", value).apply()

    var currentAgeIndex: Int
        get() = prefs.getInt("current_age", 0)
        set(value) = prefs.edit().putInt("current_age", value).apply()

    var timelineBattle: Int
        get() = prefs.getInt("timeline_battle", 1)
        set(value) = prefs.edit().putInt("timeline_battle", value).apply()

    var foodRateLevel: Int
        get() = prefs.getInt("food_rate_level", 1)
        set(value) = prefs.edit().putInt("food_rate_level", value).apply()

    var baseHpLevel: Int
        get() = prefs.getInt("base_hp_level", 1)
        set(value) = prefs.edit().putInt("base_hp_level", value).apply()

    var unitLevelMelee: Int
        get() = prefs.getInt("unit_level_melee", 1)
        set(value) = prefs.edit().putInt("unit_level_melee", value).apply()

    var unitLevelRanged: Int
        get() = prefs.getInt("unit_level_ranged", 1)
        set(value) = prefs.edit().putInt("unit_level_ranged", value).apply()

    var unitLevelHeavy: Int
        get() = prefs.getInt("unit_level_heavy", 1)
        set(value) = prefs.edit().putInt("unit_level_heavy", value).apply()

    var isMuted: Boolean
        get() = prefs.getBoolean("is_muted", false)
        set(value) = prefs.edit().putBoolean("is_muted", value).apply()

    fun getCardLevel(cardId: String): Int {
        return prefs.getInt("card_$cardId", 0)
    }

    fun setCardLevel(cardId: String, level: Int) {
        prefs.edit().putInt("card_$cardId", level).apply()
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }
}
