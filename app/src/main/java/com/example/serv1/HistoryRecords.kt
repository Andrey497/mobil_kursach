package com.example.serv1

import android.content.SharedPreferences

class HistoryRecords() {
    var list= arrayListOf<HistoryRecord>()

    fun save(preferences: SharedPreferences)
    {
        // сохраняем общее количество
        preferences.edit().putInt("N",list.size).apply();

        (0..list.size-1).forEach{
            preferences.edit().putString(Integer.toString(it)+"currentTime",list[it].currentTime).apply()
            preferences.edit().putString(Integer.toString(it)+"disease",list[it].disease).apply()
            preferences.edit().putString(Integer.toString(it)+"accuracy",list[it].accuracy).apply()
            preferences.edit().putString(Integer.toString(it)+"photoPath",list[it].photoPath).apply()
        }
    }

    fun load(preferences: SharedPreferences)
    {
        list.clear();

        val n = preferences.getInt("N",0)
        (0..n-1).forEach{
            val currentTime= preferences.getString(Integer.toString(it)+"currentTime","")
            val disease = preferences.getString(Integer.toString(it)+"disease","")
            val accuracy =preferences.getString(Integer.toString(it)+"accuracy","")
            val photoPath=preferences.getString(Integer.toString(it)+"photoPath","")

            list.add(HistoryRecord(currentTime!!,disease!!,accuracy!!,photoPath!!))
        }

    }
}