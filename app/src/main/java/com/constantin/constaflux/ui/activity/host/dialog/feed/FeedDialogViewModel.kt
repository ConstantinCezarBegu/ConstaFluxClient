package com.constantin.constaflux.ui.activity.host.dialog.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.constantin.constaflux.data.db.entity.Feed
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.DialogMode
import com.constantin.constaflux.internal.lazyDeferred

class FeedDialogViewModel(
    val mode: DialogMode,
    val item: Feed?,
    private val minifluxRepository: MinifluxRepository,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val DIALOG_STATE = "dialogState"
        const val ADVANCED_OPTIONS = "advancedOptions"
        const val FEED_TITLE_HANDLE = "feedTitle"
        const val SITE_URL_HANDLE = "siteUrl"
        const val FEED_URL_HANDLE = "feedUrl"
        const val FETCH_ORIGINAL_URL_HANDLE = "fetchOriginalUrl"
        const val CATEGORY_SPINNER_POSITION_HANDLE = "categorySpinnerPosition"
        const val OVERRIDE_DEFAULT_USER_AGENT_HANDLE = "overrideDefaultUserAgent"
        const val FEED_USERNAME_HANDLE = "feedUsername"
        const val FEED_PASSWORD_HANDLE = "feedPassword"
        const val SCRAPER_RULES_HANDLE = "scraperRules"
        const val REWRITE_RULES_HANDLE = "rewriteRules"
    }

    var dialogState: Boolean
        get() = handle.get(DIALOG_STATE) ?: false
        set(value) {
            handle.set(DIALOG_STATE, value)
        }

    val dialogStateLiveDate = handle.getLiveData<Boolean>(DIALOG_STATE)

    var advancedOptions: Boolean
        get() = handle.get(ADVANCED_OPTIONS) ?: false
        set(value) {
            handle.set(ADVANCED_OPTIONS, value)
        }

    val advancedOptionsLiveData = handle.getLiveData<Boolean>(ADVANCED_OPTIONS)

    var feedTitle: String
        get() = handle.get(FEED_TITLE_HANDLE) ?: (item?.feedTitle) ?: ""
        set(value) {
            handle.set(FEED_TITLE_HANDLE, value)
        }

    var siteUrl: String
        get() = handle.get(SITE_URL_HANDLE) ?: (item?.feedSiteUrl) ?: ""
        set(value) {
            handle.set(SITE_URL_HANDLE, value)
        }

    var feedUrl: String
        get() = handle.get(FEED_URL_HANDLE) ?: (item?.feedUrl) ?: ""
        set(value) {
            handle.set(FEED_URL_HANDLE, value)
        }

    var fetchOriginalUrl: Boolean
        get() = handle.get(FETCH_ORIGINAL_URL_HANDLE) ?: (item?.feedCrawler) ?: false
        set(value) {
            handle.set(FETCH_ORIGINAL_URL_HANDLE, value)
        }

    var categorySpinnerPosition: Int
        get() = handle.get(CATEGORY_SPINNER_POSITION_HANDLE) ?: 0
        set(value) {
            handle.set(CATEGORY_SPINNER_POSITION_HANDLE, value)
        }

    var overrideDefaultUserAgent: String
        get() = handle.get(OVERRIDE_DEFAULT_USER_AGENT_HANDLE) ?: item?.feedUserAgent ?: ""
        set(value) {
            handle.set(OVERRIDE_DEFAULT_USER_AGENT_HANDLE, value)
        }

    var feedUsername: String
        get() = handle.get(FEED_USERNAME_HANDLE) ?: item?.feedUsername ?: ""
        set(value) {
            handle.set(FEED_USERNAME_HANDLE, value)
        }

    var feedPassword: String
        get() = handle.get(FEED_PASSWORD_HANDLE) ?: item?.feedPassword ?: ""
        set(value) {
            handle.set(FEED_PASSWORD_HANDLE, value)
        }

    var scraperRules: String
        get() = handle.get(SCRAPER_RULES_HANDLE) ?: item?.feedScraperRules ?: ""
        set(value) {
            handle.set(SCRAPER_RULES_HANDLE, value)
        }

    var rewriteRules: String
        get() = handle.get(REWRITE_RULES_HANDLE) ?: item?.feedRewriteRules ?: ""
        set(value) {
            handle.set(REWRITE_RULES_HANDLE, value)
        }

    val feedCategoryList by lazyDeferred {
        minifluxRepository.getCategories()
    }

}