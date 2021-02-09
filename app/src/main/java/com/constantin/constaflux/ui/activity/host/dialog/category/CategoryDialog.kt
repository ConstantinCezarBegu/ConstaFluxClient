package com.constantin.constaflux.ui.activity.host.dialog.category

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.constantin.constaflux.R
import com.constantin.constaflux.internal.DialogMode
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import kotlinx.android.synthetic.main.dialog_category.*
import kotlinx.android.synthetic.main.dialog_promt.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class CategoryDialog : DialogFragment(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()
    private lateinit var viewModel: CategoryDialogViewModel

    companion object {
        @JvmStatic
        fun newInstance() =
            CategoryDialog()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        navigation.navBarFunctionality(true)
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
        return inflater.inflate(R.layout.dialog_category, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = navigation.getViewModel(this) as CategoryDialogViewModel

        setUpBasedOnMode()
        restorePersistence()
    }

    override fun onPause() {
        super.onPause()
        savePersistedViews()
    }

    private fun setUpBasedOnMode() {

        if (viewModel.mode == DialogMode.Create) {
            category_toolbar.menu[0].setOnMenuItemClickListener {
                val text = categoryTitleEditText.text.toString()
                if (text.isNotBlank()) {
                    (parentFragment as CategoryInteraction).createCategory(text)
                    dismiss()
                }
                true
            }
        } else if (viewModel.mode == DialogMode.Update) {
            category_toolbar.title = getString(R.string.update_category)
            deleteCategoryButton.run {
                visibility = View.VISIBLE
                setOnClickListener {
                    deleteCategoryButton.isEnabled = false
                    viewModel.dialogState = true
                }
            }
            categoryTitleEditText.append(viewModel.item!!.categoryTitle)
            category_toolbar.menu[0].setOnMenuItemClickListener {
                val text = categoryTitleEditText.text.toString()
                if (text.isNotBlank()) {
                    (parentFragment as CategoryInteraction).updateCategory(
                        viewModel.item!!.categoryId,
                        text
                    )
                    dismiss()
                }
                true
            }
        }
        category_toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }


    private fun savePersistedViews() {
        viewModel.categoryName = categoryTitleEditText.text.toString()
    }

    private fun restorePersistence() {
        categoryTitleEditText.setText(viewModel.categoryName)
        viewModel.dialogStateLiveDate.observe(this@CategoryDialog, Observer {
            if (it) showDialogDeleteCategory(view!!, view!! as ViewGroup)
        })
    }

    private fun showDialogDeleteCategory(
        view: View,
        parent: ViewGroup
    ) {
        val mBuilder: AlertDialog.Builder = AlertDialog.Builder(view.context)
        val mView: View = LayoutInflater.from(view.context)
            .inflate(R.layout.dialog_promt, parent, false)
        mBuilder.setView(mView)
        val dialog: AlertDialog = mBuilder.create()

        mView.promtCancel.setOnClickListener {
            dialog.dismiss()
        }

        mView.promptAccept.setOnClickListener {
            (parentFragment as CategoryInteraction).deleteCategory(viewModel.item!!.categoryId)
            dialog.dismiss()
            this@CategoryDialog.dismiss()
        }

        dialog.setOnDismissListener {
            viewModel.dialogState = false
            deleteCategoryButton.isEnabled = true
        }

        dialog.show()
    }

    interface CategoryInteraction {
        fun createCategory(title: String)
        fun updateCategory(id: Long, title: String)
        fun deleteCategory(id: Long)
    }
}