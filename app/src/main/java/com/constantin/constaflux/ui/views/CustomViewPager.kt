package com.constantin.constaflux.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.constantin.constaflux.internal.SwipeDirection


class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var initialXValue: Float = 0.toFloat()
    var direction: SwipeDirection? = null

    init {
        this.direction = SwipeDirection.All
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.isSwipeAllowed(event)) {
            super.onTouchEvent(event)
        } else false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.isSwipeAllowed(event)) {
            super.onInterceptTouchEvent(event)
        } else false

    }

    private fun isSwipeAllowed(event: MotionEvent): Boolean {
        if (this.direction == SwipeDirection.All) return true

        if (direction == SwipeDirection.None)
        //disable any swipe
            return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            initialXValue = event.x
            return true
        }

        if (event.action == MotionEvent.ACTION_MOVE) {
            try {
                val diffX = event.x - initialXValue
                if (diffX > 0 && direction == SwipeDirection.Right) {
                    // swipe from left to right detected
                    return false
                } else if (diffX < 0 && direction == SwipeDirection.Left) {
                    // swipe from right to left detected
                    return false
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

        }

        return true
    }
}