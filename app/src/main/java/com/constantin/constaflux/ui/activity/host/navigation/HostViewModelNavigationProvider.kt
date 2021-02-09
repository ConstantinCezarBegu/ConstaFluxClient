package com.constantin.constaflux.ui.activity.host.navigation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.constantin.constaflux.R
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.data.db.entity.Feed
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.data.search.MinifluxSuggestionProvider
import com.constantin.constaflux.internal.DialogMode
import com.constantin.constaflux.internal.FragmentContentListMode
import com.constantin.constaflux.internal.FragmentContentMode
import com.constantin.constaflux.ui.activity.host.BottomNavigationDrawerFragment
import com.constantin.constaflux.ui.activity.host.HostActivity
import com.constantin.constaflux.ui.activity.host.dialog.category.CategoryDialog
import com.constantin.constaflux.ui.activity.host.dialog.feed.FeedDialog
import com.constantin.constaflux.ui.activity.host.dialog.search.SearchDialog
import com.constantin.constaflux.ui.activity.host.fragments.entry_item.DisplayEntryFragment
import com.constantin.constaflux.ui.activity.host.fragments.selected.SelectedFragment
import com.constantin.constaflux.ui.activity.host.fragments.selected_list.SelectedListFragment
import com.constantin.constaflux.ui.activity.host.fragments.selected_list.SelectedStatusFragment
import com.constantin.constaflux.ui.activity.host.fragments.settings.SettingsFragment
import com.constantin.constaflux.ui.activity.login.LoginActivity
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main_application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HostViewModelNavigationProvider(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt
) {

    companion object {
        const val ENTRY_ID_DISPLAY = "entryIdDisplay"

        const val SELECTED_LIST_MODE = "selectedListMode"

        const val SELECTED_ID = "selectedId"
        const val SELECTED_SEARCH = "selectedSearch"
        const val SELECTED_MODE = "selectedMod"

        const val FEED_DIALOG_MODE = "feedDialogMode"
        const val ITEM_FEED = "itemFeed"

        const val CATEGORY_DIALOG_MODE = "categoryDialogMode"
        const val ITEM_CATEGORY = "itemCategory"
    }

    // Step 1 provide the root activity that will allow fragment navigation.
    lateinit var activity: AppCompatActivity
    lateinit var bottomAppBar: BottomAppBar
    lateinit var fab: FloatingActionButton
    private val bottomNavigationDrawerFragment = BottomNavigationDrawerFragment()

    fun fabAnimation() {
        if (userEncrypt.verticalFabAnimation.not()) {
            bottomAppBar.fabAnimationMode = BottomAppBar.FAB_ANIMATION_MODE_SCALE
        } else {
            bottomAppBar.fabAnimationMode = BottomAppBar.FAB_ANIMATION_MODE_SLIDE
        }
    }

    fun obtainActivity(activity: AppCompatActivity) {
        this.activity = activity
        bottomAppBar = activity.findViewById(R.id.bottomAppBar)
        fab = activity.findViewById(R.id.setAppBarAction)
    }

    // Step 2 launch desired fragment
    fun launchDisplayEntryFragment(entry: Long) {
        val bundle = Bundle()
        bundle.putLong(ENTRY_ID_DISPLAY, entry)
        immersiveMode(true)
        openFragmentAddToStack(DisplayEntryFragment(), bundle)
    }

    fun launchSelectedFragment(selectedListMode: FragmentContentListMode) {
        val bundle = Bundle()
        val oldFragment =
            activity.supportFragmentManager.findFragmentById(activity.fragment_place_holder.id)

        if (!(oldFragment is SelectedFragment && oldFragment.arguments!!.getSerializable(
                SELECTED_LIST_MODE
            ) as FragmentContentListMode == selectedListMode)
        ) {
            bundle.putSerializable(SELECTED_LIST_MODE, selectedListMode)
            openFragment(SelectedFragment(), bundle)
        }
    }

    fun launchSelectedListFragment(
        selectedId: Long,
        selectedSearch: String,
        selectedMode: FragmentContentMode
    ) {
        val bundle = Bundle()

        val oldFragment =
            activity.supportFragmentManager.findFragmentById(activity.fragment_place_holder.id)

        if (!(oldFragment is SelectedListFragment && oldFragment.arguments!!.getSerializable(
                SELECTED_MODE
            ) as FragmentContentMode == selectedMode)
        ) {
            bundle.putLong(SELECTED_ID, selectedId)
            bundle.putString(SELECTED_SEARCH, selectedSearch)
            bundle.putSerializable(SELECTED_MODE, selectedMode)

            if (selectedMode == FragmentContentMode.All || selectedMode == FragmentContentMode.Starred) {
                openFragment(SelectedListFragment(), bundle)
            } else {
                openFragmentAddToStack(SelectedListFragment(), bundle)
            }
        }
    }

    fun launchSettingsFragment() {
        val bundle = Bundle()

        val oldFragment =
            activity.supportFragmentManager.findFragmentById(activity.fragment_place_holder.id)

        if (oldFragment !is SettingsFragment) {
            openFragment(SettingsFragment(), bundle)
        }
    }

    fun launchSearchDialog(selectedSearch: String) {
        navBarFunctionality(false)
        val bundle = Bundle()
        bundle.putString(SELECTED_SEARCH, selectedSearch)
        SearchDialog.newInstance().let {
            it.arguments = bundle
            it.show(activity.supportFragmentManager, null)
        }
    }

    fun launchFeedDialog(fm: FragmentManager, feedDialogMode: DialogMode, itemFeed: Feed?) {
        navBarFunctionality(false)
        val bundle = Bundle()
        bundle.putSerializable(FEED_DIALOG_MODE, feedDialogMode)
        bundle.putParcelable(ITEM_FEED, itemFeed)

        FeedDialog.newInstance().let {
            it.arguments = bundle
            it.show(fm, null)
        }
    }

    fun launchCategoryDialog(
        fm: FragmentManager,
        categoryDialogMode: DialogMode,
        itemCategory: CategoryEntity?
    ) {
        val bundle = Bundle()
        bundle.putSerializable(CATEGORY_DIALOG_MODE, categoryDialogMode)
        bundle.putParcelable(ITEM_CATEGORY, itemCategory)

        navBarFunctionality(false)
        CategoryDialog.newInstance().let {
            it.arguments = bundle
            it.show(fm, null)
        }
    }

    private fun launchLoginActivity() {
        val appConnectIntent = Intent(context, LoginActivity::class.java)
        appConnectIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(context, appConnectIntent, null)
        activity.finish()
    }


    // Step 3 get the view model
    fun getViewModel(fragment: Fragment): ViewModel {
        val viewModelFactory: AbstractSavedStateViewModelFactory = when (fragment) {
            is DisplayEntryFragment -> {
                val bundle = fragment.arguments!!
                DisplayEntryViewModelFactory(
                    context,
                    userEncrypt,
                    minifluxRepository,
                    bundle.getLong(ENTRY_ID_DISPLAY),
                    fragment
                )
            }
            is SelectedFragment -> {
                val bundle = fragment.arguments!!
                SelectedViewModelFactory(
                    context,
                    minifluxRepository,
                    bundle.getSerializable(SELECTED_LIST_MODE) as FragmentContentListMode,
                    fragment
                )
            }
            is SelectedListFragment, is SelectedStatusFragment -> {
                val bundle = if (fragment is SelectedListFragment) fragment.arguments!!
                else fragment.parentFragment!!.arguments!!
                SelectedListViewModelFactory(
                    context,
                    minifluxRepository,
                    userEncrypt,
                    bundle.getLong(SELECTED_ID),
                    bundle.getSerializable(SELECTED_MODE) as FragmentContentMode,
                    fragment
                )
            }
            is FeedDialog -> {
                val bundle = fragment.arguments!!
                FeedDialogViewModelFactory(
                    minifluxRepository,
                    bundle.getSerializable(FEED_DIALOG_MODE) as DialogMode,
                    bundle.getParcelable(ITEM_FEED),
                    fragment
                )
            }
            is SearchDialog -> {
                val bundle = fragment.arguments!!
                SearchDialogViewModelFactory(
                    minifluxRepository,
                    bundle.getString(SELECTED_SEARCH)!!,
                    userEncrypt,
                    fragment
                )
            }
            is CategoryDialog -> {
                val bundle = fragment.arguments!!
                CategoryDialogViewModelFactory(
                    minifluxRepository,
                    bundle.getSerializable(CATEGORY_DIALOG_MODE) as DialogMode,
                    bundle.getParcelable(ITEM_CATEGORY),
                    fragment
                )
            }
            else -> {
                SettingsViewModelFactory(context, minifluxRepository, userEncrypt, fragment)
            }
        }
        return ViewModelProvider(fragment, viewModelFactory).get(ViewModel::class.java)
    }

    fun backToMain(): Boolean {
        val returning =
            checkIfMainFragment().not() && activity.supportFragmentManager.backStackEntryCount == 0


        if (returning) {
            launchSelectedListFragment(-1, "", FragmentContentMode.All)
        }
        return returning
    }

    private fun checkIfMainFragment(): Boolean {
        val oldFragment =
            activity.supportFragmentManager.findFragmentById(activity.fragment_place_holder.id)

        return activity.supportFragmentManager.backStackEntryCount == 0 && oldFragment is SelectedListFragment && oldFragment.arguments!!.getSerializable(
            SELECTED_MODE
        ) as FragmentContentMode == FragmentContentMode.All
    }

    private fun openFragment(fragment: Fragment, bundle: Bundle) {
        navBarFunctionality(false)
        fragment.arguments = bundle

        var enter = R.anim.fade_in
        var exit = R.anim.fade_out

        val oldFragment =
            activity.supportFragmentManager.findFragmentById(activity.fragment_place_holder.id)

        if (oldFragment != null) {
            when (oldFragment) {
                is SelectedListFragment -> {
                    when (fragment) {
                        is SelectedListFragment -> {

                        }
                        is SelectedFragment -> {
                            enter = R.anim.enter_from_top
                            exit = R.anim.exit_to_bottom
                        }
                        is SettingsFragment -> {
                            enter = R.anim.enter_from_top
                            exit = R.anim.exit_to_bottom
                        }
                    }
                }
                is SelectedFragment -> {
                    when (fragment) {
                        is SelectedListFragment -> {
                            enter = R.anim.enter_from_bottom
                            exit = R.anim.exit_to_top
                        }
                        is SelectedFragment -> {

                        }
                        is SettingsFragment -> {
                            enter = R.anim.enter_from_top
                            exit = R.anim.exit_to_bottom
                        }
                    }
                }
                is SettingsFragment -> {
                    when (fragment) {
                        is SelectedListFragment -> {
                            enter = R.anim.enter_from_bottom
                            exit = R.anim.exit_to_top
                        }
                        is SelectedFragment -> {
                            enter = R.anim.enter_from_bottom
                            exit = R.anim.exit_to_top
                        }
                        is SettingsFragment -> {

                        }
                    }
                }
            }
        }
        if (userEncrypt.verticalNavigationAnimation) {
            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(enter, exit)
                .replace(
                    activity.fragment_place_holder.id,
                    fragment
                ).commit()
        } else {
            navBarFunctionality(true)
            activity.supportFragmentManager.beginTransaction()
                .replace(
                    activity.fragment_place_holder.id,
                    fragment
                ).commit()

        }
    }

    private fun openFragmentAddToStack(fragment: Fragment, bundle: Bundle) {
        navBarFunctionality(false)
        fragment.arguments = bundle
        if (userEncrypt.horizontalNavigationAnimation) {
            activity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )
                .replace(
                    activity.fragment_place_holder.id,
                    fragment
                ).addToBackStack(null).commit()
        } else {
            navBarFunctionality(true)
            activity.supportFragmentManager.beginTransaction()
                .replace(
                    activity.fragment_place_holder.id,
                    fragment
                ).addToBackStack(null).commit()
        }
    }


    fun setUpBottomAppBarListFragments() {
        val oldFragment =
            activity.supportFragmentManager.findFragmentById(activity.fragment_place_holder.id)

        val fragmentContentMode =
            oldFragment?.arguments!!.getSerializable(SELECTED_MODE) as FragmentContentMode?

        bottomAppBar.run {
            showBottomAppBar()

            if (fragmentContentMode == null || fragmentContentMode == FragmentContentMode.All || fragmentContentMode == FragmentContentMode.Starred) {
                fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                replaceMenu(R.menu.bottomappbar_menu)

                navigationIcon =
                    ContextCompat.getDrawable(context, R.drawable.arrow_back_to_hamburger)
                (navigationIcon as AnimatedVectorDrawable).start()
                setNavigationOnClickListener {
                    bottomNavigationDrawerFragment.showNavigation(
                        activity.supportFragmentManager,
                        bottomNavigationDrawerFragment.tag
                    )
                }

                setNavigationSearch()
            } else {
                fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
                replaceMenu(R.menu.menu_search)

                val backArrowAnimated =
                    ContextCompat.getDrawable(context, R.drawable.hamburger_to_arrow_back)
                val backArrow = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back)

                if (navigationIcon!!.constantState != backArrow!!.constantState) {
                    navigationIcon = backArrowAnimated
                    (navigationIcon as AnimatedVectorDrawable).start()
                }

                setNavigationOnClickListener {
                    activity.onBackPressed()
                }
            }

        }

        fab.run {
            show()
            if (fragmentContentMode == null) setImageResource(R.drawable.ic_add)
        }
    }

    fun closeSearchBar() {
        bottomAppBar.menu[0].collapseActionView()
    }

    fun setUpBottomAppBarDisplayFragment() {
        bottomAppBar.run {
            performShow()
            fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
            replaceMenu(R.menu.menu_entry)
            when (activity.supportFragmentManager.backStackEntryCount) {
                1 -> {
                    navigationIcon =
                        ContextCompat.getDrawable(context, R.drawable.hamburger_to_arrow_back)
                    (navigationIcon as AnimatedVectorDrawable).start()
                }
                2 -> {
                    navigationIcon =
                        ContextCompat.getDrawable(context, R.drawable.ic_arrow_back)
                }
            }

            setNavigationOnClickListener {
                activity.onBackPressed()
            }
        }

        fab.run {
            show()
            setImageResource(R.drawable.ic_open_web)
        }
    }

    fun setUpBottomAppBarSettings() {
        bottomAppBar.run {
            performShow()
            replaceMenu(R.menu.menu_search)
            navigationIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_hamburger)
            setNavigationOnClickListener {
                bottomNavigationDrawerFragment.showNavigation(
                    activity.supportFragmentManager,
                    bottomNavigationDrawerFragment.tag
                )
            }
        }

        fab.run {
            hide()
        }
    }

    private fun setNavigationSearch() {
        bottomAppBar.run {
            val searchManager =
                activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
                setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
            }

            fixSearchView(activity, menu[0])
            menu[0].setOnMenuItemClickListener {
                navBarFunctionality(false)
                activity.shadowOverlay.visibility = View.VISIBLE
                fab.hide()
                navBarFunctionality(true)
                true
            }

            val searchItem = menu.findItem(R.id.app_bar_search)
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    navBarFunctionality(false)
                    fab.show()
                    menu[0].collapseActionView()
                    activity.shadowOverlay.visibility = View.GONE
                    navBarFunctionality(true)
                }
            }
        }
    }

    private fun fixSearchView(activity: FragmentActivity, searchMenuItem: MenuItem) {

        val searchView = searchMenuItem.actionView as SearchView
        searchView.setBackgroundColor(activity.getColor(R.color.colorPrimary))

        // id of AutoCompleteTextView
        val searchEditTextId = R.id.search_src_text // for AppCompat

        // get AutoCompleteTextView from SearchView
        val searchEditText = searchView.findViewById<View>(searchEditTextId) as AutoCompleteTextView
        val dropDownAnchor = searchView.findViewById<View>(searchEditText.dropDownAnchor)

        dropDownAnchor?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            // calculate width of DropdownView
            val screenSize = Rect()
            activity.windowManager.defaultDisplay.getRectSize(screenSize)
            // screen width
            val screenWidth = screenSize.width()
            // set DropDownView width
            searchEditText.dropDownWidth = screenWidth
        }

    }

    private fun allowOnBackPressed(onBackPressed: Boolean) {
        (activity as HostActivity).onBackPressed = onBackPressed
    }

    fun hideBottomAppBar() {
        fab.hide()
        bottomAppBar.performHide()
        bottomAppBar.visibility = View.INVISIBLE
    }

    fun showBottomAppBar() {
        bottomAppBar.visibility = View.VISIBLE
        bottomAppBar.performShow()
        fab.show()
    }

    fun displayMessage(view: View, message: String, anchor: Boolean = true) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).run {
            setBackgroundTint(activity.resources.getColor(R.color.materialDarkSurface, null))
            setTextColor(activity.resources.getColor(R.color.white, null))
            if (anchor) setAnchorView(fab.id)
            show()
        }
    }

    fun logout() {
        GlobalScope.launch {
            WorkManager.getInstance(context).cancelAllWork()
            userEncrypt.deleteUser()
            minifluxRepository.clearAll()
            SearchRecentSuggestions(
                context,
                MinifluxSuggestionProvider.AUTHORITY,
                MinifluxSuggestionProvider.MODE
            )
                .clearHistory()
            minifluxRepository.refreshApiService()
            userEncrypt.saveFirstLaunch(true)
            launchLoginActivity()
        }
    }

    fun navBarFunctionality(isEnabled: Boolean, enableFab: Boolean = isEnabled) {
        bottomNavigationDrawerFragment.isEnabled = isEnabled
        if (isEnabled) {
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }
        allowOnBackPressed(isEnabled)

        bottomAppBarMenuActivation(isEnabled)
        fab.isEnabled = enableFab
    }

    private fun bottomAppBarMenuActivation(isEnabled: Boolean) {
        bottomAppBar.run {
            for (i in 0 until menu.size()) {
                menu[i].isEnabled = isEnabled
            }
        }
    }

    private fun BottomNavigationDrawerFragment.showNavigation(
        manager: FragmentManager,
        tag: String?
    ) {
        this.show(
            manager,
            tag
        )
        navBarFunctionality(false)
    }

    fun immersiveMode(isEnabled: Boolean) {
        if (userEncrypt.immersiveMode) {
            if (isEnabled) activity.window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            else activity.window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}