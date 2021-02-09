package com.constantin.constaflux.ui.activity.host.fragments.selected_list

import android.animation.LayoutTransition
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import com.constantin.constaflux.R
import com.constantin.constaflux.ui.activity.host.navigation.HostViewModelNavigationProvider
import com.constantin.constaflux.ui.adapters.ViewPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_selected_list.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class SelectedListFragment :
    Fragment(), KodeinAware {
    override val kodein by closestKodein()
    private val navigation: HostViewModelNavigationProvider by instance()

    private var first = true
    private var fabAnimation = false

    fun getFabAnimation(): Boolean {
        return if (fabAnimation) {
            true
        } else {
            fabAnimation = true
            false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selected_list, container, false)
    }


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (nextAnim == 0) {
            null
        } else {
            val anim: Animation = AnimationUtils.loadAnimation(activity, nextAnim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    navigation.navBarFunctionality(true)
                }

                override fun onAnimationStart(p0: Animation?) {
                    navigation.navBarFunctionality(false)
                }
            })
            anim
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        first = true
        fabAnimation = false

        navigation.setUpBottomAppBarListFragments()
        selectedViewPager.run {
            adapter =
                ViewPagerAdapter(
                    childFragmentManager
                )
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    when (state) {
                        ViewPager.SCROLL_STATE_DRAGGING -> // When dragging starts
                            navigation.navBarFunctionality(false)
                        ViewPager.SCROLL_STATE_IDLE -> // on resume of page get's called
                            navigation.navBarFunctionality(isEnabled = true)
                        ViewPager.SCROLL_STATE_SETTLING -> // page is selected
                            navigation.navBarFunctionality(isEnabled = true, enableFab = false)
                    }
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    if (first && position == 0 && positionOffset == 0f && positionOffsetPixels == 0) {
                        onPageSelected(0)
                        first = false
                    }
                }

                override fun onPageSelected(position: Int) {
                    selection_toolbar.menu[1].icon = ContextCompat.getDrawable(
                        context,
                        if (position == 0) R.drawable.ic_mark_as_read else R.drawable.ic_mark_as_unread
                    )
                    navigation.fab
                        .let { fab ->
                            if (position == 0) {
                                if (getFabAnimation()) {
                                    fab.setImageResource(R.drawable.mark_as_unread_to_mark_as_read)
                                    animateVector(fab)
                                } else {
                                    fab.setImageResource(R.drawable.ic_mark_as_read)
                                }
                            } else if (position == 1) {
                                if (getFabAnimation()) {
                                    fab.setImageResource(R.drawable.mark_as_read_to_mark_as_unread)
                                    animateVector(fab)
                                } else {
                                    fab.setImageResource(R.drawable.ic_mark_as_unread)
                                }
                            }

                        }
                }
            })
        }

        selectedTabLayout.setupWithViewPager(selectedViewPager)

        top_bar.layoutTransition.addTransitionListener(object :
            LayoutTransition.TransitionListener {
            override fun startTransition(
                p0: LayoutTransition?,
                p1: ViewGroup?,
                p2: View?,
                p3: Int
            ) {

            }

            override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {
                navigation.navBarFunctionality(true)
            }

        })
    }

    fun animateVector(fab: FloatingActionButton) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M) {
            (fab.drawable as AnimatedVectorDrawableCompat).start()
        } else {
            (fab.drawable as AnimatedVectorDrawable).start()
        }

    }


}
