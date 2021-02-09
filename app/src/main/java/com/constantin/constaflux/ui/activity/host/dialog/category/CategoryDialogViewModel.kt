package com.constantin.constaflux.ui.activity.host.dialog.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.data.repository.MinifluxRepository
import com.constantin.constaflux.internal.DialogMode

class CategoryDialogViewModel(
    val mode: DialogMode,
    val item: CategoryEntity?,
    private val minifluxRepository: MinifluxRepository,
    private val handle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val DIALOG_STATE = "dialogState"
        const val CATEGORY_NAME_HANDLE = "urlHandleLoginViewModel"
    }

    var dialogState: Boolean
        get() = handle.get(DIALOG_STATE) ?: false
        set(value) {
            handle.set(DIALOG_STATE, value)
        }

    val dialogStateLiveDate = handle.getLiveData<Boolean>(DIALOG_STATE)


    var categoryName: String
        get() = handle.get(CATEGORY_NAME_HANDLE) ?: (item?.categoryTitle) ?: ""
        set(value) {
            handle.set(CATEGORY_NAME_HANDLE, value)
        }


}