package com.statickev.flecion.presentation.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.statickev.flecion.R
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.databinding.ActivityMainBinding
import com.statickev.flecion.databinding.CardQuoteBinding
import com.statickev.flecion.databinding.CardStatusOngoingBinding
import com.statickev.flecion.databinding.CardStatusOnholdBinding
import com.statickev.flecion.databinding.CardStatusPendingBinding
import com.statickev.flecion.presentation.adapter.TaskAdapter
import com.statickev.flecion.presentation.presentationUtil.highlight
import com.statickev.flecion.presentation.presentationUtil.generalSetup
import com.statickev.flecion.presentation.presentationUtil.showSnackbar
import com.statickev.flecion.presentation.presentationUtil.unhighlight
import com.statickev.flecion.presentation.viewModel.TaskViewModel
import com.statickev.flecion.util.getDayDateFormatter
import com.statickev.flecion.util.getGreeting
import com.statickev.flecion.util.minsToFormattedDuration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

private const val animateDuration: Long = 300

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private var taskJob: Job? = null
    private lateinit var adapter: TaskAdapter

    private lateinit var binding: ActivityMainBinding
    private lateinit var pendingCardBinding: CardStatusPendingBinding
    private lateinit var onHoldCardBinding: CardStatusOnholdBinding
    private lateinit var ongoingCardBinding: CardStatusOngoingBinding
    private var tempCardBinding: ViewBinding? = null
    private lateinit var quoteCardBiding: CardQuoteBinding
    private val addTaskLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val taskCreated = result.data?.getBooleanExtra("taskCreated", false) ?: false
                if (taskCreated) {
                    showSnackbar(binding.root, "Task added!")
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generalSetup(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        pendingCardBinding = CardStatusPendingBinding.bind(binding.root.findViewById(R.id.cv_pending))
        onHoldCardBinding = CardStatusOnholdBinding.bind(binding.root.findViewById(R.id.cv_onhold))
        ongoingCardBinding = CardStatusOngoingBinding.bind(binding.root.findViewById(R.id.cv_ongoing))
        quoteCardBiding = CardQuoteBinding.bind(binding.root.findViewById(R.id.cv_quote))

        val sampleTasks = buildList {
            repeat(8) { i ->
                add(
                    Task(
                        title = "Pending Task ${i + 1}",
                        description = "This is a pending task #${i + 1}",
                        status = TaskStatus.PENDING,
                        priorityLevel = ((i % 3) + 1).toByte(),
                        timeToCompleteMins = (i + 2) * 10,
                        completionRate = 20 * (i % 5),
                        doAt = LocalDateTime.now().plusHours(i.toLong()),
                        remindAt = LocalDateTime.now().plusHours(i.toLong() + 1),
                        due = LocalDateTime.now().plusDays((i % 5).toLong()),
                        addToCalendar = i % 2 == 0
                    )
                )
            }

            repeat(3) { i ->
                add(
                    Task(
                        title = "On Hold Task ${i + 1}",
                        description = "This task is temporarily paused #${i + 1}",
                        status = TaskStatus.ON_HOLD,
                        priorityLevel = ((i % 3) + 1).toByte(),
                        timeToCompleteMins = 90 + i * 15,
                        completionRate = 20 * (i % 5),
                        doAt = LocalDateTime.now().minusDays(i.toLong()),
                        remindAt = LocalDateTime.now().minusDays(i.toLong() - 1),
                        due = LocalDateTime.now().plusDays((i % 7).toLong()),
                        addToCalendar = true
                    )
                )
            }

            repeat(5) { i ->
                add(
                    Task(
                        title = "Ongoing Task ${i + 1}",
                        description = "Currently being worked on #${i + 1}",
                        status = TaskStatus.ONGOING,
                        priorityLevel = ((i % 3) + 1).toByte(),
                        timeToCompleteMins = 120 + i * 20,
                        completionRate = 20 * (i % 5),
                        doAt = LocalDateTime.now().minusHours(i.toLong() * 2),
                        remindAt = LocalDateTime.now().plusHours((i + 1).toLong()),
                        due = LocalDateTime.now().plusDays((i % 3).toLong()),
                        addToCalendar = i % 2 == 1
                    )
                )
            }
        } // TODO: Delete on production.
        taskViewModel.addTasks(sampleTasks) // TODO: Delete on production.

        adapter = TaskAdapter(emptyList())
        binding.rvTasks.adapter = adapter
        binding.rvTasks.layoutManager = LinearLayoutManager(this)

        pendingCardBinding.unhighlight(
            onHoldCardBinding,
            ongoingCardBinding
        )
        onHoldCardBinding.unhighlight(
            pendingCardBinding,
            ongoingCardBinding
        )
        ongoingCardBinding.unhighlight(
            pendingCardBinding,
            onHoldCardBinding
        )

        binding.rvTasks.post {
            binding.rvTasks.translationY = binding.rvTasks.height.toFloat()
        }
        binding.fabAdd.post {
            binding.fabAdd.translationX = binding.fabAdd.width.toFloat() * 2
        }
        binding.tvDate.text = LocalDate.now().format(getDayDateFormatter())
        binding.tvGreeting.text = getGreeting()

        cvStatusSetOnClickListener(pendingCardBinding)
        cvStatusSetOnClickListener(onHoldCardBinding)
        cvStatusSetOnClickListener(ongoingCardBinding)
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, NewTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    taskViewModel.pendingTaskState.collect {
                        """Pending (${it.count()})"""
                            .also { pendingCardBinding.tvTypeCount.text = it }

                        val totalTime = it.sumOf { task -> task.timeLeftToComplete }
                        """Total time: ${minsToFormattedDuration(totalTime)}"""
                            .also { pendingCardBinding.tvTimeToComplete.text = it }
                    }
                }
                launch {
                    taskViewModel.onHoldTaskState.collect {
                        """On Hold (${it.count()})"""
                            .also { onHoldCardBinding.tvTypeCount.text = it }

                        val totalTime = it.sumOf { task -> task.timeLeftToComplete }
                        """Total time: ${minsToFormattedDuration(totalTime)}"""
                            .also { onHoldCardBinding.tvTimeToComplete.text = it }
                    }
                }
                launch {
                    taskViewModel.ongoingTaskState.collect {
                        """Ongoing (${it.count()})"""
                            .also { ongoingCardBinding.tvTypeCount.text = it }

                        val totalTime = it.sumOf { task -> task.timeLeftToComplete }
                        """Total time: ${minsToFormattedDuration(totalTime)}"""
                            .also { ongoingCardBinding.tvTimeToComplete.text = it }
                    }
                }
            }
        }

