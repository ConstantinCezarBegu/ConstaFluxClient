package com.constantin.constaflux.ui.adapters.recycler_view

import android.graphics.Typeface
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.constantin.constaflux.R
import com.constantin.constaflux.data.db.entity.CategoryEntity
import com.constantin.constaflux.data.db.entity.Entry
import com.constantin.constaflux.data.db.entity.Feed
import com.constantin.constaflux.internal.inflate
import kotlinx.android.synthetic.main.list_item_category.view.*
import kotlinx.android.synthetic.main.list_item_entry.view.*
import kotlinx.android.synthetic.main.list_item_feed.view.*

class MinifluxPagedListAdapter(
    private val miniFluxRecyclerViewMode: MiniFluxRecyclerViewMode,
    private val onRecyclerOnClickListener: OnRecyclerOnClickListener,
    private val tracker: EntrySelectedTracker? = null
) :
    PagedListAdapter<Any, RecyclerView.ViewHolder>(diffCallback) {

    enum class MiniFluxRecyclerViewMode {
        Entry,
        Feed,
        Category
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return if (oldItem is Entry && newItem is Entry) {
                    oldItem.entryId == newItem.entryId
                } else if (oldItem is Feed && newItem is Feed) {
                    oldItem.feedId == newItem.feedId
                } else if (oldItem is CategoryEntity && newItem is CategoryEntity) {
                    oldItem.categoryId == newItem.categoryId
                } else false
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return if (oldItem is Entry && newItem is Entry) {
                    oldItem.viewEquals(newItem)
                } else if (oldItem is Feed && newItem is Feed) {
                    oldItem.viewEquals(newItem)
                } else if (oldItem is CategoryEntity && newItem is CategoryEntity) {
                    oldItem.viewEquals(newItem)
                } else false
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (miniFluxRecyclerViewMode) {
            MiniFluxRecyclerViewMode.Entry -> R.layout.list_item_entry
            MiniFluxRecyclerViewMode.Feed -> R.layout.list_item_feed
            MiniFluxRecyclerViewMode.Category -> R.layout.list_item_category
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = parent.inflate(viewType)
        return when (miniFluxRecyclerViewMode) {
            MiniFluxRecyclerViewMode.Entry -> EntryViewHolder(view, onRecyclerOnClickListener)
            MiniFluxRecyclerViewMode.Feed -> FeedViewHolder(view, onRecyclerOnClickListener)
            MiniFluxRecyclerViewMode.Category -> CategoryViewHolder(view, onRecyclerOnClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is EntryViewHolder -> holder.bind(item as Entry?)
            is FeedViewHolder -> holder.bind(item as Feed?)
            is CategoryViewHolder -> holder.bind(item as CategoryEntity?)
        }
    }

    fun getEntry(position: Int): Entry? {
        val item = getItem(position)
        return if (item is Entry?) item else null
    }

    inner class EntryViewHolder(
        itemView: View,
        private val onRecyclerOnClickListener: OnRecyclerOnClickListener
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        var item: Entry? = null

        override fun onClick(p0: View?) {
            onRecyclerOnClickListener.onRecyclerViewClickListener(adapterPosition, item!!.entryId)
        }

        override fun onLongClick(p0: View?): Boolean {
            onRecyclerOnClickListener.onRecyclerViewLongClickListener(
                adapterPosition,
                item!!.entryId
            )
            return true
        }

        fun bind(item: Entry?) {
            if (item != null) {
                this.item = item

                itemView.entry_title.text = item.entryTitle
                itemView.entry_feed_title.text = item.feedTitle
                if (item.entryStatus == "unread") itemView.entry_title.setTypeface(
                    null,
                    Typeface.BOLD
                )
                itemView.entry_published_at.text = item.entryPublishedAt.displayTime
                if (item.entryStarred) itemView.entryStar.visibility = View.VISIBLE
                else itemView.entryStar.visibility = View.INVISIBLE
                if (!item.feedIcon.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .asBitmap()
                        .load(Uri.parse("data:${item.feedIcon}"))
                        .into(itemView.entry_icon)
                }

                itemView.isActivated = false
                itemView.image_selected_view.visibility = View.INVISIBLE
                tracker?.let {
                    if (it.isSelected.value!! && it.isEntrySelected(item.entryId)) {
                        itemView.isActivated = true
                        itemView.image_selected_view.visibility = View.VISIBLE
                    }
                }

                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)
            }
        }
    }

    inner class FeedViewHolder(
        itemView: View,
        private val onRecyclerOnClickListener: OnRecyclerOnClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        var item: Feed? = null

        override fun onClick(p0: View?) {
            onRecyclerOnClickListener.onRecyclerViewClickListener(adapterPosition, item!!)
        }

        override fun onLongClick(p0: View?): Boolean {
            onRecyclerOnClickListener.onRecyclerViewLongClickListener(adapterPosition, item!!)
            return true
        }

        fun bind(item: Feed?) {
            this.item = item
            if (item != null) {
                itemView.textViewTitleFeed.text = item.feedTitle
                itemView.textViewCategoryFeed.text = item.categoryTitle
                itemView.textViewLastCheckedFeed.text = item.feedCheckedAt.displayTime

                if (!item.feedIcon.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .asBitmap()
                        .load(Uri.parse("data:${item.feedIcon}"))
                        .into(itemView.imageViewIconFeed)
                }

                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)
            }

        }
    }

    inner class CategoryViewHolder(
        itemView: View,
        private val onRecyclerOnClickListener: OnRecyclerOnClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        var item: CategoryEntity? = null

        override fun onClick(p0: View?) {
            onRecyclerOnClickListener.onRecyclerViewClickListener(adapterPosition, item!!)
        }

        override fun onLongClick(p0: View?): Boolean {
            onRecyclerOnClickListener.onRecyclerViewLongClickListener(adapterPosition, item!!)
            return true
        }

        fun bind(item: CategoryEntity?) {
            this.item = item
            if (item != null) {
                itemView.textViewCategoryTitle.text = item.categoryTitle

                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)
            }
        }
    }

    interface OnRecyclerOnClickListener {
        fun onRecyclerViewClickListener(position: Int, item: Any)
        fun onRecyclerViewLongClickListener(position: Int, item: Any)
    }

}

