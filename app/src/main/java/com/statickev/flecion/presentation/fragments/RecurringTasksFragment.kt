package com.statickev.flecion.presentation.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.statickev.flecion.R
import com.statickev.flecion.databinding.FragmentRecurringTasksBinding
import com.statickev.flecion.databinding.FragmentTaskBinding
import com.statickev.flecion.presentation.activity.NewTaskActivity
import com.statickev.flecion.presentation.activity.NewTaskActivity.Companion.IS_RECURRING
import com.statickev.flecion.presentation.adapter.RecurringTaskAdapter
import com.statickev.flecion.presentation.adapter.TaskAdapter
import com.statickev.flecion.presentation.presentationUtil.showSnackbar
import com.statickev.flecion.presentation.presentationUtil.showUndoSnackbar
import com.statickev.flecion.presentation.viewModel.RecurringTasksViewModel
import com.statickev.flecion.presentation.viewModel.TaskViewModel
import com.statickev.flecion.util.minsToFormattedDuration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecurringTasksFragment : Fragment() {

    companion object {
        fun newInstance() = RecurringTasksFragment()
    }

    private lateinit var binding: FragmentRecurringTasksBinding
    private val viewModel: RecurringTasksViewModel by viewModels()
    private var taskJob: Job? = null

    private lateinit var taskAdapter: RecurringTaskAdapter

    private val addTaskLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val taskCreated = result.data?.getBooleanExtra("taskCreated", false) ?: false
                if (taskCreated) {
                    showSnackbar(binding.root, "Task added")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecurringTasksBinding.inflate(layoutInflater)

        with (binding) {
            taskAdapter = RecurringTaskAdapter { task ->
                viewModel.toggleNotification(task)
            }
            rvTasks.adapter = taskAdapter
            rvTasks.layoutManager = LinearLayoutManager(this.root.context)

            taskJob?.cancel()
            taskJob = viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.taskState.collect { tasks ->
                        taskAdapter.submitList(tasks)
                    }
                }
            }

            fabAdd.setOnClickListener {
                val intent = Intent(this.root.context, NewTaskActivity::class.java)
                intent.putExtra(IS_RECURRING, true)
                addTaskLauncher.launch(intent)
            }
        }

        return binding.root
    }
}