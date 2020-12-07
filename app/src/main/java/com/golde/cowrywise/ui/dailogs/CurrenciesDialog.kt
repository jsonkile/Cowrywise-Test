package com.golde.cowrywise.ui.dailogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.golde.cowrywise.R
import com.golde.cowrywise.models.Currency
import com.golde.cowrywise.ui.adapters.CurrenciesAdapter
import com.golde.cowrywise.ui.viewmodels.BaseViewModel
import kotlinx.android.synthetic.main.currencies_dialog.*
import kotlinx.android.synthetic.main.currencies_dialog.view.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CurrenciesDialog : DialogFragment() {

    private val baseViewModel by sharedViewModel<BaseViewModel>()
    var currencyType = 0 //Is the user selecting for base or target currency, 0 = base; 1 = target


    private lateinit var recyclerView: RecyclerView
    private val currenciesAdapter by lazy { CurrenciesAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
        currencyType = arguments?.getInt("type") ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupObservers()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.currencies_dialog, container, false)
        val toolbar = view.currencies_toolbar
        toolbar.setNavigationOnClickListener { dialog?.dismiss() }
        return view
    }

    //Setup recycle view
    private fun setupRecyclerView() {
        recyclerView = currencies_recycler_view
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView.adapter = currenciesAdapter
        currenciesAdapter.setOnItemClickListener(object : CurrenciesAdapter.ClickListener {
            override fun onClick(view: View, pos: Int, currency: Currency) {
                //Only save the selection if the target and base currencies are not the same
                if (currencyType == 1) {
                    //save target
                    if (baseViewModel.base.value != currency.currency) {
                        baseViewModel.target.postValue(currency.currency)
                        dialog?.dismiss()
                    }
                } else {
                    //save base
                    if (baseViewModel.target.value != currency.currency) {
                        baseViewModel.base.postValue(currency.currency)
                        dialog?.dismiss()
                    }
                }
            }
        })
    }

    //Set observer for the currency list and scroll the recycler view to the previous selected item
    private fun setupObservers() {
        baseViewModel.currencies.observeForever {
            currenciesAdapter.inflate(it)
            baseViewModel.currencies.value?.forEachIndexed { index, currency ->
                if (currencyType == 1) {
                    //If selecting for target currency
                    if (currency.currency == baseViewModel.target.value) {
                        recyclerView.post {
                            recyclerView.layoutManager?.scrollToPosition(index)
                        }
                    }
                } else {
                    //If selecting for base currency
                    if (currency.currency == baseViewModel.base.value) {
                        recyclerView.post {
                            recyclerView.layoutManager?.scrollToPosition(index)
                        }
                    }
                }
            }
        }
    }
}