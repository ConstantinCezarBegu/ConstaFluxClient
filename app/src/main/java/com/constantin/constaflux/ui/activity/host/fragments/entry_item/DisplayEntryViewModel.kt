package com.constantin.constaflux.ui.activity.host.fragments.entry_item

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.constantin.constaflux.data.encrypt.UserEncrypt
import com.constantin.constaflux.data.network.response.entry.UpdateEntriesRequest
import com.constantin.constaflux.data.repository.MinifluxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DisplayEntryViewModel(
    private val context: Context,
    private val userEncrypt: UserEncrypt,
    private val minifluxRepository: MinifluxRepository,
    private val entryID: Long,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val STAR_STATE = "starState"
        const val STATUS_STATE = "statusState"
        const val FETCH_ORIGINAL_STATE = "fetchOriginalState"
    }

    val entry = minifluxRepository.getEntryDisplay(entryID)

    val errorLiveData = minifluxRepository.errorLiveData

    val verticalArticleScrollBar: Boolean
        get() {
            return userEncrypt.articleScrollBar
        }

    var starState: Boolean
        get() = handle.get<Boolean>(STAR_STATE) ?: entry.entryStarred
        set(value) {
            handle.set(STAR_STATE, value)
        }

    val starStateLiveData = handle.getLiveData<Boolean>(STAR_STATE)

    var statusState: String
        get() = handle.get<String>(STATUS_STATE) ?: entry.entryStatus
        set(value) {
            handle.set(STATUS_STATE, value)
        }

    val statusStateLiveDate = handle.getLiveData<String>(STATUS_STATE)

    var originalState: Boolean
        get() = handle.get<Boolean>(FETCH_ORIGINAL_STATE) ?: false
        set(value) {
            handle.set(FETCH_ORIGINAL_STATE, value)
        }

    val originalStateLiveData = handle.getLiveData<Boolean>(FETCH_ORIGINAL_STATE)

    fun updateEntryStar() {
        updateStarDatabase()
        viewModelScope.launch(Dispatchers.IO) {
            if (!minifluxRepository.updateEntryStar(entry.entryId)) {
                updateStarDatabase()
            }
        }
    }

    private fun updateStarDatabase() {
        entry.entryStarred = !entry.entryStarred
        viewModelScope.launch(Dispatchers.Main) {
            starState = entry.entryStarred
            minifluxRepository.updateEntryStar(entry.entryId)
        }

    }


    fun updateEntry() {
        updateStatusDatabase()
        viewModelScope.launch(Dispatchers.IO) {
            if (!minifluxRepository.updateEntries(
                    UpdateEntriesRequest(
                        listOf(
                            entry.entryId
                        ), entry.entryStatus
                    )
                )
            ) {
                updateStatusDatabase()
            }
        }
    }


    private fun updateStatusDatabase() {
        if (entry.entryStatus == "unread") entry.entryStatus = "read"
        else entry.entryStatus = "unread"
        viewModelScope.launch(Dispatchers.Main) {
            statusState = entry.entryStatus
            minifluxRepository.updateEntryStatus(listOf(entry.entryId), entry.entryStatus)
        }

    }
}