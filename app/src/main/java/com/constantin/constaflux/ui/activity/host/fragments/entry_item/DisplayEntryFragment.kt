package com.constantin.constaflux.ui.activity.host.fragments.entry_item

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.Observer
import com.constantin.constaflux.R
import com.constantin.constaflux.data.network.MinifluxDataSource
import com.constantin.constaflux.internal.observeChange
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.base.ScopedFragment
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.fragment_display_item_entry.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dankito.readability4j.Readability4J
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.io.IOException


class DisplayEntryFragment : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()

    private lateinit var viewModel: DisplayEntryViewModel


    private val client = OkHttpClient()

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_item_entry, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = navigation.getViewModel(this) as DisplayEntryViewModel
        if (savedInstanceState != null) navigation.immersiveMode(true)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
            .addDefaultShareMenuItem()
            .build()
        setUpAppBar(customTabsIntent)
        setUpWebView(customTabsIntent, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        entryContentWebView.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        observeError()
    }

    override fun onDetach() {
        super.onDetach()
        navigation.immersiveMode(false)
    }

    private fun setUpAppBar(customTabsIntent: CustomTabsIntent) {
        navigation.setUpBottomAppBarDisplayFragment()
        navigation.bottomAppBar
            .let { bottomAppBar ->
                observeIconChange(bottomAppBar)
                bottomAppBar.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.starMenuItem -> {
                            viewModel.updateEntryStar()
                        }
                        R.id.readMenuItem -> {
                            viewModel.updateEntry()
                        }
                        R.id.shareMenuItem -> {
                            share(viewModel.entry.entryUrl)
                        }
                        R.id.fetchOriginalMenuItem -> {
                            if (viewModel.originalState) {
                                entryContentWebView.loadData(
                                    prepareContent(viewModel.entry.entryContent),
                                    "text/html",
                                    "UTF-8"
                                )
                                viewModel.originalState = false
                            } else {
                                getOriginalContent(viewModel.entry.entryUrl, bottomAppBar)
                                bottomAppBar.menu[3].isEnabled = false
                            }
                        }
                    }
                    true
                }
            }

        navigation.fab
            .let { fab ->
                fab.setOnClickListener {
                    customTabsIntent.launchUrl(context, Uri.parse(viewModel.entry.entryUrl))
                }
            }
    }

    private fun share(url: String) {
        val entryTitle = viewModel.entry.entryTitle
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_TITLE, entryTitle)
        sendIntent.putExtra(Intent.EXTRA_TEXT, url)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, null))
    }


    private fun observeIconChange(bottomAppBar: BottomAppBar) {

        setUpMenuStar(bottomAppBar.menu[0], viewModel.starState, false)
        setUpMenuRead(bottomAppBar.menu[1], viewModel.statusState, false)
        setUpMenuFetchOriginal(bottomAppBar.menu[3], viewModel.originalState, false)

        viewModel.starStateLiveData.observe(this@DisplayEntryFragment, Observer {
            setUpMenuStar(bottomAppBar.menu[0], it, true)
        })
        viewModel.statusStateLiveDate.observe(this@DisplayEntryFragment, Observer {
            setUpMenuRead(bottomAppBar.menu[1], it, true)
        })
        viewModel.originalStateLiveData.observe(this@DisplayEntryFragment, Observer {
            setUpMenuFetchOriginal(bottomAppBar.menu[3], it, true)
        })
    }

    private fun setUpMenuStar(menuItem: MenuItem, isStarred: Boolean, animation: Boolean) {
        menuItem.let { menu ->
            if (animation) {

                if (isStarred) {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.no_star_to_star
                    )
                    menu.title = resources.getString(R.string.un_star_article)
                } else {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.star_to_no_star
                    )
                    menu.title = resources.getString(R.string.star_article)
                }
                (menu.icon as AnimatedVectorDrawable).start()

            } else {
                if (isStarred) {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_star
                    )
                    menu.title = resources.getString(R.string.un_star_article)
                } else {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_no_star
                    )
                    menu.title = resources.getString(R.string.star_article)
                }
            }
        }
    }

    private fun setUpMenuRead(menuItem: MenuItem, isRead: String, animation: Boolean) {
        menuItem.let { menu ->
            if (animation) {
                if (isRead == "read") {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.mark_as_unread_to_mark_as_read
                    )
                    menu.title = resources.getString(R.string.mark_as_unread)
                } else {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.mark_as_read_to_mark_as_unread
                    )
                    menu.title = resources.getString(R.string.mark_as_read)
                }
                (menu.icon as AnimatedVectorDrawable).start()
            } else {
                if (isRead == "read") {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_mark_as_read
                    )
                    menu.title = resources.getString(R.string.mark_as_unread)
                } else {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_mark_as_unread
                    )
                    menu.title = resources.getString(R.string.mark_as_read)
                }
            }
        }
    }

    private fun setUpMenuFetchOriginal(
        menuItem: MenuItem,
        isFetchOriginal: Boolean,
        animation: Boolean
    ) {
        menuItem.let { menu ->
            if (animation) {
                if (isFetchOriginal) {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.fetch_original_to_undo_fetch_original
                    )
                    menu.title = resources.getString(R.string.undo_fetch_original)
                } else {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.undo_fetch_original_to_fetch_original
                    )
                    menu.title = resources.getString(R.string.fetch_original)
                }
                (menu.icon as AnimatedVectorDrawable).start()
            } else {
                if (isFetchOriginal) {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_undo_fetch_original
                    )
                    menu.title = resources.getString(R.string.undo_fetch_original)
                } else {
                    menu.icon = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.ic_fetch_original_article
                    )
                    menu.title = resources.getString(R.string.fetch_original)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView(customTabsIntent: CustomTabsIntent, savedInstanceState: Bundle?) {

        entryContentWebView.let {

            if (savedInstanceState == null) {
                it.loadData(prepareContent(viewModel.entry.entryContent), "text/html", "UTF-8")
            } else {
                it.restoreState(savedInstanceState)
            }

            it.setBackgroundColor(Color.TRANSPARENT)
            it.webViewClient = WebViewClient()
            it.settings.javaScriptEnabled = true
            it.isVerticalScrollBarEnabled = viewModel.verticalArticleScrollBar
            it.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                    return if (url != null && URLUtil.isValidUrl(url)) {
                        customTabsIntent.launchUrl(context, Uri.parse(url))
                        true
                    } else {
                        false
                    }
                }
            }
            it.webChromeClient = object : WebChromeClient() {
                private lateinit var fullScreenView: View

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)

                    if (view is FrameLayout) {
                        navigation.hideBottomAppBar()
                        fullScreenView = view
                        fullScreenContainer.addView(fullScreenView)
                        fullScreenContainer.visibility = VISIBLE
                        mainContainer.visibility = GONE
                    }
                }

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    navigation.showBottomAppBar()
                    fullScreenContainer.removeView(fullScreenView)
                    fullScreenContainer.visibility = GONE
                    mainContainer.visibility = VISIBLE
                }
            }
            it.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY - oldScrollY > 0) {
                    (requireActivity() as AppCompatActivity).findViewById<BottomAppBar>(R.id.bottomAppBar)
                        .performHide()

                } else if (scrollY - oldScrollY < 0) {
                    (requireActivity() as AppCompatActivity).findViewById<BottomAppBar>(R.id.bottomAppBar)
                        .performShow()
                }
            }
            it.setOnCreateContextMenuListener { _, _, _ ->

                val hitTestResult: WebView.HitTestResult = it.hitTestResult

                val url: String? = hitTestResult.extra

                if (url != null && URLUtil.isValidUrl(url)) {
                    share(url)
                }
            }
        }
    }

    private fun getOriginalContent(
        url: String,
        bottomAppBar: BottomAppBar
    ) {
        launch(Dispatchers.IO) {
            try {

                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                if (response.body() != null) {
                    val readability4J = Readability4J(url, response.body()!!.string())
                    val article = readability4J.parse()
                    val extractedContentHtmlWithUtf8Encoding = article.contentWithUtf8Encoding!!

                    val text = prepareContent(extractedContentHtmlWithUtf8Encoding)

                    launch(Dispatchers.Main) {
                        entryContentWebView.loadData(
                            text,
                            "text/html",
                            "UTF-8"
                        )
                    }
                }
                launch(Dispatchers.Main) {
                    viewModel.originalState = true
                    bottomAppBar.menu[3].isEnabled = true
                }
            } catch (e: IOException) {
                viewModel.errorLiveData.postValue(MinifluxDataSource.HttpErrors.INTERNET_CONNECTION)
            }
        }

    }


    private fun prepareContent(body: String): String {
        val author =
            if (viewModel.entry.entryAuthor.isNotBlank()) " - ${viewModel.entry.entryAuthor}" else ""
        val category =
            if (viewModel.entry.categoryTitle.isNotBlank()) " - ${viewModel.entry.categoryTitle}" else ""
        val title = "<h1>${viewModel.entry.entryTitle}</h1>"
        val icon =
            if (!viewModel.entry.feedIcon.isNullOrEmpty()) "<img id=\"feedIcon\" src=\"${"data:" + viewModel.entry.feedIcon}\"/> " else ""
        val subtitle = "<p>$icon${viewModel.entry.feedTitle}$author$category</p>"

        return ("<html><head>"
                + "<style type=\"text/css\">"
                + "::selection{ background: #44906E; color:#FFFFFF; }"
                + "img { display: block; margin-left: auto; margin-right: auto; width: 100%;  height: auto; }"
                + "body {padding: 1em 1em; color: #fff;}"
                + "#feedIcon { display: inline; margin-left : 0; margin-right : 0;  height: 1em; width: auto; }"
                + "iframe {width: 100%;  height: auto;} "
                + "a{color: ${String.format(
            "#%06X",
            0xFFFFFF and ContextCompat.getColor(context!!, R.color.colorAccent)
        )};}"
                + "</style></head>"
                + "<body>"
                + title
                + subtitle
                + "<hr>"
                + "<br>"
                + body
                + "<br>"
                + "<br>"
                + "<br>"
                + "</body></html>")
    }

    private fun observeError() {
        viewModel.errorLiveData.let {
            it.removeObservers(this@DisplayEntryFragment)
            it.observeChange(this@DisplayEntryFragment, Observer { error ->
                when (error) {
                    MinifluxDataSource.HttpErrors.SUCCESS -> {
                    }
                    MinifluxDataSource.HttpErrors.INTERNET_CONNECTION -> {
                        navigation.displayMessage(
                            this@DisplayEntryFragment.requireView(),
                            "No Internet"
                        )
                    }
                    MinifluxDataSource.HttpErrors.AUTHENTICATION -> {
                        navigation.logout()
                    }
                    MinifluxDataSource.HttpErrors.HTTP_ERROR -> {
                        navigation.displayMessage(this@DisplayEntryFragment.requireView(), "Error")
                    }
                }
            })
        }
    }


}