package com.expedia.bookings.launch.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import org.joda.time.LocalDate
import java.util.ArrayList

class PriceGraphView(context: Context, val attrs: AttributeSet) : FrameLayout(context, attrs) {

//    private val priceGraph: LineChart by bindView(R.id.price_graph)
    private val priceGraphContainer: LinearLayout by bindView(R.id.price_graph_container)

//    private val dateFormatter = SimpleDateFormat("dd/MM", resources.configuration.locale)
    private var startDate = LocalDate()

    private val lineColors = ColorTemplate.MATERIAL_COLORS

    init {
        View.inflate(context, R.layout.price_graph_view, this)
//        val priceGraph = LineChart(context, attrs)
//        addView(priceGraph)

//        setUpGraph(priceGraph)
    }

    fun setData(data: LinkedHashMap<String, ArrayList<Float>>, startDate: LocalDate) {
        priceGraphContainer.removeAllViews()
        if (data.count() <= 0) {
            return
        }
        this.startDate = startDate

//        var maxDay = 0


        var dataCount = 0
        for ((name, priceData) in data) {
            var dataSets = ArrayList<ILineDataSet>()

            var prices = ArrayList<Entry>()
            var x = 0.toFloat()

            for (price in priceData) {
                prices.add(Entry(x, price))
                x++
            }

            val lineDataSet = LineDataSet(prices, name)
            lineDataSet.valueTextSize = 11F
            lineDataSet.setValueFormatter( { value, _, _, _ ->
                "$" + Math.round(value)
            })

            val color = lineColors[dataCount % lineColors.size]
            lineDataSet.setColor(color)
            lineDataSet.setCircleColor(color)
            dataSets.add(lineDataSet)

            val data = LineData(dataSets)
            dataCount++

            val priceGraph = LineChart(context, attrs)
            priceGraphContainer.addView(priceGraph)

            val params = priceGraph.layoutParams as MarginLayoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.graph_height)
            val margin = resources.getDimensionPixelSize(R.dimen.graph_margin)
            params.setMargins(margin, 0, margin, margin)
            priceGraph.layoutParams = params

            setUpGraph(priceGraph, priceData[0])

            priceGraph.data = data
            priceGraph.invalidate()
        }
    }

    private fun setUpGraph(graph: LineChart, priceOnFirstDay: Float) {
//        graph.setOnChartValueSelectedListener(this)

        graph.setDrawGridBackground(false)
//        Drawable background = getDrawable(R.drawable.new_background);
//        graph.setBackground(background);
        graph.description.isEnabled = false
        graph.setDrawBorders(false)

        graph.axisLeft.isEnabled = true
        graph.axisRight.isEnabled = false

        graph.axisLeft.setDrawAxisLine(true)
        graph.axisLeft.setDrawGridLines(false)
        // formatRate(BigDecimal amount, String currencyCode, int flags) in case want to format, should use flag no decimal, and take param currencyCode

        graph.xAxis.setDrawAxisLine(true)
        graph.xAxis.setDrawGridLines(false)
        graph.xAxis.setTextSize(7f)
        graph.xAxis.position = XAxis.XAxisPosition.BOTTOM

        graph.xAxis.setValueFormatter( { value, _ ->
            val date = startDate.plusDays(value.toInt())
//            dateFormatter.format(date)
            date.toString("MM/dd")
        })

        graph.axisLeft.setValueFormatter( { value, _ ->
            "$" + value
        })


        // enable touch gestures
//        graph.setTouchEnabled(true)

        // enable scaling and dragging
        graph.isDragEnabled = false
//        graph.setScaleEnabled(true)
        graph.isScaleYEnabled = false
        graph.isScaleXEnabled = false

        val leftAxis = graph.getAxisLeft()
        leftAxis.setDrawLabels(false)
        var ll = LimitLine(priceOnFirstDay, "")
        ll.setLineColor(Color.GRAY)
        ll.setLineWidth(1f)
        leftAxis.setGranularity(priceOnFirstDay)
        leftAxis.setLabelCount(0, true)
        leftAxis.addLimitLine(ll)



        // if disabled, scaling can be done on x- and y-axis separately
        graph.setPinchZoom(false)

        val l = graph.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
    }
}
