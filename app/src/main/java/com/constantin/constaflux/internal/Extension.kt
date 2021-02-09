package com.constantin.constaflux.internal

import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.constantin.constaflux.R
import com.constantin.constaflux.data.db.entity.*
import com.constantin.constaflux.data.network.response.category.CategoriesResponse
import com.constantin.constaflux.data.network.response.entry.EntriesResponse


fun List<Entry>.toEntryID(): List<Long> {
    return this.map {
        it.entryId
    }
}

fun List<Entry>.entryExtractId(): List<Long> {
    return this.map {
        it.entryId
    }
}

fun List<EntryEntity>.entryEntityExtractId(): List<Long> {
    return map {
        it.entryId
    }
}

fun List<FeedEntity>.feedEntityExtractId(): List<Long> {
    return this.map {
        it.feedId
    }
}

fun List<CategoryEntity>.categoryEntityExtractId(): List<Long> {
    return this.map {
        it.categoryId
    }
}

fun EntriesResponse.toEntryTableId(): List<EntryIdTableEntity> {
    return this.entryEntities.map {
        EntryIdTableEntity(it.entryId, this.category)
    }
}

fun Array<CategoryEntity>.categoryExtractTitle(): Array<String> {
    return this.map {
        it.categoryTitle
    }.toTypedArray()
}

fun List<Any>.showNoContent(textView: TextView) {
    if (this.isEmpty()) {
        textView.visibility = View.VISIBLE
    } else {
        textView.visibility = View.INVISIBLE
    }
}

fun List<CategoriesResponse>.toCategoryEntity(): List<CategoryEntity> {
    return this.map {
        CategoryEntity(it.id, it.title)
    }
}

// Does not get triggered the first time
fun <T> LiveData<T>.observeChange(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<T>
) {
    var ignoredCount = 0
    observe(lifecycleOwner, Observer<T> { t ->
        if (ignoredCount++ > 0) {
            observer.onChanged(t)
        }
    })
}

// Gets triggered after a period of time and then get's removed
fun <T> LiveData<T>.observeCount(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<T>,
    activationCount: Int
) {
    var ignoredCount = 0
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            if (ignoredCount++ == activationCount) {
                observer.onChanged(t)
                removeObserver(this)
            }
        }
    })
}

fun ViewGroup.inflate(
    @LayoutRes layoutId: Int,
    inflater: LayoutInflater = LayoutInflater.from(context),
    attachToRoot: Boolean = false
): View {
    return inflater.inflate(layoutId, this, attachToRoot)
}

fun enableDisableSwipeRefresh(
    enable: Boolean,
    selectedListSwipeRefreshLayout: SwipeRefreshLayout
) {
    selectedListSwipeRefreshLayout.isEnabled = enable
}


fun SwipeRefreshLayout.setTheme() {
    this.setProgressBackgroundColorSchemeColor(
        resources.getColor(
            R.color.materialDarkSurface,
            null
        )
    )
    this.setColorSchemeColors(resources.getColor(R.color.minifluxColor, null))
}


fun View.increaseHitArea(dp: Float) {

    val increasedArea = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        Resources.getSystem().displayMetrics
    ).toInt()
    val parent = parent as View

    parent.post {
        val rect = Rect()
        this.getHitRect(rect)
        rect.top -= increasedArea
        rect.left -= increasedArea
        rect.bottom += increasedArea
        rect.right += increasedArea
        parent.touchDelegate = TouchDelegate(rect, this)
    }
}