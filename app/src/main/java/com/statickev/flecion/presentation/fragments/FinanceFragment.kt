package com.statickev.flecion.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.statickev.flecion.R
import com.statickev.flecion.presentation.viewModel.FinanceViewModel
import kotlin.getValue

class FinanceFragment : Fragment() {

    companion object {
        fun newInstance() = FinanceFragment()
    }

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_finance, container, false)
    }
}
