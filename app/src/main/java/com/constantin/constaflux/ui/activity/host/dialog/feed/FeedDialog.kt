package com.constantin.constaflux.ui.activity.host.dialog.feed

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.lifecycle.Observer
import com.constantin.constaflux.R
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.data.network.response.feed.CreateFeedsRequest
import com.constantin.constaflux.data.network.response.feed.UpdateFeedsRequest
import com.constantin.constaflux.internal.DialogMode
import com.constantin.constaflux.internal.categoryExtractTitle
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.base.ScopedDialog
import kotlinx.android.synthetic.main.dialog_feed.*
import kotlinx.android.synthetic.main.dialog_promt.view.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class FeedDialog :
    ScopedDialog(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()
    private lateinit var viewModel: FeedDialogViewModel

    private var feedCategoryArray = arrayOf<CategoryEntity>()

    companion object {
        @JvmStatic
        fun newInstance() =
            FeedDialog()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        navigation.navBarFunctionality(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_feed, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = navigation.getViewModel(this) as FeedDialogViewModel
        extractList()
        setUpBasedOnMode()
        restorePersistence()
    }

    override fun onPause() {
        super.onPause()
        savePersistence()
    }

    private fun savePersistence() {
        viewModel.run {
            feedTitle = titleFeedEditText.text.toString()
            siteUrl = siteUrlEditText.text.toString()
            feedUrl = feedUrlEditText.text.toString()
            viewModel.fetchOriginalUrl = scrapperSwitch.isChecked
            categorySpinnerPosition = feedCategorySelectionSpinner.selectedItemPosition
            overrideDefaultUserAgent = feedUserAgentEditText.text.toString()
            feedUsername = feedUsernameEditText.text.toString()
            feedPassword = feedPasswordEditText.text.toString()
            scraperRules = scraperRulesEditText.text.toString()
            rewriteRules = rewriteRulesEditText.text.toString()
        }
    }

    private fun restorePersistence() {
        viewModel.run {
            titleFeedEditText.setText(feedTitle)
            siteUrlEditText.setText(siteUrl)
            feedUrlEditText.setText(feedUrl)
            scrapperSwitch.isChecked = viewModel.fetchOriginalUrl
            feedUserAgentEditText.setText(overrideDefaultUserAgent)
            feedUsernameEditText.setText(feedUsername)
            feedPasswordEditText.setText(feedPassword)
            scraperRulesEditText.setText(scraperRules)
            rewriteRulesEditText.setText(rewriteRules)
            dialogStateLiveDate.observe(this@FeedDialog, Observer {
                if (it) showDialogFeedDelete(view!!, view!! as ViewGroup)
            })
            advancedOptionsLiveData.observe(this@FeedDialog, Observer {
                showAdvanced(it)
            })
        }

    }

    private fun extractList() {
        launch {
            viewModel.feedCategoryList.await().observe(this@FeedDialog, Observer {
                feedCategoryArray = it.toTypedArray()
                val feedCategoryString = feedCategoryArray.categoryExtractTitle()

                if (feedCategoryString.isNotEmpty()) {
                    val spinnerArrayAdapter =
                        ArrayAdapter(
                            this@FeedDialog.context!!,
                            android.R.layout.simple_spinner_item,
                            feedCategoryString
                        )
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    feedCategorySelectionSpinner.adapter = spinnerArrayAdapter

                    // Set the position of the spinner if in update mode (since it already has a selection)
                    if (viewModel.mode == DialogMode.Update && viewModel.item != null) {
                        feedCategorySelectionSpinner.setSelection(
                            feedCategoryString.indexOf(
                                viewModel.item?.categoryTitle
                            )
                        )
                    }
                }
            })
        }
    }

    private fun setUpBasedOnMode() {

        if (viewModel.mode == DialogMode.Create) {
            serUpUpdateView(false)
            dialog_feed_toolbar.menu[0].setOnMenuItemClickListener {
                createFeed()
                true
            }
        } else if (viewModel.mode == DialogMode.Update) {
            serUpUpdateView(true)
            deleteFeedButton.setOnClickListener {
                deleteFeedButton.isEnabled = false
                viewModel.dialogState = true
            }
            dialog_feed_toolbar.menu[0].setOnMenuItemClickListener {
                updateFeed()
                true
            }
        }

        dialog_feed_toolbar.setNavigationOnClickListener {
            dismiss()
        }

        showAdvancedOptionsFeedButton.setOnClickListener {
            viewModel.advancedOptions = !viewModel.advancedOptions
        }
    }

    private fun createFeed() {
        if (feedCategoryArray.isNotEmpty()) {
            val feedUrl = feedUrlEditText.text.toString()
            val category = feedCategoryArray[feedCategorySelectionSpinner.selectedItemPosition]
            val crawler = scrapperSwitch.isChecked
            val username = feedUserAgentEditText.text.toString()
            val password = feedUsernameEditText.text.toString()
            val feedUserAgent = feedUserAgentEditText.text.toString()

            if (feedUrl.isNotBlank()) {
                (parentFragment as FeedInteraction).createFeed(
                    CreateFeedsRequest(
                        feedUrl,
                        category.categoryId,
                        if (username.isNotBlank()) username else null,
                        if (password.isNotBlank()) password else null,
                        crawler,
                        if (feedUserAgent.isNotBlank()) feedUserAgent else null
                    )
                )
                dismiss()
            }
        }
    }

    private fun serUpUpdateView(isActive: Boolean) {
        if (isActive) {
            dialog_feed_toolbar.title = getString(R.string.update_feed)
            updateFeedLayout.visibility = View.VISIBLE
            updateFeedLayoutAdvanced.visibility = View.VISIBLE
        } else {
            updateFeedLayout.visibility = View.GONE
            updateFeedLayoutAdvanced.visibility = View.GONE
        }
    }

    private fun updateFeed() {
        if (feedCategoryArray.isNotEmpty()) {
            val feedUrl = feedUrlEditText.text.toString()
            val siteUrl = siteUrlEditText.text.toString()
            val title = titleFeedEditText.text.toString()
            val category =
                feedCategoryArray[feedCategorySelectionSpinner.selectedItemPosition].categoryId

            val scraperRules = scraperRulesEditText.text.toString()
            val rewriteRules = rewriteRulesEditText.text.toString()
            val crawler = scrapperSwitch.isChecked
            val username = feedUsernameEditText.text.toString()
            val password = feedPasswordEditText.text.toString()
            val userAgent = feedUserAgentEditText.text.toString()


            if (feedUrl.isNotBlank() && siteUrl.isNotBlank() && title.isNotBlank()) {
                (parentFragment as FeedInteraction).updateFeed(
                    viewModel.item!!.feedId,
                    UpdateFeedsRequest(
                        feedUrl,
                        siteUrl,
                        title,
                        category,

                        if (scraperRules.isNotBlank()) scraperRules else null,
                        if (rewriteRules.isNotBlank()) rewriteRules else null,
                        crawler,
                        if (username.isNotBlank()) username else null,
                        if (password.isNotBlank()) password else null,
                        if (userAgent.isNotBlank()) userAgent else null
                    )
                )
                dismiss()
            }
        }
    }

    private fun showAdvanced(showAdvanced: Boolean) {
        if (showAdvanced) {
            showAdvancedOptionsFeedButton.text = getString(R.string.hide_advanced_options)
            showAdvancedOptionsFeedLayout.visibility = View.VISIBLE
        } else {
            showAdvancedOptionsFeedButton.text = getString(R.string.show_advanced_options)
            showAdvancedOptionsFeedLayout.visibility = View.GONE
        }
    }

    private fun showDialogFeedDelete(
        view: View,
        parent: ViewGroup
    ) {
        val mBuilder: AlertDialog.Builder = AlertDialog.Builder(view.context)
        val mView: View = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_promt, parent, false)
        mBuilder.setView(mView)
        val dialog: AlertDialog = mBuilder.create()

        mView.promtCancel.setOnClickListener {
            dialog.dismiss()
        }

        mView.promptAccept.setOnClickListener {
            (parentFragment as FeedInteraction).deleteFeed(viewModel.item!!.feedId)
            dialog.dismiss()
            this@FeedDialog.dismiss()
        }

        dialog.setOnDismissListener {
            viewModel.dialogState = false
            deleteFeedButton.isEnabled = true
        }

        dialog.show()
    }

    interface FeedInteraction {
        fun createFeed(createFeedRequest: CreateFeedsRequest)
        fun updateFeed(id: Long, updateFeedsRequest: UpdateFeedsRequest)
        fun deleteFeed(id: Long)
    }
}