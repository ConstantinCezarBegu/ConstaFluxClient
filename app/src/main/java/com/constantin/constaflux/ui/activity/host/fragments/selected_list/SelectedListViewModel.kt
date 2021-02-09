package com.constantin.constaflux.ui.activity.host.fragments.selected_list

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.toLiveData
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.network.response.entry.UpdateEntriesRequest
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.FragmentContentMode
import com.constantin.constaflux.internal.lazyDeferred
import com.constantin.constaflux.ui.adapters.recycler_view.EntryBoundaryCallback
import com.constantin.constaflux.ui.adapters.recycler_view.EntrySelectedTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectedListViewModel(
    private val context: Context,
    private val minifluxRepository: MinifluxRepository,
    private val userEncrypt: UserEncrypt,

    private val selectedId: Long,
    private val selectedMode: FragmentContentMode,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val STATUS = "status"
        const val POSITION_RECYCLER_VIEW_UNREAD = "positionRecyclerViewUnread"
    }

    val errorLiveData = minifluxRepository.errorLiveData

    val entrySelectedTracker = EntrySelectedTracker()

    val fabMode = userEncrypt.articleListFabAction

    val longPressMode = userEncrypt.articleLongPressAction

    var status: String
        get() = handle.get<String>(STATUS) ?: "unread"
        set(value) {
            handle.set(STATUS, value)
        }

    var positionRecyclerViewUnread: Int
        get() = handle.get<Int>(POSITION_RECYCLER_VIEW_UNREAD) ?: 0
        set(value) {
            handle.set(POSITION_RECYCLER_VIEW_UNREAD, value)
        }

    val selectedEntriesStatusALLID by lazyDeferred {
        when (selectedMode) {
            FragmentContentMode.All -> {
                minifluxRepository.getEntriesALLID(
                    status = status,
                    starred = null
                )
            }
            FragmentContentMode.Starred -> {
                minifluxRepository.getEntriesALLID(
                    status = status,
                    starred = true
                )
            }
            FragmentContentMode.Feeds -> {
                minifluxRepository.getFeedEntriesALLID(
                    status = status,
                    feedId = selectedId
                )
            }
            FragmentContentMode.Category -> {
                minifluxRepository.getCategoryEntriesALLID(
                    status = status,
                    categoryId = selectedId
                )
            }
        }
    }

    val selectedEntriesStatus by lazyDeferred {
        when (selectedMode) {
            FragmentContentMode.All -> {
                minifluxRepository.getEntries(
                    clear = true,
                    status = status,
                    starred = null
                )
            }
            FragmentContentMode.Starred -> {
                minifluxRepository.getEntries(
                    clear = true,
                    status = status,
                    starred = true
                )
            }
            FragmentContentMode.Feeds -> {
                minifluxRepository.getFeedEntries(
                    clear = true,
                    status = status,
                    feedId = selectedId
                )
            }
            FragmentContentMode.Category -> {
                minifluxRepository.getCategoryEntries(
                    clear = true,
                    status = status,
                    categoryId = selectedId
                )
            }
        }.toLiveData(
            pageSize = 20,
            boundaryCallback =
            if (selectedMode == FragmentContentMode.Category) {
                null
            } else {
                EntryBoundaryCallback(
                    viewModelScope,
                    minifluxRepository,
                    selectedId, selectedMode, status, entrySelectedTracker
                )
            }
        )
    }


    suspend fun fetchSelectedEntries() {
        when (selectedMode) {

            FragmentContentMode.All -> {
                minifluxRepository.fetchEntriesStatus(
                    clear = true,
                    status = status,
                    starred = null,
                    search = null,
                    after = null
                )
            }
            FragmentContentMode.Starred -> {
                minifluxRepository.fetchEntriesStatus(
                    clear = true,
                    status = status,
                    starred = true,
                    search = null,
                    after = null
                )
            }
            FragmentContentMode.Feeds -> {
                minifluxRepository.fetchFeedEntries(
                    clear = true,
                    status = status,
                    feedId = selectedId,
                    after = null
                )
            }
            FragmentContentMode.Category -> {
                minifluxRepository.fetchEntriesStatus(
                    clear = true,
                    status = status,
                    starred = null,
                    search = null,
                    after = null
                )
            }
        }
    }

    fun updateEntryStatus(
        entries: List<Long>,
        failFunction: (() -> Unit)? = null
    ) {
        val status = if (this@SelectedListViewModel.status == "unread") "read"
        else "unread"

        if (entries.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                updateDatabaseEntryStatus(entries, status)
                if (!minifluxRepository.updateEntries(
                        UpdateEntriesRequest(
                            entries,
                            status
                        )
                    )
                ) {
                    updateDatabaseEntryStatus(
                        entries,
                        this@SelectedListViewModel.status
                    )
                    failFunction?.invoke()
                }
            }
        }
    }

    private fun updateDatabaseEntryStatus(
        entries: List<Long>,
        status: String
    ) {
        minifluxRepository.updateEntryStatus(entries, status)
    }


    fun updateEntryStar(
        entries: List<Long>
    ) {
        viewModelScope.launch {
            entries.forEach {
                viewModelScope.launch {
                    if (minifluxRepository.updateEntryStar(it)) {
                        updateDatabaseEntryStar(listOf(it))
                    }
                }
            }
        }
    }

    private fun updateDatabaseEntryStar(
        entries: List<Long>
    ) {
        minifluxRepository.updateEntryStar(entries)
    }

}