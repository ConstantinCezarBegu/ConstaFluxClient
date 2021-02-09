package com.constantin.constaflux.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.constantin.constaflux.ui.activity.host.fragments.selected_list.SelectedStatusFragment


class ViewPagerAdapter(
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val childFragmentName = arrayOf("Unread", "Read")
    private val childFragment: Array<Fragment> =
        arrayOf(
            SelectedStatusFragment.newInstance("unread"),
            SelectedStatusFragment.newInstance("read")
        )

    override fun getItem(position: Int): Fragment {
        return childFragment[position]
    }

    override fun getCount(): Int {
        return childFragment.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val title = childFragmentName[position]
        return title.subSequence(title.lastIndexOf(".") + 1, title.length)
    }
}