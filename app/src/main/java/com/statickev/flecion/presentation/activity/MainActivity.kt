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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.statickev.flecion.R
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.data.model.TaskStatus
import com.statickev.flecion.databinding.ActivityMainBinding
import com.statickev.flecion.presentation.adapter.TaskAdapter
import com.statickev.flecion.presentation.presentationUtil.generalSetup
import com.statickev.flecion.presentation.presentationUtil.showSnackbar
import com.statickev.flecion.presentation.presentationUtil.showUndoSnackbar
import com.statickev.flecion.presentation.viewModel.TaskViewModel
import com.statickev.flecion.util.getDayDateFormatter
import com.statickev.flecion.util.getGreeting
import com.statickev.flecion.util.minsToFormattedDuration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

private const val animateDuration: Long = 300
private const val shiftDuration: Long = 150
private const val shiftDistance = 30f

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private var selectedCard: CardView? = null
    private lateinit var taskAdapter: TaskAdapter
    private var taskJob: Job? = null

    private val addTaskLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val taskCreated = result.data?.getBooleanExtra("taskCreated", false) ?: false
                if (taskCreated) {
                    showSnackbar(binding.root, "Task added")
                }
            }
        }
    val ithDeleteTask = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val task = taskAdapter.getTaskAt(position)
                taskViewModel.deleteTask(task)
            }
        }
    )

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generalSetup(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

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

        with (binding) {
            taskAdapter = TaskAdapter(emptyList())
            rvTasks.adapter = taskAdapter
            rvTasks.layoutManager = LinearLayoutManager(this.root.context)

            rvTasks.post {
                rvTasks.translationY = rvTasks.height.toFloat() + rvTasks.paddingBottom
            }
            ithDeleteTask.attachToRecyclerView(rvTasks)

            tvDate.text = LocalDate.now().format(getDayDateFormatter())

            btnMenu.setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                else drawerLayout.openDrawer(GravityCompat.START)
            }

            cvPending.setOnClickListener {
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        taskViewModel.pendingTaskState.collect { tasks ->
                            taskAdapter.submitList(tasks)
                        }
                    }
                }

                if (selectedCard?.id == R.id.cv_pending) {
                    cvPending.scaleX = 1f
                    cvPending.scaleY = 1f
                    cvPending.setCardBackgroundColor(ContextCompat.getColor(
                        root.context,
                        R.color.md_theme_outlineVariant
                    ))

                    cvOnhold.animate()
                        .translationX(0f)
                        .setDuration(shiftDuration)
                        .start()
                    cvOngoing.animate()
                        .translationX(0f)
                        .setDuration(shiftDuration)
                        .start()

                    rvTasks.animate()
                        .translationY(rvTasks.height.toFloat() + rvTasks.paddingBottom)
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    flContainer.animate()
                        .translationY(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    selectedCard = null
                }
                else {
                    cvPending.scaleX = 1.1f
                    cvPending.scaleY = 1.1f
                    cvPending.setCardBackgroundColor(ContextCompat.getColor(
                        root.context,
                        R.color.md_theme_primaryContainer
                    ))

                    cvOnhold.animate()
                        .translationX(-shiftDistance)
                        .setDuration(shiftDuration)
                        .start()
                    cvOngoing.animate()
                        .translationX(-shiftDistance)
                        .setDuration(shiftDuration)
                        .start()

                    svStatusButton.smoothScrollTo(
                        svStatusButton.getChildAt(0).right,
                        0
                    )

                    when (selectedCard?.id) {
                        R.id.cv_onhold -> {
                            cvOnhold.scaleX = 1f
                            cvOnhold.scaleY = 1f
                            cvOnhold.setCardBackgroundColor(ContextCompat.getColor(
                                root.context,
                                R.color.md_theme_outlineVariant
                            ))

                            cvPending.animate()
                                .translationX(0f)
                                .setDuration(150)
                                .start()
                            cvOngoing.animate()
                                .translationX(-shiftDistance)
                                .setDuration(150)
                                .start()
                        }
                        R.id.cv_ongoing -> {
                            cvOngoing.scaleX = 1f
                            cvOngoing.scaleY = 1f
                            cvOngoing.setCardBackgroundColor(ContextCompat.getColor(
                                root.context,
                                R.color.md_theme_outlineVariant
                            ))

                            cvPending.animate()
                                .translationX(0f)
                                .setDuration(150)
                                .start()
                            cvOnhold.animate()
                                .translationX(-shiftDistance)
                                .setDuration(150)
                                .start()
                        }
                    }

                    rvTasks.animate()
                        .translationY(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(DecelerateInterpolator())
                        .start()

                    flContainer.animate()
                        .translationY(-flContainer.height.toFloat())
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    selectedCard = cvPending
                }
            }

            cvOnhold.setOnClickListener {
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        taskViewModel.onHoldTaskState.collect { tasks ->
                            taskAdapter.submitList(tasks)
                        }
                    }
                }

                if (selectedCard?.id == R.id.cv_onhold) {
                    cvOnhold.scaleX = 1f
                    cvOnhold.scaleY = 1f
                    cvOnhold.setCardBackgroundColor(ContextCompat.getColor(
                        root.context,
                        R.color.md_theme_outlineVariant
                    ))

                    cvPending.animate()
                        .translationX(0f)
                        .setDuration(150)
                        .start()
                    cvOngoing.animate()
                        .translationX(0f)
                        .setDuration(150)
                        .start()

                    rvTasks.animate()
                        .translationY(rvTasks.height.toFloat() + rvTasks.paddingBottom)
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    flContainer.animate()
                        .translationY(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    selectedCard = null
                }
                else {
                    cvOnhold.scaleX = 1.1f
                    cvOnhold.scaleY = 1.1f
                    cvOnhold.setCardBackgroundColor(ContextCompat.getColor(
                        root.context,
                        R.color.md_theme_primaryContainer
                    ))

                    cvPending.animate()
                        .translationX(shiftDistance)
                        .setDuration(150)
                        .start()
                    cvOngoing.animate()
                        .translationX(-shiftDistance)
                        .setDuration(150)
                        .start()

                    val ll = svStatusButton.getChildAt(0) as LinearLayout
                    val middle = ll.getChildAt(0).width / 2 - 45
                    svStatusButton.smoothScrollTo(
                        svStatusButton.getChildAt(0).left + middle,
                        0
                    )

                    when (selectedCard?.id) {
                        R.id.cv_pending -> {
                            cvPending.scaleX = 1f
                            cvPending.scaleY = 1f
                            cvPending.setCardBackgroundColor(ContextCompat.getColor(
                                root.context,
                                R.color.md_theme_outlineVariant
                            ))

                            cvOnhold.animate()
                                .translationX(0f)
                                .setDuration(150)
                                .start()
                            cvOngoing.animate()
                                .translationX(-shiftDistance)
                                .setDuration(150)
                                .start()
                        }
                        R.id.cv_ongoing -> {
                            cvOngoing.scaleX = 1f
                            cvOngoing.scaleY = 1f
                            cvOngoing.setCardBackgroundColor(ContextCompat.getColor(
                                root.context,
                                R.color.md_theme_outlineVariant
                            ))

                            cvPending.animate()
                                .translationX(shiftDistance)
                                .setDuration(150)
                                .start()
                            cvOnhold.animate()
                                .translationX(0f)
                                .setDuration(150)
                                .start()
                        }
                    }

                    rvTasks.animate()
                        .translationY(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(DecelerateInterpolator())
                        .start()

                    flContainer.animate()
                        .translationY(-flContainer.height.toFloat())
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    selectedCard = cvOnhold
                }
            }

            cvOngoing.setOnClickListener {
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        taskViewModel.ongoingTaskState.collect { tasks ->
                            taskAdapter.submitList(tasks)
                        }
                    }
                }

                if (selectedCard?.id == R.id.cv_ongoing) {
                    cvOngoing.scaleX = 1f
                    cvOngoing.scaleY = 1f
                    cvOngoing.setCardBackgroundColor(ContextCompat.getColor(
                        root.context,
                        R.color.md_theme_outlineVariant
                    ))

                    cvPending.animate()
                        .translationX(0f)
                        .setDuration(150)
                        .start()
                    cvOnhold.animate()
                        .translationX(0f)
                        .setDuration(150)
                        .start()

                    rvTasks.animate()
                        .translationY(rvTasks.height.toFloat() + rvTasks.paddingBottom)
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    flContainer.animate()
                        .translationY(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    selectedCard = null
                }
                else {
                    cvOngoing.scaleX = 1.1f
                    cvOngoing.scaleY = 1.1f
                    cvOngoing.setCardBackgroundColor(ContextCompat.getColor(
                        root.context,
                        R.color.md_theme_primaryContainer
                    ))

                    cvPending.animate()
                        .translationX(shiftDistance)
                        .setDuration(150)
                        .start()
                    cvOnhold.animate()
                        .translationX(shiftDistance)
                        .setDuration(150)
                        .start()

                    svStatusButton.smoothScrollTo(0, 0)

                    when (selectedCard?.id) {
                        R.id.cv_pending -> {
                            cvPending.scaleX = 1f
                            cvPending.scaleY = 1f
                            cvPending.setCardBackgroundColor(ContextCompat.getColor(
                                root.context,
                                R.color.md_theme_outlineVariant
                            ))

                            cvOnhold.animate()
                                .translationX(shiftDistance)
                                .setDuration(150)
                                .start()
                            cvOngoing.animate()
                                .translationX(0f)
                                .setDuration(150)
                                .start()
                        }
                        R.id.cv_onhold -> {
                            cvOnhold.scaleX = 1f
                            cvOnhold.scaleY = 1f
                            cvOnhold.setCardBackgroundColor(ContextCompat.getColor(
                                root.context,
                                R.color.md_theme_outlineVariant
                            ))

                            cvPending.animate()
                                .translationX(shiftDistance)
                                .setDuration(150)
                                .start()
                            cvOngoing.animate()
                                .translationX(0f)
                                .setDuration(150)
                                .start()
                        }
                    }

                    rvTasks.animate()
                        .translationY(0f)
                        .setDuration(animateDuration)
                        .setInterpolator(DecelerateInterpolator())
                        .start()

                    flContainer.animate()
                        .translationY(-flContainer.height.toFloat())
                        .setDuration(animateDuration)
                        .setInterpolator(AccelerateInterpolator())
                        .start()

                    selectedCard = cvOngoing
                }
            }

            fabAdd.setOnClickListener {
                val intent = Intent(this.root.context, NewTaskActivity::class.java)
                addTaskLauncher.launch(intent)
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        taskViewModel.pendingTaskState.collect { it ->
                            """Pending (${it.count()})"""
                                .also { tvTypeCountPending.text = it }

                            val totalTime = it.sumOf { task -> task.timeLeftToComplete }
                            """Total time: ${minsToFormattedDuration(totalTime)}"""
                                .also { tvTimeToCompletePending.text = it }
                        }
                    }
                    launch {
                        taskViewModel.onHoldTaskState.collect { it ->
                            """On Hold (${it.count()})"""
                                .also { tvTypeCountOnhold.text = it }

                            val totalTime = it.sumOf { task -> task.timeLeftToComplete }
                            """Total time: ${minsToFormattedDuration(totalTime)}"""
                                .also { tvTimeToCompleteOnhold.text = it }
                        }
                    }
                    launch {
                        taskViewModel.ongoingTaskState.collect { it ->
                            """Ongoing (${it.count()})"""
                                .also { tvTypeCountOngoing.text = it }

                            val totalTime = it.sumOf { task -> task.timeLeftToComplete }
                            """Total time: ${minsToFormattedDuration(totalTime)}"""
                                .also { tvTimeToCompleteOngoing.text = it }
                        }
                    }
                    launch {
                        taskViewModel.uiEvent.collect { event ->
                            when (event) {
                                is TaskViewModel.UiEvent.ShowSnackbarOnDelete -> showUndoSnackbar(
                                    binding.root,
                                    event.message
                                ) {
                                    taskViewModel.addTask(event.deletedTask)
                                }
                            }
                        }
                    }
                }
            }
        }

        setContentView(binding.root)
    }
}