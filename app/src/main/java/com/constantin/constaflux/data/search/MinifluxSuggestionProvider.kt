package com.constantin.constaflux.data.search

import android.content.SearchRecentSuggestionsProvider

class MinifluxSuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.example.MinifluxSuggestionProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES or DATABASE_MODE_2LINES
    }
}