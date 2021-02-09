package com.constantin.constaflux.ui.activity.login.fragments.loading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoadingViewModel(
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt,
    private val handle: SavedStateHandle
) : ViewModel() {

    init {
        minifluxRepository.refreshApiService()
    }

    val me by lazyDeferred {
        minifluxRepository.getMe()
    }

    fun endFirstLaunch() {
        userEncrypt.saveFirstLaunch(false)
    }

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            minifluxRepository.fetchCategories()
            minifluxRepository.fetchFeeds(viewModelScope)
            minifluxRepository.fetchMe()
        }
    }
}
