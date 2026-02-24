package com.statickev.flecion.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.statickev.flecion.databinding.FragmentWeeklyCalendarBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@AndroidEntryPoint
class WeeklyCalendarFragment : Fragment() {
    private lateinit var binding: FragmentWeeklyCalendarBinding
//    private val viewModel: WeeklyCalendarViewModel by viewModels()
    private lateinit var chosenDoW: DayOfWeek

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeeklyCalendarBinding.inflate(layoutInflater)

        val today = LocalDateTime.now()

        val sunday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

        val weekDates = mapOf(
            DayOfWeek.SUNDAY to sunday,
            DayOfWeek.MONDAY to sunday.plusDays(1),
            DayOfWeek.TUESDAY to sunday.plusDays(2),
            DayOfWeek.WEDNESDAY to sunday.plusDays(3),
            DayOfWeek.THURSDAY to sunday.plusDays(4),
            DayOfWeek.FRIDAY to sunday.plusDays(5),
            DayOfWeek.SATURDAY to sunday.plusDays(6)
        )

        with(binding) {
            tvSun.text = weekDates[DayOfWeek.SUNDAY]?.dayOfMonth.toString()
            tvMon.text = weekDates[DayOfWeek.MONDAY]?.dayOfMonth.toString()
            tvTue.text = weekDates[DayOfWeek.TUESDAY]?.dayOfMonth.toString()
            tvWed.text = weekDates[DayOfWeek.WEDNESDAY]?.dayOfMonth.toString()
            tvThu.text = weekDates[DayOfWeek.THURSDAY]?.dayOfMonth.toString()
            tvFri.text = weekDates[DayOfWeek.FRIDAY]?.dayOfMonth.toString()
            tvSat.text = weekDates[DayOfWeek.SATURDAY]?.dayOfMonth.toString()

            btnSun.setOnClickListener {

            }
            btnMon.setOnClickListener {

            }
            btnTue.setOnClickListener {

            }
            btnWed.setOnClickListener {

            }
            btnThu.setOnClickListener {

            }
            btnFri.setOnClickListener {

            }
            btnSat.setOnClickListener {

            }
        }

        return binding.root
    }
}