package com.constantin.constaflux.ui.activity.host.fragments.selected

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.constantin.constaflux.R
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.data.db.entity.Feed
import com.constantin.constaflux.data.network.MinifluxDataSource
import com.constantin.constaflux.data.network.response.feed.CreateFeedsRequest
import com.constantin.constaflux.data.network.response.feed.UpdateFeedsRequest
import com.constantin.constaflux.internal.*
import com.constantin.constaflux.ui.activity.host.dialog.category.CategoryDialog
import com.constantin.constaflux.ui.activity.host.dialog.feed.FeedDialog
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.adapters.recycler_view.MinifluxPagedListAdapter
import com.constantin.constaflux.ui.base.ScopedFragment
import kotlinx.android.synthetic.main.fragment_selected.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SelectedFragment : ScopedFragment(),
    KodeinAware,
    MinifluxPagedListAdapter.OnRecyclerOnClickListener,
    FeedDialog.FeedInteraction,
    CategoryDialog.CategoryInteraction {

    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()

    private lateinit var viewModel: SelectedViewModel
    private lateinit var minifluxPagedList: MinifluxPagedListAdapter

    private var mLastClickTime = System.currentTimeMillis()
    private var mLastLongClickTime = System.currentTimeMillis()
    private val CLICK_TIME_INTERVAL: Long = 200

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (nextAnim == 0) {
            null
        } else {
            val anim: Animation = AnimationUtils.loadAnimation(activity, nextAnim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    navigation.navBarFunctionality(true)
                }

                override fun onAnimationStart(p0: Animation?) {
                    navigation.navBarFunctionality(false)
                }
            })
            anim
        }
    }

    override fun onRecyclerViewClickListener(position: Int, item: Any) {
        val now = System.currentTimeMillis()
        if (now - mLastClickTime < CLICK_TIME_INTERVAL) {
            return
        }
        mLastClickTime = now

        if (item is Feed)
            navigation.launchSelectedListFragment(
                item.feedId,
                "",
                FragmentContentMode.Feeds
            )
        else if (item is CategoryEntity)
            navigation.launchSelectedListFragment(
                item.categoryId,
                "",
                FragmentContentMode.Category
            )
    }

    override fun onRecyclerViewLongClickListener(position: Int, item: Any) {
        val now = System.currentTimeMillis()
        if (now - mLastLongClickTime < CLICK_TIME_INTERVAL) {
            return
        }
        mLastLongClickTime = now

        if (item is Feed) navigation.launchFeedDialog(childFragmentManager, DialogMode.Update, item)
        else if (item is CategoryEntity) navigation.launchCategoryDialog(
            childFragmentManager,
            DialogMode.Update,
            item
        )
    }

    override fun createFeed(createFeedRequest: CreateFeedsRequest) {
        selectedSwipeRefreshLayout.isRefreshing = true
        viewModel.createFeed(createFeedRequest)
    }

    override fun updateFeed(id: Long, updateFeedsRequest: UpdateFeedsRequest) {
        selectedSwipeRefreshLayout.isRefreshing = true
        viewModel.updateFeed(id, updateFeedsRequest)
    }

    override fun deleteFeed(id: Long) {
        selectedSwipeRefreshLayout.isRefreshing = true
        viewModel.deleteFeed(id)
    }


    override fun createCategory(title: String) {
        selectedSwipeRefreshLayout.isRefreshing = true
        viewModel.createCategory(title)
    }

    override fun updateCategory(id: Long, title: String) {
        selectedSwipeRefreshLayout.isRefreshing = true
        viewModel.updateCategory(id, title)
    }

    override fun deleteCategory(id: Long) {
        selectedSwipeRefreshLayout.isRefreshing = true
        viewModel.deleteCategory(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selected, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = navigation.getViewModel(this) as SelectedViewModel

        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            selectedSwipeRefreshLayout.isRefreshing = true
        }

        navigation.setUpBottomAppBarListFragments()
        setUpSwipeToRefreshLayout()
        setUpRecyclerView()
        bindUI()
        setUpAppBar()
    }

    override fun onResume() {
        super.onResume()
        observeError()
    }

    override fun onStop() {
        super.onStop()
        val layoutManager = selectedRecyclerView.layoutManager
        if (layoutManager != null && layoutManager is GridLayoutManager) {
            viewModel.positionRecyclerView = layoutManager.findFirstVisibleItemPosition()
        }
    }

    private fun setUpSwipeToRefreshLayout() {
        selectedSwipeRefreshLayout.run {
            setTheme()
            isRefreshing = true
            setOnRefreshListener {
                launch(Dispatchers.IO) {
                    viewModel.fetchSelected()
                }
            }
        }
    }

    private fun setUpAppBar() {
        navigation.fab.let {
            it.setOnClickListener {
                if (viewModel.selectedListMode == FragmentContentListMode.Feeds) {
                    navigation.launchFeedDialog(childFragmentManager, DialogMode.Create, null)
                } else if (viewModel.selectedListMode == FragmentContentListMode.Category) {
                    navigation.launchCategoryDialog(childFragmentManager, DialogMode.Create, null)
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        (selectedRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false

        minifluxPagedList =
            MinifluxPagedListAdapter(
                when (viewModel.selectedListMode) {
                    FragmentContentListMode.Feeds -> MinifluxPagedListAdapter.MiniFluxRecyclerViewMode.Feed
                    FragmentContentListMode.Category -> MinifluxPagedListAdapter.MiniFluxRecyclerViewMode.Category
                },
                this
            ).also(
                selectedRecyclerView::setAdapter
            )

        selectedRecyclerView?.layoutManager =
            GridLayoutManager(
                context,
                when {
                    resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT -> 2
                    resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE -> 3
                    else -> 1
                }
                , GridLayoutManager.VERTICAL, false
            )
    }

    private fun bindUI() = launch {
        viewModel.selected.await().let {
            it.removeObservers(this@SelectedFragment)
            it.observe(this@SelectedFragment, Observer { list ->
                minifluxPagedList.submitList(list as PagedList<Any>)
                selectedSwipeRefreshLayout.isRefreshing = false
                list.showNoContent(selected_no_content)
            })

            selectedRecyclerView.layoutManager?.scrollToPosition(viewModel.positionRecyclerView)
        }
    }


    private fun observeError() {
        viewModel.errorLiveData.let {
            it.removeObservers(this@SelectedFragment)
            it.observe(this@SelectedFragment, Observer { error ->
                when (error) {
                    MinifluxDataSource.HttpErrors.SUCCESS -> {
                        selectedSwipeRefreshLayout.isRefreshing = false
                    }
                    MinifluxDataSource.HttpErrors.INTERNET_CONNECTION -> {
                        selectedSwipeRefreshLayout.isRefreshing = false
                        navigation.displayMessage(
                            this@SelectedFragment.requireView(),
                            "No Internet"
                        )
                    }
                    MinifluxDataSource.HttpErrors.AUTHENTICATION -> {
                        navigation.logout()
                    }
                    MinifluxDataSource.HttpErrors.HTTP_ERROR -> {
                        selectedSwipeRefreshLayout.isRefreshing = false
                        navigation.displayMessage(this@SelectedFragment.requireView(), "Error")
                    }
                }
            })
        }
    }
}