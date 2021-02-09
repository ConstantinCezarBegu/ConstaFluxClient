package com.constantin.constaflux.ui.activity.host.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.constantin.constaflux.R
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.base.ScopedFragment
import kotlinx.android.synthetic.main.dialog_promt.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SettingsFragment : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()
    private lateinit var viewModel: SettingsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (nextAnim == 0) {
            null
        } else {
            val anim: Animation = AnimationUtils.loadAnimation(activity, nextAnim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    navigation.navBarFunctionality(true)
                }

                override fun onAnimationStart(p0: Animation?) {
                    navigation.navBarFunctionality(false)
                }
            })
            anim
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = navigation.getViewModel(this) as SettingsViewModel
        navigation.setUpBottomAppBarSettings()
        bindUI()
    }

    private fun bindUI() = launch {
        viewModel.user.let {
            settings_username.text = it.username
            settings_user_url.text = it.url
        }

        logoutButton.setOnClickListener {
            logoutButton.isEnabled = false
            viewModel.logoutDialogState = true
        }

        clearShearchButton.setOnClickListener {
            viewModel.clearSearch()
            viewModel.clearSearchState = false
        }

        clearFetchEntriesButton.setOnClickListener {
            viewModel.clearEntries()
            viewModel.clearFetchEntriesState = false
        }

        viewModel.clearSearchStateLiveDate.observe(this@SettingsFragment, Observer {
            if (!it) {
                clearShearchButton.isEnabled = false
                clearShearchButton.setTextColor(resources.getColor(R.color.disabled, null))
            }
        })

        viewModel.clearFetchEntriesStateLiveDate.observe(this@SettingsFragment, Observer {
            if (!it) {
                clearFetchEntriesButton.isEnabled = false
                clearFetchEntriesButton.setTextColor(resources.getColor(R.color.disabled, null))
            }
        })

        viewModel.logoutDialogStateLiveDate.observe(this@SettingsFragment, Observer {
            if (it) showDialogLogout(view!!, view as ViewGroup)
        })

        switchVerticalNavigationAnimation.run {
            isChecked = viewModel.verticalNavigationAnimation
            setOnCheckedChangeListener { _, b ->
                viewModel.verticalNavigationAnimation = b
            }
        }

        switchHorizontalNavigationAnimation.run {
            isChecked = viewModel.horizontalNavigationAnimation
            setOnCheckedChangeListener { _, b ->
                viewModel.horizontalNavigationAnimation = b
            }
        }

        switchFabVerticalAnimation.run {
            isChecked = viewModel.verticalFabAnimation
            setOnCheckedChangeListener { _, b ->
                viewModel.verticalFabAnimation = b
                navigation.fabAnimation()
            }
        }

        switchImmersiveMode.run {
            isChecked = viewModel.immersiveMode
            setOnCheckedChangeListener { _, b ->
                viewModel.immersiveMode = b
            }
        }

        spinnerArticleFabAction.run {
            adapter = ArrayAdapter.createFromResource(
                context,
                R.array.fab_article_action,
                android.R.layout.simple_spinner_item
            )
            setSelection(viewModel.articleListFabAction)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.articleListFabAction = p2
                }
            }
        }

        spinnerArticleLongPressAction.run {
            adapter = ArrayAdapter.createFromResource(
                context,
                R.array.long_press_article_action,
                android.R.layout.simple_spinner_item
            )
            setSelection(viewModel.articleLongPressAction)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.articleLongPressAction = p2
                }
            }
        }

        switchArticleScrollBar.run {
            isChecked = viewModel.verticalArticleScrollBar
            setOnCheckedChangeListener { _, b ->
                viewModel.verticalArticleScrollBar = b
            }
        }
    }

    private fun showDialogLogout(
        view: View,
        parent: ViewGroup
    ) {

        val mBuilder: AlertDialog.Builder = AlertDialog.Builder(view.context)
        val mView: View = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_promt, parent, false)
        mBuilder.setView(mView)
        val dialog: AlertDialog = mBuilder.create()

        mView.headerText.text = getString(R.string.logout)
        mView.promptBodyText.text = getString(R.string.logoutPrompt)

        mView.promtCancel.setOnClickListener {
            dialog.dismiss()
        }

        mView.promptAccept.setOnClickListener {
            dialog.dismiss()
            navigation.logout()
        }

        dialog.setOnDismissListener {
            viewModel.logoutDialogState = false
            logoutButton.isEnabled = true
        }

        dialog.show()
    }
}