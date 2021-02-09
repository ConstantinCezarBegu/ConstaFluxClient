package com.constantin.constaflux.ui.activity.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.constantin.constaflux.R
import com.constantin.constaflux.ui.activity.login.navigation.LoginViewModelNavigationProvider
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance


class LoginActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: LoginViewModelNavigationProvider by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        navigation.obtainActivity(this)


        if (savedInstanceState == null) {
            navigation.launchLoginFragment()
        }

    }
}
