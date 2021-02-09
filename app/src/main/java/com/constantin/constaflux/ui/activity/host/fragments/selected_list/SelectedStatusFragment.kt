package com.constantin.constaflux.ui.activity.host.fragments.selected_list

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.constantin.constaflux.R
import com.constantin.constaflux.data.network.MinifluxDataSource
import com.constantin.constaflux.internal.*
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.adapters.recycler_view.MinifluxPagedListAdapter
import com.constantin.constaflux.ui.adapters.recycler_view.SwipeToDeleteCallback
import com.constantin.constaflux.ui.base.ScopedFragment
import kotlinx.android.synthetic.main.dialog_promt.view.*
import kotlinx.android.synthetic.main.fragment_selected_list.*
import kotlinx.android.synthetic.main.fragment_selected_status.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class SelectedStatusFragment :
    ScopedFragment(), KodeinAware,
    MinifluxPagedListAdapter.OnRecyclerOnClickListener,
    SwipeToDeleteCallback.OnEntryRecyclerSwipeListener {

    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()
    private var tabStatus: String? = null
    private lateinit var viewModel: SelectedListViewModel

    private lateinit var minifluxPagedListAdapter: MinifluxPagedListAdapter
    private lateinit var swipeToDeleteCallback: SwipeToDeleteCallback

    private var selectALLID = listOf<Long>()

    private var mLastClickTime = System.currentTimeMillis()
    private var mLastLongClickTime = System.currentTimeMillis()
    private val CLICK_TIME_INTERVAL: Long = 200

    companion object {
        @JvmStatic
        fun newInstance(status: String): SelectedStatusFragment {
            val fragment = SelectedStatusFragment()
            fragment.tabStatus = status
            return fragment
        }

    }

    override fun onRecyclerViewClickListener(position: Int, item: Any) {
        val now = System.currentTimeMillis()
        if (now - mLastClickTime < CLICK_TIME_INTERVAL) {
            return
        }
        mLastClickTime = now

        if (item is Long) {
            if (viewModel.entrySelectedTracker.isSelected.value!!) {
                viewModel.entrySelectedTracker.modifyList(item)
                minifluxPagedListAdapter.notifyItemChanged(position)
            } else {
                if (viewModel.status == "unread") viewModel.updateEntryStatus(listOf(item))
                navigation.launchDisplayEntryFragment(item)
            }
        }
    }

    override fun onRecyclerViewLongClickListener(position: Int, item: Any) {
        val now = System.currentTimeMillis()
        if (now - mLastLongClickTime < CLICK_TIME_INTERVAL) {
            return
        }
        mLastLongClickTime = now

        if (item is Long) {
            if (viewModel.entrySelectedTracker.isSelected.value!!) {
                viewModel.entrySelectedTracker.modifyList(item)
                minifluxPagedListAdapter.notifyItemChanged(position)
            } else {
                if (viewModel.longPressMode == 0) viewModel.updateEntryStar(listOf(item))
                else if (viewModel.longPressMode == 1) {
                    navigation.navBarFunctionality(false)
                    viewModel.entrySelectedTracker.isSelected.postValue(true)
                    viewModel.entrySelectedTracker.modifyList(item)
                    minifluxPagedListAdapter.notifyItemChanged(position)
                }
            }
        }
    }

    override fun onEntrySwipeListener(item: Long) {
        viewModel.updateEntryStatus(listOf(item)) {
            launch(Dispatchers.Main) {
                minifluxPagedListAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selected_status, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = navigation.getViewModel(this) as SelectedListViewModel
        if (tabStatus != null) viewModel.status = tabStatus!!

        if (savedInstanceState == null) {
            selectedListSwipeRefreshLayoutUnread.isRefreshing = true
        }


        setUpSwipeToRefreshLayout()
        setUpRecyclerView()
        bindUI()
    }

    override fun onResume() {
        super.onResume()
        setUpBottomBar()
        setUpSelectionGestures()
        parentFragment?.selectedViewPager?.let {
            it.direction =
                if (viewModel.status == "unread") SwipeDirection.Right else SwipeDirection.Left
        }
        observeError()
    }

    override fun onStop() {
        super.onStop()
        val layoutManager = selectedUnreadRecyclerView.layoutManager
        if (layoutManager != null && layoutManager is LinearLayoutManager) {
            viewModel.positionRecyclerViewUnread = layoutManager.findFirstVisibleItemPosition()
        }
    }

    private fun observeError() {
        viewModel.errorLiveData.let {
            it.removeObservers(this@SelectedStatusFragment)
            it.observeChange(this@SelectedStatusFragment, Observer { error ->
                when (error) {
                    MinifluxDataSource.HttpErrors.SUCCESS -> {
                        selectedListSwipeRefreshLayoutUnread.isRefreshing = false
                    }
                    MinifluxDataSource.HttpErrors.INTERNET_CONNECTION -> {
                        selectedListSwipeRefreshLayoutUnread.isRefreshing = false
                        navigation.displayMessage(
                            this@SelectedStatusFragment.requireView(),
                            "No Internet"
                        )
                    }
                    MinifluxDataSource.HttpErrors.AUTHENTICATION -> {
                        navigation.logout()
                    }
                    MinifluxDataSource.HttpErrors.HTTP_ERROR -> {
                        selectedListSwipeRefreshLayoutUnread.isRefreshing = false
                        navigation.displayMessage(
                            this@SelectedStatusFragment.requireView(),
                            "Error"
                        )
                    }
                }
            })
        }
    }

    private fun setUpSwipeToRefreshLayout() {
        selectedListSwipeRefreshLayoutUnread.run {
            setTheme()
            setOnRefreshListener {
                launch(Dispatchers.IO) {
                    viewModel.fetchSelectedEntries()
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        (selectedUnreadRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false

        minifluxPagedListAdapter =
            MinifluxPagedListAdapter(
                MinifluxPagedListAdapter.MiniFluxRecyclerViewMode.Entry,
                this,
                viewModel.entrySelectedTracker
            ).also(
                selectedUnreadRecyclerView::setAdapter
            )

        swipeToDeleteCallback =
            SwipeToDeleteCallback(
                selectedListSwipeRefreshLayoutUnread,
                this,
                minifluxPagedListAdapter,
                if (viewModel.status == "unread") SelectedMode.Unread else SelectedMode.Read
            )

        ItemTouchHelper(
            swipeToDeleteCallback
        ).attachToRecyclerView(selectedUnreadRecyclerView)

        selectedUnreadRecyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    private fun setUpBottomBar() {
        navigation.fab
            .let { fab ->
                fab.setOnClickListener {
                    navigation.navBarFunctionality(false)
                    if (viewModel.fabMode == 0) viewModel.entrySelectedTracker.isSelected.postValue(
                        true
                    )
                    else if (viewModel.fabMode == 1) displayMarkAllDialog(view!!, view as ViewGroup)
                }
            }
    }

    private fun bindUI() = launch {
        viewModel.selectedEntriesStatus.await().let {
            it.removeObservers(this@SelectedStatusFragment)
            it.observe(this@SelectedStatusFragment, Observer { entryList ->
                minifluxPagedListAdapter.submitList(entryList as PagedList<Any>)
                entryList.showNoContent(selectedUnreadNoContentTextView)
                selectedListSwipeRefreshLayoutUnread.isRefreshing = false
            })
        }
        viewModel.selectedEntriesStatusALLID.await().let {
            it.removeObservers(this@SelectedStatusFragment)
            it.observe(this@SelectedStatusFragment, Observer { allID ->
                selectALLID = allID
            })
        }
        selectedUnreadRecyclerView.layoutManager?.scrollToPosition(viewModel.positionRecyclerViewUnread)
    }

    private fun setUpSelectionGestures() {

        viewModel.entrySelectedTracker.selectedSize.removeObservers(this@SelectedStatusFragment)
        viewModel.entrySelectedTracker.selectedSize.observe(this@SelectedStatusFragment, Observer {
            parentFragment?.selection_toolbar?.title = it.toString()
        })

        viewModel.entrySelectedTracker.isSelected.removeObservers(this@SelectedStatusFragment)
        viewModel.entrySelectedTracker.isSelected.observe(
            this@SelectedStatusFragment,
            Observer {
                if (it) {
                    selectedListSwipeRefreshLayoutUnread.isEnabled = false
                    parentFragment?.selectedViewPager?.direction = SwipeDirection.None
                    swipeToDeleteCallback.stopSwipeDir()
                    navigation.hideBottomAppBar()
                    parentFragment?.selectedTabLayout?.visibility = View.GONE
                    parentFragment?.selection_toolbar?.visibility = View.VISIBLE
                } else {
                    selectedListSwipeRefreshLayoutUnread.isEnabled = true
                    parentFragment?.selectedViewPager?.direction =
                        if (viewModel.status == "unread") SwipeDirection.Right else SwipeDirection.Left
                    swipeToDeleteCallback.allowSwipeDir()
                    navigation.showBottomAppBar()
                    parentFragment?.selectedTabLayout?.visibility = View.VISIBLE
                    parentFragment?.selection_toolbar?.visibility = View.GONE
                }
            })

        parentFragment?.selection_toolbar?.let {
            it.setNavigationOnClickListener {
                navigation.navBarFunctionality(false)
                viewModel.entrySelectedTracker.closeList()
                minifluxPagedListAdapter.notifyDataSetChanged()
            }
            it.menu[1].setOnMenuItemClickListener {
                viewModel.updateEntryStatus(
                    viewModel.entrySelectedTracker.entriesSelected
                )
                viewModel.entrySelectedTracker.closeList()
                true
            }

            it.menu[0].setOnMenuItemClickListener {
                viewModel.updateEntryStar(
                    viewModel.entrySelectedTracker.entriesSelected
                )
                viewModel.entrySelectedTracker.closeList()
                minifluxPagedListAdapter.notifyDataSetChanged()
                true
            }

            it.menu[2].setOnMenuItemClickListener {
                if (viewModel.entrySelectedTracker.isAllSelected.value != null && viewModel.entrySelectedTracker.isAllSelected.value!!.not()) {
                    viewModel.entrySelectedTracker.selectALLID(
                        selectALLID
                    )
                } else {
                    viewModel.entrySelectedTracker.clearList()
                }

                minifluxPagedListAdapter.notifyDataSetChanged()
                true
            }
        }
    }

    private fun displayMarkAllDialog(
        view: View,
        parent: ViewGroup
    ) {
        val mBuilder: AlertDialog.Builder = AlertDialog.Builder(view.context)
        val mView: View = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_promt, parent, false)
        mBuilder.setView(mView)
        val dialog: AlertDialog = mBuilder.create()

        mView.headerText.text =
            if (viewModel.status == "unread") getString(R.string.mark_as_read) else getString(R.string.mark_as_unread)
        mView.promptBodyText.text = getString(R.string.body_mark_all, selectALLID.size)

        mView.promtCancel.setOnClickListener {
            dialog.dismiss()
        }

        mView.promptAccept.setOnClickListener {
            dialog.dismiss()
            viewModel.updateEntryStatus(
                selectALLID
            )
        }

        dialog.setOnDismissListener {
            navigation.navBarFunctionality(true)
        }

        dialog.show()
    }
}