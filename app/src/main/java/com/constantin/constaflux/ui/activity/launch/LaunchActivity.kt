package com.constantin.constaflux.ui.activity.launch

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.ui.activity.host.HostActivity
import com.constantin.constaflux.ui.activity.login.LoginActivity
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class LaunchActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    val userEncrypt: UserEncrypt by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = if (userEncrypt.getFirstLaunch()) {
            Intent(this, LoginActivity::class.java)
        } else {
            Intent(this, HostActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}