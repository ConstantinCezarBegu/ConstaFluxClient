package com.constantin.constaflux.ui.activity.host.dialog.search

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.constantin.constaflux.R
import com.constantin.constaflux.data.network.MinifluxDataSource
import com.constantin.constaflux.internal.setTheme
import com.constantin.constaflux.internal.showNoContent
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.adapters.recycler_view.MinifluxPagedListAdapter
import com.constantin.constaflux.ui.base.ScopedDialog
import kotlinx.android.synthetic.main.dialog_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SearchDialog :
    ScopedDialog(), KodeinAware, MinifluxPagedListAdapter.OnRecyclerOnClickListener {
    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()
    private lateinit var viewModel: SearchViewModel

    private lateinit var minifluxPagedListAdapter: MinifluxPagedListAdapter

    private var mLastClickTime = System.currentTimeMillis()
    private var mLastLongClickTime = System.currentTimeMillis()
    private val CLICK_TIME_INTERVAL: Long = 200

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchDialog()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        navigation.navBarFunctionality(true)
    }

    override fun onRecyclerViewClickListener(position: Int, item: Any) {
        val now = System.currentTimeMillis()
        if (now - mLastClickTime < CLICK_TIME_INTERVAL) {
            return
        }
        mLastClickTime = now

        if (item is Long) navigation.launchDisplayEntryFragment(item)
        this.dismiss()
    }

    override fun onRecyclerViewLongClickListener(position: Int, item: Any) {
        val now = System.currentTimeMillis()
        if (now - mLastLongClickTime < CLICK_TIME_INTERVAL) {
            return
        }
        mLastLongClickTime = now

        if (item is Long) navigation.launchDisplayEntryFragment(item)
        this.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = navigation.getViewModel(this) as SearchViewModel

        if (savedInstanceState == null) {
            searchSwipeRefreshLayout?.isRefreshing = true
        }

        viewModel.searchLiveData.observe(this@SearchDialog, Observer {
            toolbar_search.title = it
        })

        toolbar_search.setNavigationOnClickListener {
            dismiss()
        }

        setUpRecyclerView()
        bindUI()
    }

    override fun onResume() {
        super.onResume()
        setUpSwipeToRefreshLayout()
        observeError()
    }

    override fun onStop() {
        super.onStop()
        val layoutManager = searchRecyclerView.layoutManager
        if (layoutManager != null && layoutManager is LinearLayoutManager) {
            viewModel.positionRecyclerViewSearch = layoutManager.findFirstVisibleItemPosition()
        }
    }

    private fun setUpSwipeToRefreshLayout() {
        searchSwipeRefreshLayout.run {
            setTheme()
            setOnRefreshListener {
                launch(Dispatchers.IO) {
                    viewModel.fetchSearchedEntries()
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        (searchRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false

        minifluxPagedListAdapter =
            MinifluxPagedListAdapter(
                MinifluxPagedListAdapter.MiniFluxRecyclerViewMode.Entry,
                this
            ).also(
                searchRecyclerView::setAdapter
            )

        searchRecyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    private fun bindUI() = launch {
        viewModel.searchedEntries.await().let {
            it.removeObservers(this@SearchDialog)
            it.observe(this@SearchDialog, Observer { listEntry ->
                minifluxPagedListAdapter.submitList(listEntry as PagedList<Any>)
                searchSwipeRefreshLayout?.isRefreshing = false
                listEntry.showNoContent(noContentSearch)
            })
        }


        searchRecyclerView.layoutManager?.scrollToPosition(viewModel.positionRecyclerViewSearch)
    }

    private fun observeError() {
        viewModel.errorLiveData.let {
            it.removeObservers(this@SearchDialog)
            it.observe(this@SearchDialog, Observer { error ->
                when (error) {
                    MinifluxDataSource.HttpErrors.SUCCESS -> {
                        searchSwipeRefreshLayout.isRefreshing = false
                    }
                    MinifluxDataSource.HttpErrors.INTERNET_CONNECTION -> {
                        searchSwipeRefreshLayout.isRefreshing = false
                        navigation.displayMessage(
                            this@SearchDialog.requireView(),
                            "No Internet. Displaying previous search...",
                            false
                        )
                    }
                    MinifluxDataSource.HttpErrors.AUTHENTICATION -> {
                        navigation.logout()
                    }
                    MinifluxDataSource.HttpErrors.HTTP_ERROR -> {
                        searchSwipeRefreshLayout.isRefreshing = false
                        navigation.displayMessage(this@SearchDialog.requireView(), "Error", false)
                    }
                }
            })
        }
    }
}