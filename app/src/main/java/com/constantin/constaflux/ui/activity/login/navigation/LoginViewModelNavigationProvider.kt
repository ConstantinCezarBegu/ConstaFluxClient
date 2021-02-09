package com.constantin.constaflux.ui.activity.login.navigation

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.ui.activity.host.HostActivity
import com.constantin.constaflux.ui.activity.login.fragments.loading.LoadingFragment
import com.constantin.constaflux.ui.activity.login.fragments.login.LoginFragment
import kotlinx.android.synthetic.main.activity_login.*

class LoginViewModelNavigationProvider(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt
) {

    // Step 1 provide the root activity that will allow fragment navigation.
    private var activity: AppCompatActivity? = null

    fun obtainActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    // step 2 launch activity
    fun launchLoginFragment() {
        openFragment(LoginFragment())
    }

    fun launchLoadingFragment() {
        openFragment(LoadingFragment())
    }

    fun launchHostActivity() {
        val appConnectIntent = Intent(context, HostActivity::class.java)
        appConnectIntent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(context, appConnectIntent, null)
        activity?.finish()
    }

    // Step 3 get the view model
    fun getViewModel(fragment: Fragment): ViewModel {

        val viewModelFactory: AbstractSavedStateViewModelFactory = when (fragment) {
            is LoginFragment -> {
                LoginViewModelFactory(context, userEncrypt, fragment)
            }
            else -> {
                LoadingViewModelFactory(minifluxRepository, userEncrypt, fragment)
            }
        }
        return ViewModelProvider(fragment, viewModelFactory).get(ViewModel::class.java)
    }

    private fun openFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.replace(
            activity!!.fragment_login_place_holder.id,
            fragment
        )?.commit()
    }
}