package com.constantin.constaflux.ui.activity.host

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.constantin.constaflux.R
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.internal.FragmentContentListMode
import com.constantin.constaflux.internal.FragmentContentMode
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottomsheet.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class BottomNavigationDrawerFragment : BottomSheetDialogFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val userEncrypt: UserEncrypt by instance()
    private val navigation: HostViewModelNavigationProvider by instance()
    var isEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = userEncrypt.getUserDetail()

        bottomsheet_username.text = user.username
        bottomsheet_user_url.text = user.url

        navigation_view.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks

            when (menuItem.itemId) {
                R.id.nav_all -> {
                    navigation.launchSelectedListFragment(-1, "", FragmentContentMode.All)
                }
                R.id.nav_starred -> {
                    navigation.launchSelectedListFragment(-1, "", FragmentContentMode.Starred)
                }
                R.id.nav_feeds -> {
                    navigation.launchSelectedFragment(FragmentContentListMode.Feeds)
                }
                R.id.nav_categories -> {
                    navigation.launchSelectedFragment(FragmentContentListMode.Category)
                }
                else -> navigation.launchSettingsFragment()
            }

            dismiss()
            true
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isEnabled && isVisible.not()) {
            super.show(manager, tag)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        navigation.navBarFunctionality(true)
    }
}