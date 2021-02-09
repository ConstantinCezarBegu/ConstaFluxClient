package com.constantin.constaflux.ui.activity.login.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.constantin.constaflux.R
import com.constantin.constaflux.internal.Errors
import com.constantin.constaflux.ui.activity.login.navigation.LoginViewModelNavigationProvider
import kotlinx.android.synthetic.main.fragment_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class LoginFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: LoginViewModelNavigationProvider by instance()
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = navigation.getViewModel(this) as LoginViewModel
        loadPersistedViews()
        loadingErrorFeedback()
        loginButton.setOnClickListener { connectToMiniflux() }
    }

    override fun onPause() {
        super.onPause()
        savePersistedViews()
    }

    private fun loadPersistedViews() {
        urlEditText.setText(viewModel.url)
        usernameEditText.setText(viewModel.user)
        passwordEditText.setText(viewModel.pass)
    }

    private fun savePersistedViews() {
        viewModel.url = urlEditText.text.toString()
        viewModel.user = usernameEditText.text.toString()
        viewModel.pass = passwordEditText.text.toString()
    }

    private fun loadingErrorFeedback() {
        viewModel.error.observe(this, Observer {
            when (it) {
                Errors.NO_CONNECTIVITY -> {
                    showConnectivityError(true)
                    loadingLogin(false)
                }
                Errors.URL -> {
                    invalidURL()
                    loadingLogin(false)
                }
                Errors.CREDENTIALS -> {
                    invalidCredential()
                    loadingLogin(false)
                }
                Errors.SUCCESS -> {
                    navigation.launchLoadingFragment()
                }
                else -> {
                }
            }
        })
    }

    private fun connectToMiniflux() {
        missingInputDisplayError()
        if (!urlEditText.text.isNullOrBlank() &&
            !usernameEditText.text.isNullOrBlank() &&
            !passwordEditText.text.isNullOrBlank()
        ) {
            showConnectivityError(false)
            loadingLogin(true)
            viewModel.connectUser(
                urlEditText.text.toString(),
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }

    private fun missingInputDisplayError() {
        if (urlEditText.text.isNullOrBlank()) {
            urlTextInput.error = "Provide the entryUrl of the miniflux server"
            urlTextInput.isErrorEnabled = true
        } else {
            urlTextInput.isErrorEnabled = false
        }

        if (usernameEditText.text.isNullOrBlank()) {
            usernameTextInput.error = "Provide the username for the account"
            usernameTextInput.isErrorEnabled = true
        } else {
            usernameTextInput.isErrorEnabled = false
        }

        if (passwordEditText.text.isNullOrBlank()) {
            paswordTextInput.error = "Provide the password for the account"
            paswordTextInput.isErrorEnabled = true
        } else {
            paswordTextInput.isErrorEnabled = false
        }
    }

    private fun invalidURL() {
        urlTextInput.error = "Invalid URL"
        urlTextInput.isErrorEnabled = true
    }

    private fun invalidCredential() {
        usernameTextInput.error = "Invalid username"
        usernameTextInput.isErrorEnabled = true
        paswordTextInput.error = "Invalid password"
        paswordTextInput.isErrorEnabled = true
    }

    private fun loadingLogin(loading: Boolean) {
        if (loading) {
            progressBar_loading_login.visibility = View.VISIBLE
            loginButton.visibility = View.INVISIBLE
        } else {
            progressBar_loading_login.visibility = View.INVISIBLE
            loginButton.visibility = View.VISIBLE
        }
    }

    private fun showConnectivityError(show: Boolean) {
        if (show) {
            connectivityErrorWorning.visibility = View.VISIBLE
        } else {
            connectivityErrorWorning.visibility = View.GONE
        }
    }

}
