package com.constantin.constaflux.ui.adapters.recycler_view

import androidx.lifecycle.MutableLiveData

class EntrySelectedTracker {
    val isSelected = MutableLiveData(false)
    val selectedSize = MutableLiveData(0)
    var isAllSelected = MutableLiveData(false)


    private val entriesSelectedSorage: MutableList<Long> = mutableListOf()
    val entriesSelected: List<Long>
        get() {
            return entriesSelectedSorage.toList()
        }

    fun selectALLID(items: List<Long>) {
        entriesSelectedSorage.clear()
        entriesSelectedSorage.addAll(items)
        selectedSize.value = entriesSelectedSorage.size
        isAllSelected.value = true
    }

    fun isEntrySelected(item: Long): Boolean {
        return item in entriesSelectedSorage
    }

    fun modifyList(item: Long) {
        if (isEntrySelected(item)) {
            entriesSelectedSorage.remove(item)
            selectedSize.value = selectedSize.value!! - 1
            isAllSelected.value = false
        } else {
            entriesSelectedSorage.add(item)
            selectedSize.value = selectedSize.value!! + 1
            if (!isSelected.value!!) isSelected.value = true
        }
    }

    fun closeList() {
        clearList()
        isSelected.value = false
    }

    fun clearList() {
        entriesSelectedSorage.clear()
        selectedSize.value = 0
        isAllSelected.value = false
    }
}