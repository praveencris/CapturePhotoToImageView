package com.sabkayar.praveen.capturedphototoimageview

import android.content.Context
import android.content.SharedPreferences


public class SharedPref(val context: Context?) {
    lateinit var mPreferences: SharedPreferences
    init {
        val sharedPref = context?.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        mPreferences = sharedPref!!
    }
    fun saveVariable(key: Int, value: String?) {
        val sharedPref = mPreferences ?: return
        with(sharedPref.edit()) {
            putString(context?.getString(key), value)
            apply()
        }
    }

    fun getVariable(key: Int, default: String): String? {
        return mPreferences.getString(context?.getString(key), default)
    }
}

public class Utils {

}