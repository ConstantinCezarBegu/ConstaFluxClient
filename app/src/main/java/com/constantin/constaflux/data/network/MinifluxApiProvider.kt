package com.constantin.constaflux.data.network

import com.constantin.constaflux.data.encrypt.UserEncrypt

class MinifluxApiProvider(
    private val userEncrypt: UserEncrypt
) {
    private var minifluxApiService: MinifluxApiService

    init {
        this.minifluxApiService = MinifluxApiService(userEncrypt)
    }

    fun refresh() {
        this.minifluxApiService = MinifluxApiService(userEncrypt)
    }

    fun getApi(): MinifluxApiService {
        return minifluxApiService
    }
}