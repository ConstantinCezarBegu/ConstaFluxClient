package com.constantin.constaflux.ui.adapters.recycler_view

import androidx.paging.PagedList
import com.constantin.constaflux.data.db.entity.Entry
import com.constantin.constaflux.data.repository.MinifluxRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchedEntryBoundaryCallback(
    private val coroutineScope: CoroutineScope,
    private val minifluxRepository: MinifluxRepository,

    private val search: String
) : PagedList.BoundaryCallback<Entry>() {

    override fun onItemAtEndLoaded(itemAtEnd: Entry) {
        coroutineScope.launch(Dispatchers.IO) {
            val timeAfter = itemAtEnd.entryPublishedAt.unixTimeStamp.toString()
            minifluxRepository.fetchEntriesStatus(
                clear = false,
                status = null,
                starred = null,
                search = search,
                after = timeAfter
            )
        }
    }
}