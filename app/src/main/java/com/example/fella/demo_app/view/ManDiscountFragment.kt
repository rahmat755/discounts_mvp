package com.example.fella.demo_app.view


import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fella.demo_app.App

import com.example.fella.demo_app.R
import com.example.fella.demo_app.adapters.DiscountAdapter
import com.example.fella.demo_app.model.entities.DiscountItem
import com.example.fella.demo_app.presenter.ManDiscountPresenter
import com.example.fella.demo_app.presenter.MainContract
import com.example.fella.demo_app.utils.InfiniteScrollListener
import com.example.fella.demo_app.utils.inflate
import kotlinx.android.synthetic.main.activity_discounts.*
import kotlinx.android.synthetic.main.fragment_man_discount.*
import android.content.Intent
import android.net.Uri


class ManDiscountFragment : Fragment(), MainContract.View, DiscountAdapter.OnViewSelectedListener {
    override fun onItemClicked(itemURL: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(itemURL)))
    }

    private lateinit var discountAdapter: DiscountAdapter
    private val linearLayout = LinearLayoutManager(context)
    private lateinit var mPresenter: ManDiscountPresenter

    override fun showDiscounts(discounts: ArrayList<DiscountItem>) {
        discountAdapter.addItems(discounts)
    }

    override fun showError(error: Throwable) {
        Snackbar.make(man_discount_recycler_view, "$error", Snackbar.LENGTH_INDEFINITE)
                .setAction("Повторить попытку") { requestData(page = App.page) }.show()
    }

    override fun showProgressBar() {
        activity?.progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgressBar() {
        activity?.progressBar?.visibility = View.GONE
    }

    private fun requestData(page: Int) {
        mPresenter.onLoad(page)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        discountAdapter = DiscountAdapter(this)
        mPresenter = ManDiscountPresenter(this)
        man_discount_recycler_view.apply {
            layoutManager = linearLayout
            adapter = discountAdapter
            addOnScrollListener(InfiniteScrollListener({ requestData(App.page) }, linearLayout))
        }
        requestData(App.page)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_man_discount)
    }

    override fun onResume() {
        super.onResume()
        if (App.state != null) {
            linearLayout.onRestoreInstanceState(App.state)
            discountAdapter.removeAllItems()
            discountAdapter.addItems(App.discounts_man)
        }
    }

    override fun onPause() {
        super.onPause()
        App.state = linearLayout.onSaveInstanceState()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDestroy()
    }
}