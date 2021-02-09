package com.constantin.constaflux.ui.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.constantin.constaflux.internal.increaseHitArea

class CustomToolBarButton(context: Context, attrs: AttributeSet) :
    AppCompatImageButton(context, attrs) {
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.increaseHitArea(100F)
    }
}