//        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvTasks)

        setContentView(binding.root)
    }

    private fun cvStatusSetOnClickListener(binding: ViewBinding) {
        fun observeTasks(flow: Flow<List<Task>>) {
            taskJob?.cancel()
            taskJob = lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    flow.collect { tasks ->
                        adapter.submitList(tasks)
                    }
                }
            }
        }

        val cardView = when (binding) {
            is CardStatusPendingBinding -> binding.cvStatus
            is CardStatusOnholdBinding -> binding.cvStatus
            is CardStatusOngoingBinding -> binding.cvStatus
            else -> return
        }

        cardView.setOnClickListener {
            val tempBinding = when (binding) {
                is CardStatusPendingBinding -> binding
                is CardStatusOnholdBinding -> binding
                is CardStatusOngoingBinding -> binding
                else -> return@setOnClickListener
            }

            when (tempBinding) {
                is CardStatusPendingBinding -> observeTasks(taskViewModel.pendingTaskState)
                is CardStatusOnholdBinding -> observeTasks(taskViewModel.onHoldTaskState)
                is CardStatusOngoingBinding -> observeTasks(taskViewModel.ongoingTaskState)
            }

            if (tempCardBinding?.let { tempBinding::class == it::class } == true) {
                when (binding) {
                    is CardStatusPendingBinding -> {
                        (tempCardBinding as CardStatusPendingBinding).unhighlight(
                            onHoldCardBinding,
                            ongoingCardBinding
                        )
                    }
                    is CardStatusOnholdBinding -> {
                        (tempCardBinding as CardStatusOnholdBinding).unhighlight(
                            pendingCardBinding,
                            ongoingCardBinding
                        )
                    }
                    is CardStatusOngoingBinding -> {
                        (tempCardBinding as CardStatusOngoingBinding).unhighlight(
                            pendingCardBinding,
                            onHoldCardBinding
                        )
                    }
                }
                tempCardBinding = null

                with (this.binding) {
//                    root.setBackgroundColor(ContextCompat.getColor(
//                        root.context,
//                        R.color.light_gray_sec
//                    ))

                    popupMenu.apply {
                        animate()
                            .translationY(0f)
                            .setDuration(animateDuration)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                    }

                    rvTasks.apply {
                        animate()
                            .translationY(this.height.toFloat())
                            .setDuration(animateDuration)
                            .setInterpolator(AccelerateInterpolator())
                            .start()
                    }

                    fabAdd.apply {
                        animate()
                            .translationX(this.width.toFloat() * 2)
                            .setDuration(animateDuration)
                            .setInterpolator(AccelerateInterpolator())
                            .start()
                    }
                }

                return@setOnClickListener
            } else {
                with (this.binding) {
                    popupMenu.animate()
                        .translationY(popupMenu.height.toFloat())
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    rvTasks.animate()
                        .translationY(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(DecelerateInterpolator())
                        .start()

                    fabAdd.animate()
                        .translationX(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
            }

            when (tempCardBinding) {
                is CardStatusPendingBinding -> (tempCardBinding as CardStatusPendingBinding).unhighlight(
                    onHoldCardBinding,
                    ongoingCardBinding
                )
                is CardStatusOnholdBinding -> (tempCardBinding as CardStatusOnholdBinding).unhighlight(
                    pendingCardBinding,
                    ongoingCardBinding
                )
                is CardStatusOngoingBinding -> (tempCardBinding as CardStatusOngoingBinding).unhighlight(
                    pendingCardBinding,
                    onHoldCardBinding
                )
            }

            when (binding) {
                is CardStatusPendingBinding -> {
                    binding.highlight(
                        this.binding,
                        onHoldCardBinding,
                        ongoingCardBinding
                    )
                    this.binding.svStatusButton.smoothScrollTo(
                        this.binding.svStatusButton.getChildAt(0).right,
                        0
                    )
                }
                is CardStatusOnholdBinding -> {
                    binding.highlight(
                        this.binding,
                        pendingCardBinding,
                        ongoingCardBinding
                    )
                    val ll = this.binding.svStatusButton.getChildAt(0) as LinearLayout
                    val middle = ll.getChildAt(0).width / 2
                    this.binding.svStatusButton.smoothScrollTo(
                        this.binding.svStatusButton.getChildAt(0).left + middle,
                        0
                    )
                }
                is CardStatusOngoingBinding -> {
                    binding.highlight(
                        this.binding,
                        pendingCardBinding,
                        onHoldCardBinding
                    )
                    this.binding.svStatusButton.smoothScrollTo(0, 0)
                }
            }

            tempCardBinding = binding
        }
    }

//    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
//        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
//        0 // swipe disabled
//    ) {
//        override fun onMove(
//            recyclerView: RecyclerView,
//            viewHolder: RecyclerView.ViewHolder,
//            target: RecyclerView.ViewHolder
//        ): Boolean {
//            // TODO: Implement the move action later.
////            val fromPos = viewHolder.adapterPosition
////            val toPos = target.adapterPosition
////
////            adapter.moveItem(fromPos, toPos)
//            return true
//        }
//
//        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//            // not needed since we don’t swipe
//        }
//
//        override fun isLongPressDragEnabled(): Boolean = true
//    }

}