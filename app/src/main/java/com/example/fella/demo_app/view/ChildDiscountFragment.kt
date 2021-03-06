package com.example.fella.demo_app.view


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fella.demo_app.App

import com.example.fella.demo_app.R
import com.example.fella.demo_app.adapters.DiscountAdapter
import com.example.fella.demo_app.adapters.DiscountDelegateAdapter
import com.example.fella.demo_app.utils.AdapterConstants
import com.example.fella.demo_app.utils.EqualSpacingItemDecoration
import com.example.fella.demo_app.utils.InfiniteScrollListener
import com.example.fella.demo_app.utils.inflate
import com.example.fella.demo_app.viewmodel.DiscountViewModel
import com.example.fella.demo_app.viewmodel.DiscountViewModelFactory
import com.example.fella.demo_app.viewmodel.ToolBarViewModel
import com.example.fella.demo_app.viewmodel.ToolbarViewModelFactory
import kotlinx.android.synthetic.main.fragment_child_discount.*
import javax.inject.Inject


class ChildDiscountFragment : Fragment(), DiscountDelegateAdapter.OnViewSelectedListener {
    @Inject
    lateinit var discountViewModelFactory: DiscountViewModelFactory
    @Inject
    lateinit var toolbarViewModelFactory: ToolbarViewModelFactory
    private lateinit var discountAdapter: DiscountAdapter
    private val gridLayout = GridLayoutManager(context, 2)
    lateinit var model: DiscountViewModel
    lateinit var toolbarModel: ToolBarViewModel
    private var isHot: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.discountsComponent.injectChild(this)
    }

    override fun onItemClicked(itemURL: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(itemURL)))
    }

    private fun showError(error: String) {
        Snackbar.make(child_discount_recycler_view, error, Snackbar.LENGTH_INDEFINITE)
                .setAction("Повторить попытку") { requestData(isHot) }.show()
    }

    private fun requestData(isHot: Boolean) {
        if (isHot)
            model.getChildHotDiscounts()
        else
            model.getChildDiscounts()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = ViewModelProviders.of(this, discountViewModelFactory).get(DiscountViewModel::class.java)
        toolbarModel = ViewModelProviders.of(this, toolbarViewModelFactory).get(ToolBarViewModel::class.java)
        discountAdapter = DiscountAdapter(this)
        val itemDecoration = EqualSpacingItemDecoration(16, 2)
        gridLayout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (discountAdapter.getItemViewType(position)) {
                    AdapterConstants.DISCOUNTS -> 1
                    AdapterConstants.LOADING -> 2
                    else -> -1
                }
            }
        }
        child_discount_recycler_view.apply {
            layoutManager = gridLayout
            adapter = discountAdapter
            addOnScrollListener(InfiniteScrollListener({ requestData(isHot) }, gridLayout))
            addItemDecoration(itemDecoration)
        }
        model.getChildRecyclerViewState().observe(this, Observer { state ->
            gridLayout.onRestoreInstanceState(state)
        })
        model.getChildDiscounts().observe(this, Observer { item ->
            discountAdapter.removeAllItems()
            discountAdapter.addItems(item!!)
        })
        model.showChildError.observe(this, Observer {
            it?.getContentIfNotHandled()?.let {
                showError(it)
            }
        })
        toolbarModel.getToolbarState().observe(this, Observer {
            model.pageWoman.value = 1
            isHot=it!!
            model.childDiscount.value = arrayListOf()
            child_discount_recycler_view.clearOnScrollListeners()
            child_discount_recycler_view.addOnScrollListener(
                    InfiniteScrollListener({ requestData(it) }, gridLayout)
            )
            requestData(it)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_child_discount)
    }

    override fun onResume() {
        super.onResume()
        gridLayout.onRestoreInstanceState(model.childRecyclerViewState?.value)

    }

    override fun onStop() {
        super.onStop()
        model.childRecyclerViewState!!.value = gridLayout.onSaveInstanceState()
    }
}
