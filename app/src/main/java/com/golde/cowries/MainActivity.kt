package com.golde.cowries

import android.graphics.DashPathEffect
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.golde.cowries.UI.ViewModels.ConversionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

class MainActivity : AppCompatActivity() {

    private val cvm by lazy { ViewModelProviders.of(this).get(ConversionViewModel::class.java) }
    private var toCurr = "NGN"
    private var fromCurr = "USD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.landing_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //Listen for new rates
        cvm.listen4Rates()

//        //get historical Rates
//        cvm.historicalRates30()

        //monitor the rate value
        cvm.toRate.observeForever {
            input_to.setText(it.toString())
        }

        //monitor the 'to currency' value
        cvm.to.observeForever {
            input_to.setSuffix(" $it")
            to_btn.text = it.toString()
        }

        //monitor the 'from currency' value
        cvm.from.observeForever {
            input_from.setSuffix(" $it")
            from_btn.text = it.toString()
        }

        cvm.toValue.observeForever {
            input_to.setText(it.toString())
        }



        from_btn.setOnClickListener {
            MaterialDialog(this).title(R.string.select_currency).show {
                listItemsSingleChoice(items = cvm.offerListOfCurrencies()){ _, index, text ->
                    this@MainActivity.fromCurr = cvm.offerListOfCurrencies()[index]
                    updateBus()
                }
            }
        }

        to_btn.setOnClickListener {
            MaterialDialog(this).title(R.string.select_currency).show {
                listItemsSingleChoice(items = cvm.offerListOfCurrencies()){ _, index, text ->
                    this@MainActivity.toCurr = cvm.offerListOfCurrencies()[index]
                    updateBus()
                }
            }
        }

        convert_btn.setOnClickListener {
            cvm.convert(input_from.text.toString().trim().toDouble())
            if(cvm.checkPointsStatus()){
                cvm.initCheckPoints()
                cvm.checkpoints.observeForever {
                    if(it.isNotEmpty()){
                        drawGraph(it)
                    }
                }
            }
        }


        updateBus()
    }

    //trigger Event Bus
    private fun updateBus(){
        val hm : MutableMap<String, String> =  mutableMapOf<String, String>()
        hm["to"] = this@MainActivity.toCurr
        hm["from"] = this@MainActivity.fromCurr
        EventBus.getDefault().post(hm);
        cvm.convert(input_from.text.toString().trim().toDouble())
    }

    //Draw historical rates graph
    private fun drawGraph(cps : Map<String, Double>){
        val entries : ArrayList<Entry>  = arrayListOf<Entry>()
        var x = 4
        cps.iterator().forEach { it->
            entries.add(Entry(x.toFloat(), it.value.toFloat()))
            x -= 1
        }

        Collections.sort(entries, EntryXComparator())
        val set = LineDataSet(entries, "Historical Conversion data")
        set.setDrawFilled(true)
        set.setDrawValues(false)
        set.setDrawCircles(false);
        chart.setTouchEnabled(false)

        val labels = cvm.CheckPointsDates()

        val formatter = object : IAxisValueFormatter {

            // we don't draw numbers, so no decimal digits needed
            val decimalDigits: Int
                get() = 0

            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                return labels[value.toInt()]
            }
        }
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

        chart.setNoDataText("Select currencies and click the convert button to see the graph.")
        chart.setNoDataTextTypeface(Typeface.createFromAsset(this.assets, "monts_medium.ttf"))


        chart.isClickable = false
        chart.axisLeft.spaceBottom = 70F
        chart.axisLeft.spaceTop = 10F
//        chart.axisRight.zeroLineColor = R.color.colorPrimary
//        chart.axisLeft.zeroLineColor = R.color.grey
        chart.axisRight.setDrawTopYLabelEntry(false)

        val drawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient)
        set.fillDrawable = drawable
        chart.xAxis.setDrawGridLines(false)
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        val data = LineData(dataSets)
        chart.data = data
        //chart.set
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
        return super.onOptionsItemSelected(item)
    }


}
