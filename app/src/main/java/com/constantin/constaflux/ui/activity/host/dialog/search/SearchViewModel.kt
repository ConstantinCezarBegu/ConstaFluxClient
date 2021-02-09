package com.constantin.constaflux.ui.activity.host.dialog.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.lazyDeferred
import com.constantin.constaflux.ui.adapters.recycler_view.SearchedEntryBoundaryCallback

class SearchViewModel(
    private val minifluxRepository: MinifluxRepository,
    private val selectedSearch: String,
    private val userEncrypt: UserEncrypt,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val POSITION_RECYCLER_VIEW_SEARCH = "positionRecyclerViewSearch"
    }

    val errorLiveData = minifluxRepository.errorLiveData

    val searchLiveData = userEncrypt.getSearchLiveData()

    var positionRecyclerViewSearch: Int
        get() = handle.get(POSITION_RECYCLER_VIEW_SEARCH) ?: 0
        set(value) {
            handle.set(POSITION_RECYCLER_VIEW_SEARCH, value)
        }

    val searchedEntries by lazyDeferred {
        minifluxRepository.getSearchedEntries(true, selectedSearch).toLiveData(
            pageSize = 20,
            boundaryCallback = SearchedEntryBoundaryCallback(
                viewModelScope,
                minifluxRepository,
                selectedSearch
            )
        )
    }

    suspend fun fetchSearchedEntries() {
        minifluxRepository.fetchEntriesStatus(true, null, null, selectedSearch, null)
    }
}
