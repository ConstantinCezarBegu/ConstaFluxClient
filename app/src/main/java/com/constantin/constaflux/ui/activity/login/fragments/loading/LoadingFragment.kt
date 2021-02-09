package com.constantin.constaflux.ui.activity.login.fragments.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.constantin.constaflux.R

import com.constantin.constaflux.internal.observeCount
import com.constantin.constaflux.ui.activity.login.navigation.LoginViewModelNavigationProvider
import com.constantin.constaflux.ui.base.ScopedFragment
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class LoadingFragment : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: LoginViewModelNavigationProvider by instance()
    private lateinit var viewModel: LoadingViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = navigation.getViewModel(this) as LoadingViewModel
        viewModel.fetchData()
        observingFetch()
    }

    private fun observingFetch() = launch {
        viewModel.me.await().observeCount(this@LoadingFragment, Observer {
            viewModel.endFirstLaunch()
            navigation.launchHostActivity()
        }, 1)
    }
}
