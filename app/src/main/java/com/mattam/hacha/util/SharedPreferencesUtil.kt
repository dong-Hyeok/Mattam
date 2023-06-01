package com.mattam.template.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.provider.TedPermissionProvider.context
import com.mattam.template.config.ApplicationClass

class SharedPreferencesUtil(context: Context) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences(ApplicationClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun addUserCookie(cookies: HashSet<String>) {
        val editor = preferences.edit()
        editor.putStringSet(ApplicationClass.COOKIES_KEY_NAME, cookies)
        editor.apply()
    }

    fun getUserCookie(): MutableSet<String>? {
        return preferences.getStringSet(ApplicationClass.COOKIES_KEY_NAME, HashSet())
    }

    fun getString(key:String): String? {
        return preferences.getString(key, null)
    }

    fun addUserTokenId(token:String){
        val editor = preferences.edit()
        editor.putString("Token", token)
        editor.apply()
    }

    fun getUserTokenId() :String{
        val userToken = preferences.getString("Token", "")
        return userToken!!
    }

    fun addUserId(userID:String){
        Log.d("sharedPre", "addUserId: $userID")
        val editor = preferences.edit()
        editor.putString("userID", userID)
        editor.apply()
    }

    fun getUserId(): String {
        val userID = preferences.getString("userID", "")
        return userID!!
    }

    fun addProfileImg(profileImg:String){
        Log.d("sharedPre", "ProfileImg: $profileImg")
        val editor = preferences.edit()
        editor.putString("profileImg", profileImg)
        editor.apply()
    }

    fun getProfileImg(): String {
        val profileImg = preferences.getString("profileImg", "")
        return profileImg!!
    }

    fun clearUserInfo(){
        Firebase.auth.signOut()
        context.getSharedPreferences("Token", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("userID", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("profileImg", Context.MODE_PRIVATE).edit().clear().apply()
    }


}