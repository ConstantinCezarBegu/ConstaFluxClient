package com.constantin.constaflux.ui.adapters.recycler_view

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.constantin.constaflux.internal.SelectedMode
import com.constantin.constaflux.internal.enableDisableSwipeRefresh


class SwipeToDeleteCallback(
    private val swipeRefreshLayout: SwipeRefreshLayout,
    private val onEntryRecyclerSwipeListener: OnEntryRecyclerSwipeListener,
    private val adapter: MinifluxPagedListAdapter,
    private val selectedMode: SelectedMode
) :
    ItemTouchHelper.SimpleCallback(
        0,
        if (selectedMode == SelectedMode.Unread) ItemTouchHelper.RIGHT else ItemTouchHelper.LEFT
    ) {

    private var enableSwipeRefreshLayout = false

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }


    fun stopSwipeDir() {
        setDefaultSwipeDirs(0)
    }

    fun allowSwipeDir() {
        setDefaultSwipeDirs(if (selectedMode == SelectedMode.Unread) ItemTouchHelper.RIGHT else ItemTouchHelper.LEFT)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        enableDisableSwipeRefresh(enableSwipeRefreshLayout, swipeRefreshLayout)
        enableSwipeRefreshLayout = !enableSwipeRefreshLayout
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val itemPosition = viewHolder.adapterPosition
        onEntryRecyclerSwipeListener.onEntrySwipeListener(adapter.getEntry(itemPosition)!!.entryId)
    }

    interface OnEntryRecyclerSwipeListener {
        fun onEntrySwipeListener(item: Long)
    }
}