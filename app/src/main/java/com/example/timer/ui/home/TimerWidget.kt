package com.example.timer.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.timer.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class TimerWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttrs) {

    private var isStarted = false
    private var clock: Clock
    private val startStopButton: Button
    private val resetButton: Button
    private val timeTextView: TextView
    private var counterListeners = mutableSetOf<(Long) -> Unit>()

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("HH: mm: ss")
    private var counter: Long = 0
        set(value) {
            field=value
            counterListeners.forEach { it( field) }}
    private var job: Job? = null


    init {
        val root = inflate(context, R.layout.timer_widget, this)
        clock = root.findViewById(R.id.clock)
        startStopButton = root.findViewById(R.id.startButton)
        resetButton = root.findViewById(R.id.resetButton)
        timeTextView = root.findViewById(R.id.timerTextView)
        startStopButton.setOnClickListener { if (!isStarted) start() else stop() }
        resetButton.setOnClickListener {
            reset()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    fun addListener(listener: (Long) -> Unit) {
        counterListeners.add(listener)
        listener(counter)
    }
    fun removeListener(listener: (Long) -> Unit){
        counterListeners.remove ( listener )
    }


    fun start() {
        clock.isTimer = false
        clock.isStarted = true
        startStopButton.text = resources.getText(R.string.stop)
        isStarted = true
        job = rootView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            while (true) {
                counter = Calendar.getInstance().timeInMillis
                val text = formatter.format(Date(counter))
                timeTextView.text = text
                delay(500)

            }
        }
    }

    fun stop() {
        job?.cancel()
        startStopButton.text = resources.getText(R.string.start)
        isStarted = false
        clock.isStarted = false
        timeTextView.text = if (!clock.isTimer) formatter.format(Date(counter)) else getTimerText()
    }

    fun reset() {
        job?.cancel()
        counter = 0
        clock.counter = 0
        clock.isTimer = true
        if (isStarted) {
            job = rootView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                while (true) {
                    timeTextView.text = getTimerText()
                    delay(1000)
                    if (counter.toInt() == 86399) counter = 0 else counter++
                    if (clock.counter == 86399) clock.counter else clock.counter++
                }
            }
        } else {
            timeTextView.text = getTimerText()
        }
    }

    private fun getTimerText(): String {
        val hours = (counter.toDouble() / 3600).toInt()
        val minutes = ((counter - hours * 3600).toDouble() / 60).toInt()
        val seconds = ((counter - hours * 3600) - minutes * 60).toInt()
        val hoursText: String = if (hours < 10) "0$hours" else hours.toString()
        val minutesText: String = if (minutes < 10) "0$minutes" else minutes.toString()
        val secondsText: String = if (seconds < 10) "0$seconds" else seconds.toString()
        return "$hoursText: $minutesText: $secondsText"
    }
}

