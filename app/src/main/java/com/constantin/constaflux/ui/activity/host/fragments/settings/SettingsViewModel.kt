package com.constantin.constaflux.ui.activity.host.fragments.settings

import android.content.Context
import android.provider.SearchRecentSuggestions
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constantin.constaflux.data.encrypt.UserAccountInfo
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.data.search.MinifluxSuggestionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val CLEAR_SEARCH_STATE = "minifluxClearSearchState"
        const val CLEAR_FETCH_ENTRIES_STATE = "minifluxClearFetchEntriesState"
        const val LOGOUT_DIALOG_STATE = "minifluxLogoutState"
    }

    var articleLongPressAction: Int
        set(value) {
            userEncrypt.articleLongPressAction = value
        }
        get() {
            return userEncrypt.articleLongPressAction
        }

    var articleListFabAction: Int
        set(value) {
            userEncrypt.articleListFabAction = value
        }
        get() {
            return userEncrypt.articleListFabAction
        }

    var verticalNavigationAnimation: Boolean
        set(value) {
            userEncrypt.verticalNavigationAnimation = value
        }
        get() {
            return userEncrypt.verticalNavigationAnimation
        }


    var horizontalNavigationAnimation: Boolean
        set(value) {
            userEncrypt.horizontalNavigationAnimation = value
        }
        get() {
            return userEncrypt.horizontalNavigationAnimation
        }


    var verticalFabAnimation: Boolean
        set(value) {
            userEncrypt.verticalFabAnimation = value
        }
        get() {
            return userEncrypt.verticalFabAnimation
        }

    var immersiveMode: Boolean
        set(value) {
            userEncrypt.immersiveMode = value
        }
        get() {
            return userEncrypt.immersiveMode
        }

    var verticalArticleScrollBar: Boolean
        set(value) {
            userEncrypt.articleScrollBar = value
        }
        get() {
            return userEncrypt.articleScrollBar
        }


    var clearSearchState: Boolean
        get() = handle.get<Boolean>(CLEAR_SEARCH_STATE) ?: true
        set(value) {
            handle.set(CLEAR_SEARCH_STATE, value)
        }
    val clearSearchStateLiveDate = handle.getLiveData<Boolean>(CLEAR_SEARCH_STATE)

    var clearFetchEntriesState: Boolean
        get() = handle.get<Boolean>(CLEAR_FETCH_ENTRIES_STATE) ?: true
        set(value) {
            handle.set(CLEAR_FETCH_ENTRIES_STATE, value)
        }
    val clearFetchEntriesStateLiveDate = handle.getLiveData<Boolean>(CLEAR_FETCH_ENTRIES_STATE)

    var logoutDialogState: Boolean
        get() = handle.get<Boolean>(LOGOUT_DIALOG_STATE) ?: false
        set(value) {
            handle.set(LOGOUT_DIALOG_STATE, value)
        }

    val logoutDialogStateLiveDate = handle.getLiveData<Boolean>(LOGOUT_DIALOG_STATE)

    val user: UserAccountInfo by lazy {
        userEncrypt.getUserDetail()
    }

    fun clearSearch() {
        viewModelScope.launch(Dispatchers.IO) {
            SearchRecentSuggestions(
                context,
                MinifluxSuggestionProvider.AUTHORITY,
                MinifluxSuggestionProvider.MODE
            )
                .clearHistory()
        }
    }

    fun clearEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            minifluxRepository.clearEntries()
        }
    }
}