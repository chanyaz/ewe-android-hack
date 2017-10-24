package com.expedia.bookings.launch.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrameLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
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

    fun setData(data: HashMap<String, ArrayList<Float>>, startDate: LocalDate) {
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

            val color = lineColors[dataCount % lineColors.size]
            lineDataSet.setColor(color)
            lineDataSet.setCircleColor(color)
            dataSets.add(lineDataSet)

            val data = LineData(dataSets)
            dataCount++

            val priceGraph = LineChart(context, attrs)
            priceGraphContainer.addView(priceGraph)

            val params = priceGraph.layoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.graph_height)
            priceGraph.layoutParams = params

            setUpGraph(priceGraph)

            priceGraph.data = data
            priceGraph.invalidate()
        }
    }

    private fun setUpGraph(graph: LineChart) {
//        graph.setOnChartValueSelectedListener(this)

        graph.setDrawGridBackground(false)
//        Drawable background = getDrawable(R.drawable.new_background);
//        graph.setBackground(background);
        graph.description.isEnabled = false
        graph.setDrawBorders(false)

        graph.axisLeft.isEnabled = false

        graph.axisRight.setDrawAxisLine(true)
        graph.axisRight.setDrawGridLines(true)
        // formatRate(BigDecimal amount, String currencyCode, int flags) in case want to format, should use flag no decimal, and take param currencyCode

        graph.xAxis.setDrawAxisLine(true)
        graph.xAxis.setDrawGridLines(false)

        graph.xAxis.setValueFormatter( { value, _ ->
            val date = startDate.plusDays(value.toInt())
//            dateFormatter.format(date)
            date.toString("dd/MM")
        })

        // enable touch gestures
//        graph.setTouchEnabled(true)

        // enable scaling and dragging
        graph.isDragEnabled = true
//        graph.setScaleEnabled(true)
        graph.isScaleYEnabled = true
        graph.isScaleXEnabled = false

        // if disabled, scaling can be done on x- and y-axis separately
        graph.setPinchZoom(false)

        val l = graph.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(true)
    }
}
