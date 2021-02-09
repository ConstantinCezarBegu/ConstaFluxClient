package com.constantin.constaflux.data.encrypt

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.constantin.constaflux.internal.stringLiveData


class UserEncrypt(context: Context) {
    companion object {
        const val userAccountUrl = "minifluxUserUrl"
        const val userAccountUrlDefault = "https://reader.miniflux.app"
        const val userAccountName = "minifluxUserAccount"
        const val userAccountPassword = "minifluxUserPassword"
        const val userAccountDefault = ""

        const val FIRST_LAUNCH = "minifluxFirstLaunch"
        const val DEFAULT_FIRST_LAUNCH = true

        const val USER_SEARCH = "userSearch"

        const val VERTICAL_NAVIGATION_ANIMATION = "verticalNavigationAnimation"
        const val HORIZONTAL_NAVIGATION_ANIMATION = "horizontalNavigationAnimation"
        const val VERTICAL_FAB_ANIMATION = "verticalFabAnimation"
        const val ANIMATION_DEFAULT = true

        const val IMMERSIVE_MODE = "immersiveMode"
        const val IMMERSIVE_MODE_DEFAULT = false
        const val ARTICLE_LIST_FAB_ACTION = "articleListFabAction"
        const val ARTICLE_LONG_PRESS_ACTION = "articleListLongPressAction"
        const val ARTICLE_ACTION_DEFAULT_FAB = 1
        const val ARTICLE_ACTION_DEFAULT_LONG_PRESS = 1
        const val VERTICAL_ARTICLE_SCROLL_BAR = "articleScrollBar"
        const val VERTICAL_ARTICLE_SCROLL_BAR_DEFAULT = true
    }


    var articleLongPressAction: Int
        set(value) {
            val sharedPrefsEditor = encryptedSharedPreferences.edit()
            sharedPrefsEditor.putInt(ARTICLE_LONG_PRESS_ACTION, value)
            sharedPrefsEditor.apply()
        }
        get() {
            return encryptedSharedPreferences.getInt(
                ARTICLE_LONG_PRESS_ACTION,
                ARTICLE_ACTION_DEFAULT_LONG_PRESS
            )
        }

    var articleListFabAction: Int
        set(value) {
            val sharedPrefsEditor = encryptedSharedPreferences.edit()
            sharedPrefsEditor.putInt(ARTICLE_LIST_FAB_ACTION, value)
            sharedPrefsEditor.apply()
        }
        get() {
            return encryptedSharedPreferences.getInt(
                ARTICLE_LIST_FAB_ACTION,
                ARTICLE_ACTION_DEFAULT_FAB
            )
        }

    var verticalNavigationAnimation: Boolean
        set(value) {
            val sharedPrefsEditor = encryptedSharedPreferences.edit()
            sharedPrefsEditor.putBoolean(VERTICAL_NAVIGATION_ANIMATION, value)
            sharedPrefsEditor.apply()
        }
        get() {
            return encryptedSharedPreferences.getBoolean(
                VERTICAL_NAVIGATION_ANIMATION,
                ANIMATION_DEFAULT
            )
        }

    var horizontalNavigationAnimation: Boolean
        set(value) {
            val sharedPrefsEditor = encryptedSharedPreferences.edit()
            sharedPrefsEditor.putBoolean(HORIZONTAL_NAVIGATION_ANIMATION, value)
            sharedPrefsEditor.apply()
        }
        get() {
            return encryptedSharedPreferences.getBoolean(
                HORIZONTAL_NAVIGATION_ANIMATION,
                ANIMATION_DEFAULT
            )
        }

    var verticalFabAnimation: Boolean
        set(value) {
            val sharedPrefsEditor = encryptedSharedPreferences.edit()
            sharedPrefsEditor.putBoolean(VERTICAL_FAB_ANIMATION, value)
            sharedPrefsEditor.apply()
        }
        get() {
            return encryptedSharedPreferences.getBoolean(
                VERTICAL_FAB_ANIMATION,
                ANIMATION_DEFAULT
            )
        }

    var immersiveMode: Boolean
        set(value) {
            val sharedPrefsEditor = encryptedSharedPreferences.edit()
            sharedPrefsEditor.putBoolean(IMMERSIVE_MODE, value)
            sharedPrefsEditor.apply()
        }
        get() {
            return encryptedSharedPreferences.getBoolean(
                IMMERSIVE_MODE,
                IMMERSIVE_MODE_DEFAULT
            )
        }

    var articleScrollBar: Boolean
        set(value) {
            val sharedPrefsEditor = encryptedSharedPreferences.edit()
            sharedPrefsEditor.putBoolean(VERTICAL_ARTICLE_SCROLL_BAR, value)
            sharedPrefsEditor.apply()
        }
        get() {
            return encryptedSharedPreferences.getBoolean(
                VERTICAL_ARTICLE_SCROLL_BAR,
                VERTICAL_ARTICLE_SCROLL_BAR_DEFAULT
            )
        }

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        "${context.packageName}_encrypted_preferences",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveFirstLaunch(firstLaunch: Boolean) {
        val sharedPrefsEditor = encryptedSharedPreferences.edit()
        sharedPrefsEditor.putBoolean(FIRST_LAUNCH, firstLaunch)
        sharedPrefsEditor.apply()
    }

    fun getFirstLaunch(): Boolean {
        return encryptedSharedPreferences.getBoolean(FIRST_LAUNCH, DEFAULT_FIRST_LAUNCH)
    }

    fun saveUser(userAccount: UserAccount) {
        val sharedPrefsEditor = encryptedSharedPreferences.edit()
        sharedPrefsEditor.putString(userAccountUrl, userAccount.url)
        sharedPrefsEditor.putString(userAccountName, userAccount.username)
        sharedPrefsEditor.putString(userAccountPassword, userAccount.password)
        sharedPrefsEditor.apply()
    }

    fun getUser(): UserAccount {
        return UserAccount(
            encryptedSharedPreferences.getString(userAccountUrl, userAccountUrlDefault)!!,
            encryptedSharedPreferences.getString(userAccountName, userAccountDefault)!!,
            encryptedSharedPreferences.getString(userAccountPassword, userAccountDefault)!!
        )
    }

    fun getUserDetail(): UserAccountInfo {
        return UserAccountInfo(
            encryptedSharedPreferences.getString(userAccountUrl, userAccountUrlDefault)!!,
            encryptedSharedPreferences.getString(userAccountName, userAccountDefault)!!
        )
    }

    fun deleteUser() {
        val sharedPrefsEditor = encryptedSharedPreferences.edit()
        sharedPrefsEditor.putString(userAccountName, userAccountDefault)
        sharedPrefsEditor.putString(userAccountPassword, userAccountDefault)
        sharedPrefsEditor.apply()
    }


    fun getSearchLiveData() =
        encryptedSharedPreferences.stringLiveData(USER_SEARCH, userAccountDefault)

    fun saveSearch(search: String) {
        val sharedPrefsEditor = encryptedSharedPreferences.edit()
        sharedPrefsEditor.putString(USER_SEARCH, search)
        sharedPrefsEditor.apply()
    }
}

