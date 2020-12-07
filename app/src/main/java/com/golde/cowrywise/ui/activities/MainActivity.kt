package com.golde.cowrywise.ui.activities

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.golde.cowrywise.R
import com.golde.cowrywise.helpers.CURRENCIES_DIALOG_TAG
import com.golde.cowrywise.ui.dailogs.CurrenciesDialog
import com.golde.cowrywise.ui.viewmodels.BaseViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val baseViewModel: BaseViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.landing_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupUI()
        setupObservers()

        downloadRatesAndSymbols()
    }

    private fun downloadRatesAndSymbols() {
        baseViewModel.getLatestRates()
        baseViewModel.getSymbols()
    }

    //Observe all livedata objects
    private fun setupObservers() {
        baseViewModel.rates.observeForever {
            //If the DB is empty then download the conversion rates
            if (it.isEmpty()) {
                downloadRatesAndSymbols()
                Snackbar.make(
                    findViewById(R.id.container),
                    "Downloading...",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                /**
                 * The DB is not empty, initialize the conversion form with USD EUR and set the base value to $1
                 */
                baseViewModel.convert(1F)
            }
        }

        baseViewModel.base.observeForever {
            chart?.clear()
            base_btn.text = it
            base_currency_label.text = it
        }

        baseViewModel.target.observeForever {
            chart?.clear()
            target_btn.text = it
            target_currency_label.text = it
        }

        baseViewModel.baseAmount.observeForever {
            input_base.setText(it.toString())
        }

        baseViewModel.targetAmount.observeForever {
            input_target.setText(it.toString())
        }

        //When the user requests to see chart. If the request was successful, the historic rates value is updated and the chart is drawn
        baseViewModel.historicRates.observeForever {
            progress_indicator?.visibility = View.GONE
            if (baseViewModel.historicRates.value != null) drawGraph() else {
                chart?.clear()
            }
        }

        //Show error message when there is one
        baseViewModel.errorMessage.observeForever {
            if (!it.isNullOrBlank()) Snackbar.make(
                findViewById(R.id.container),
                it,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun setupUI() {
        //Chart
        chart.setNoDataText("Tap the buttons ↑ to load the chart")
        chart.setNoDataTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        chart.setNoDataTextTypeface(Typeface.createFromAsset(this.assets, "monts_medium.ttf"))

        //base edit text
        input_base?.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus) input_base?.setSelection(input_base?.text.toString().length)
        }

        //Change base currency button
        base_btn?.setOnClickListener {
            showCurrenciesDialog(0)
        }

        //Change target currency button
        target_btn?.setOnClickListener {
            showCurrenciesDialog(1)
        }

        //Convert button
        convert_btn?.setOnClickListener {
            baseViewModel.convert(input_base.text.toString().toFloat())
        }

        history_thirty_btn?.setOnClickListener {
            progress_indicator?.visibility = View.VISIBLE
            chart?.clear()
            baseViewModel.getHistoricalRatesThirty()
        }

        history_ninety_btn?.setOnClickListener {
            progress_indicator?.visibility = View.VISIBLE
            chart?.clear()
            baseViewModel.getHistoricalRatesNinety()
        }
    }

    //Open dialog for selecting currency. 'type' specifies of base or target
    private fun showCurrenciesDialog(type: Int) {
        val ft = supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putInt("type", type)
        val dialog = CurrenciesDialog()
        dialog.arguments = bundle
        ft.let { dialog.show(it, CURRENCIES_DIALOG_TAG) }
    }

    //Draw historical rates graph
    private fun drawGraph() {
        val entries: ArrayList<Entry> = arrayListOf()
        var x = 9
        baseViewModel.historicRates.value?.reversed()?.iterator()?.forEach { it ->
            entries.add(Entry(x.toFloat(), it.second))
            x -= 1
        }

        Collections.sort(entries, EntryXComparator())
        val set = LineDataSet(entries, "Historical Conversion data")
        val drawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient)
        set.fillDrawable = drawable
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawFilled(true)
        set.setDrawValues(false)
        set.setDrawCircles(false);
        chart.setTouchEnabled(false)

        val labels = mutableListOf<String>()
        baseViewModel.historicRates.value?.reversed()?.forEach {
            labels.add(it.first)
        }

        Log.d("MAZE RUNNER", baseViewModel.historicRates.value?.size.toString())

        val formatter = IAxisValueFormatter { value, axis -> labels[value.toInt()] }
        chart.xAxis.granularity = 1f
        chart.xAxis.valueFormatter = formatter

        chart.description.text = ""
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.axisLineColor = ContextCompat.getColor(this, R.color.lblue)
        chart.xAxis.textColor = ContextCompat.getColor(this, R.color.lblue)
        chart.xAxis.textSize = 10f
        chart.xAxis.axisLineWidth = 1f
        chart.xAxis.isEnabled = true
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.axisLeft.isEnabled = false


        chart.isClickable = false
        chart.axisLeft.spaceBottom = 70F
        chart.axisLeft.spaceTop = 25F
        chart.axisRight.setDrawTopYLabelEntry(false)

        chart.xAxis.setDrawGridLines(false)
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set)
        val data = LineData(dataSets)
        chart.data = data
        chart.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_signup) {
            return true
        }

        if (item.itemId == R.id.action_api) {
            MaterialDialog(this).show {
                input(prefill = baseViewModel.getAPIKeyFromSharedPref() ?: "") { _, text ->
                    baseViewModel.saveAPIKeyToSharedPreference(text.toString())
                }
                positiveButton(R.string.submit)
                title(text = "Update API Key")

            }
            return true
        }

        if (item.itemId == R.id.action_reload) {
            downloadRatesAndSymbols()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}
