package com.constantin.constaflux.ui.activity.login.fragments.login

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constantin.constaflux.data.encrypt.UserAccount
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.network.authenticator.MinifluxAuthenticator
import com.constantin.constaflux.internal.Errors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException

class LoginViewModel(
    private val context: Context,
    private val userEncrypt: UserEncrypt,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val URL_HANDLE = "urlHandleLoginViewModel"
        const val USER_HANDLE = "usernameHandleLoginViewModel"
        const val PASS_HANDLE = "passwordHandleLoginViewModel"
    }

    var url: String?
        get() = handle.get<String>(URL_HANDLE)
        set(value) {
            handle.set(URL_HANDLE, value)
        }

    var user: String?
        get() = handle.get<String>(USER_HANDLE)
        set(value) {
            handle.set(USER_HANDLE, value)
        }

    var pass: String?
        get() = handle.get<String>(PASS_HANDLE)
        set(value) {
            handle.set(PASS_HANDLE, value)
        }

    val error: MutableLiveData<Errors> by lazy {
        MutableLiveData(Errors.CORRECT)
    }

    init {
        url = userEncrypt.getUserDetail().url
    }

    fun connectUser(
        url: String,
        username: String,
        password: String
    ) {
        if (isOnline(context)) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    MinifluxAuthenticator(
                        username,
                        password
                    ).run(url)
                    userEncrypt.saveUser(UserAccount(url, username, password))
                    error.postValue(Errors.SUCCESS)
                } catch (e: IllegalArgumentException) {
                    error.postValue(Errors.URL)
                } catch (e: UnknownHostException) {
                    error.postValue(Errors.URL)
                } catch (e: IOException) {
                    error.postValue(Errors.CREDENTIALS)
                }
            }
        } else {
            error.postValue(Errors.NO_CONNECTIVITY)
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
