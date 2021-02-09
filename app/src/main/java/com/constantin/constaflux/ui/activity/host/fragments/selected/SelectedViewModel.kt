package com.constantin.constaflux.ui.activity.host.fragments.selected

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constantin.constaflux.data.network.response.category.CategoryRequest
import com.constantin.constaflux.data.network.response.feed.CreateFeedsRequest
import com.constantin.constaflux.data.network.response.feed.UpdateFeedsRequest
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.FragmentContentListMode
import com.constantin.constaflux.internal.lazyDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectedViewModel(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    val selectedListMode: FragmentContentListMode,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val POSITION_RECYCLER_VIEW = "positionRecyclerView"
    }

    val errorLiveData = minifluxRepository.errorLiveData

    var positionRecyclerView: Int
        get() = handle.get<Int>(POSITION_RECYCLER_VIEW) ?: 0
        set(value) {
            handle.set(POSITION_RECYCLER_VIEW, value)
        }

    val selected by lazyDeferred {
        when (selectedListMode) {
            FragmentContentListMode.Feeds -> {
                minifluxRepository.getFeeds()
            }
            FragmentContentListMode.Category -> {
                minifluxRepository.getCategories()
            }
        }
    }

    suspend fun fetchSelected() {
        when (selectedListMode) {
            FragmentContentListMode.Feeds -> {
                minifluxRepository.fetchFeeds(viewModelScope)
            }
            FragmentContentListMode.Category -> {
                minifluxRepository.fetchCategories()
            }
        }
    }

    fun createCategory(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (minifluxRepository.addCategory(CategoryRequest(title))) {
                minifluxRepository.fetchCategories()
            }
        }
    }

    fun updateCategory(id: Long, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (minifluxRepository.updateCategory(id, CategoryRequest(title))) {
                minifluxRepository.fetchCategories()
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (minifluxRepository.deleteCategory(id)) {
                minifluxRepository.fetchCategories()
            }
        }
    }

    fun createFeed(createFeedRequest: CreateFeedsRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            if (minifluxRepository.addFeed(createFeedRequest)) {
                minifluxRepository.fetchFeeds(this)
            }
        }
    }

    fun updateFeed(id: Long, updateFeedsRequest: UpdateFeedsRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            if (minifluxRepository.updateFeed(id, updateFeedsRequest)) {
                minifluxRepository.fetchCategories()
            }
        }
    }

    fun deleteFeed(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (minifluxRepository.deleteFeed(id)) {
                minifluxRepository.fetchFeeds(this)
            }
        }
    }
}