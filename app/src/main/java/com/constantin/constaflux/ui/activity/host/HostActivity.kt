package com.constantin.constaflux.ui.activity.host

import android.app.NotificationManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.constantin.constaflux.ConstaFluxWorkerManager
import com.constantin.constaflux.ConstaFluxWorkerManager.Companion.UNIQUE_WORKER_NAME_TRACKER
import com.constantin.constaflux.R
import com.constantin.constaflux.data.search.MinifluxSuggestionProvider
import com.constantin.constaflux.internal.FragmentContentMode
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_host.*
import kotlinx.android.synthetic.main.content_main_application.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.util.concurrent.TimeUnit


class HostActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()
    var onBackPressed = true

    companion object {
        const val IS_SEARCH_UP = "isSearchUp"
    }

    override fun onBackPressed() {
        if (onBackPressed && !navigation.backToMain()) {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        navigation.obtainActivity(this)
        navigation.fabAnimation()
        val toolbar: BottomAppBar = findViewById(R.id.bottomAppBar)

        toolbar.setNavigationOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        if (savedInstanceState == null) {
            navigation.launchSelectedListFragment(-1, "", FragmentContentMode.All)
        }

        enqueueWork()
    }

    override fun onResume() {
        super.onResume()
        clearNotification()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.getStringExtra(SearchManager.QUERY).also { query ->
                navigation.closeSearchBar()
                SearchRecentSuggestions(
                    this,
                    MinifluxSuggestionProvider.AUTHORITY,
                    MinifluxSuggestionProvider.MODE
                )
                    .saveRecentQuery(query, null)
                navigation.launchSearchDialog(query!!)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (this.bottomAppBar.menu.size() > 0 && this.bottomAppBar.menu[0].itemId == R.id.app_bar_search) {
            outState.putBoolean(
                IS_SEARCH_UP,
                this.bottomAppBar.menu[0].isActionViewExpanded
            )
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val isSearchUp = savedInstanceState.getBoolean(IS_SEARCH_UP)
        if (isSearchUp) {
            this.shadowOverlay.visibility = View.VISIBLE
            this.setAppBarAction.hide()
        }
    }

    private fun enqueueWork() {

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORKER_NAME_TRACKER,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequest.Builder(
                    ConstaFluxWorkerManager::class.java,
                    PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                    TimeUnit.MILLISECONDS

                ).build()
            )
    }

    private fun clearNotification() {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
