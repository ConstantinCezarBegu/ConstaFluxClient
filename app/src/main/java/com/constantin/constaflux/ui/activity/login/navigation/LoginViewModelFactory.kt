package com.constantin.constaflux.ui.activity.login.navigation

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.ui.activity.login.fragments.loading.LoadingViewModel
import com.constantin.constaflux.ui.activity.login.fragments.login.LoginViewModel


class LoginViewModelFactory(
    private val context: Context,
    private val userEncrypt: UserEncrypt,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return LoginViewModel(context, userEncrypt, handle) as T
    }
}

class LoadingViewModelFactory(
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return LoadingViewModel(minifluxRepository, userEncrypt, handle) as T
    }
}