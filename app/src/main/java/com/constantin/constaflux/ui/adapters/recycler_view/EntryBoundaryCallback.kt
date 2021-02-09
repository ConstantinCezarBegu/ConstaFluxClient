package com.constantin.constaflux.ui.adapters.recycler_view

import androidx.paging.PagedList
import com.constantin.constaflux.data.db.entity.Entry
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.FragmentContentMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EntryBoundaryCallback(
    private val coroutineScope: CoroutineScope,
    private val minifluxRepository: MinifluxRepository,

    private val selectedId: Long,
    private val selectedMode: FragmentContentMode,
    private val status: String,
    private val tracker: EntrySelectedTracker? = null

) : PagedList.BoundaryCallback<Entry>() {

    override fun onZeroItemsLoaded() {
        coroutineScope.launch {
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
    }

    override fun onItemAtEndLoaded(itemAtEnd: Entry) {
        coroutineScope.launch(Dispatchers.IO) {
            if ((tracker == null) || (tracker.isSelected.value!!.not())) {
                val timeAfter = itemAtEnd.entryPublishedAt.unixTimeStamp.toString()
                when (selectedMode) {
                    FragmentContentMode.All -> {
                        minifluxRepository.fetchEntriesStatus(
                            clear = false,
                            status = status,
                            after = timeAfter,
                            starred = null,
                            search = null
                        )
                    }
                    FragmentContentMode.Starred -> {
                        minifluxRepository.fetchEntriesStatus(
                            clear = false,
                            status = status,
                            starred = true,
                            after = timeAfter,
                            search = null
                        )
                    }
                    FragmentContentMode.Feeds -> {
                        minifluxRepository.fetchFeedEntries(
                            clear = false,
                            status = status,
                            feedId = selectedId,
                            after = timeAfter
                        )
                    }
                    FragmentContentMode.Category -> {
                        minifluxRepository.fetchEntriesStatus(
                            clear = false,
                            status = status,
                            after = timeAfter,
                            starred = null,
                            search = null
                        )
                    }
                }
            }
        }
    }
}
