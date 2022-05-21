package com.example.echoloc.database

import android.content.Context
import android.content.SharedPreferences

class Pref {
    private  val PREFERANCE_NAME="my_preferance"

    lateinit var context: Context

    lateinit var  preferences: SharedPreferences
    constructor(context: Context)
    {
        this.context=context
        preferences=context.getSharedPreferences(PREFERANCE_NAME, Context.MODE_PRIVATE)
    }

    public fun saveData(key:String,value:String)
    {
        var editor=preferences.edit()
        editor.putString(key,value)
        editor.commit()
    }
    public fun getData(key: String):String
    {
        return preferences.getString(key,"")!!
    }

    public fun clearData(){
        var editor = preferences.edit()
        editor.clear()
        editor.commit()
    }
}