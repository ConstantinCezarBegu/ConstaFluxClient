package com.constantin.constaflux.ui.activity.host.navigation

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.data.db.entity.Feed
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.DialogMode
import com.constantin.constaflux.internal.FragmentContentListMode
import com.constantin.constaflux.internal.FragmentContentMode
import com.constantin.constaflux.ui.activity.host.dialog.category.CategoryDialogViewModel
import com.constantin.constaflux.ui.activity.host.dialog.feed.FeedDialogViewModel
import com.constantin.constaflux.ui.activity.host.dialog.search.SearchViewModel
import com.constantin.constaflux.ui.activity.host.fragments.entry_item.DisplayEntryViewModel
import com.constantin.constaflux.ui.activity.host.fragments.selected.SelectedViewModel
import com.constantin.constaflux.ui.activity.host.fragments.selected_list.SelectedListViewModel
import com.constantin.constaflux.ui.activity.host.fragments.settings.SettingsViewModel

class DisplayEntryViewModelFactory(
    private val context: Context,
    private val userEncrypt: UserEncrypt,
    private val minifluxRepository: MinifluxRepository,
    private val entry: Long,

    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return DisplayEntryViewModel(context, userEncrypt, minifluxRepository, entry, handle) as T
    }
}

class SelectedViewModelFactory(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    private val selectedListMode: FragmentContentListMode,

    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return SelectedViewModel(context, minifluxRepository, selectedListMode, handle) as T
    }
}


class SelectedListViewModelFactory(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt,
    private val selectedId: Long,
    private val selectedMode: FragmentContentMode,

    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return SelectedListViewModel(
            context,
            minifluxRepository,
            userEncrypt,
            selectedId,
            selectedMode, handle
        ) as T
    }
}

class SettingsViewModelFactory(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt,

    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return SettingsViewModel(context, minifluxRepository, userEncrypt, handle) as T
    }
}

class SearchDialogViewModelFactory(
    private val minifluxRepository: MinifluxRepository,
    private val search: String,
    private val userEncrypt: UserEncrypt,

    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return SearchViewModel(minifluxRepository, search, userEncrypt, handle) as T
    }
}

class FeedDialogViewModelFactory(
    private val minifluxRepository: MinifluxRepository,
    private val feedDialogMode: DialogMode,
    private val itemFeed: Feed?,

    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return FeedDialogViewModel(feedDialogMode, itemFeed, minifluxRepository, handle) as T
    }
}

class CategoryDialogViewModelFactory(
    private val minifluxRepository: MinifluxRepository,
    private val categoryDialogMode: DialogMode,
    private val itemCategory: CategoryEntity?,

    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return CategoryDialogViewModel(
            categoryDialogMode,
            itemCategory,
            minifluxRepository,
            handle
        ) as T
    }
}