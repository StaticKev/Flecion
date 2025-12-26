package com.statickev.flecion.presentation.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.statickev.flecion.R
import com.statickev.flecion.presentation.viewModel.RecurringTasksViewModel

class RecurringTasksFragment : Fragment() {

    companion object {
        fun newInstance() = RecurringTasksFragment()
    }

    private val viewModel: RecurringTasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recurring_tasks, container, false)
    }
}