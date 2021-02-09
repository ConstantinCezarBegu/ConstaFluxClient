package com.constantin.constaflux

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.constantin.constaflux.data.db.MinifluxDatabase
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.network.MinifluxApiProvider
import com.constantin.constaflux.data.network.MinifluxDataSource
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.activity.login.navigation.LoginViewModelNavigationProvider
import com.jakewharton.threetenabp.AndroidThreeTen
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class ConstaFluxApplication : Application(), KodeinAware {

    companion object {
        const val CHANNEL_NEW_ENTRY_ID = "channelEntry"
    }


    override val kodein: Kodein = Kodein.lazy {
        import(androidXModule(this@ConstaFluxApplication))

        // Database binding
        bind() from singleton { MinifluxDatabase(instance()) }
        bind() from singleton { instance<MinifluxDatabase>().feedsDao() }
        bind() from singleton { instance<MinifluxDatabase>().meDao() }
        bind() from singleton { instance<MinifluxDatabase>().entryDao() }
        bind() from singleton { instance<MinifluxDatabase>().categoryDao() }
        bind() from singleton { instance<MinifluxDatabase>().entryIdTableDao() }

        // Connection binding
        bind() from singleton { MinifluxApiProvider(instance()) }
        bind() from singleton { MinifluxDataSource(instance()) }
        bind() from singleton {
            MinifluxRepository(
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind() from singleton { UserEncrypt(instance()) }

        // View model manager binding
        bind() from singleton {
            LoginViewModelNavigationProvider(
                instance(),
                instance(),
                instance()
            )
        }
        bind() from singleton {
            HostViewModelNavigationProvider(
                instance(),
                instance(),
                instance()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        AndroidThreeTen.init(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannels = mutableListOf<NotificationChannel>()

            val intake = NotificationChannel(
                CHANNEL_NEW_ENTRY_ID,
                "Entries updates.",
                NotificationManager.IMPORTANCE_HIGH
            )
            intake.description = "Lets the users know when they get new articles."
            notificationChannels += intake

            getSystemService(NotificationManager::class.java)
                .createNotificationChannels(notificationChannels)

        }
    }
}