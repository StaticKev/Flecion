package com.statickev.flecion.presentation.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.databinding.CardTaskBinding
import com.statickev.flecion.presentation.activity.TaskDetailActivity
import com.statickev.flecion.util.getDateFormatter
import com.statickev.flecion.util.minsToFormattedDuration

class TaskAdapter(
    private var tasks: List<Task>
): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    companion object {
        const val TASK_ID = "task_id"
    }

    class TaskViewHolder(val binding: CardTaskBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {
        val binding = CardTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {
        val task = tasks[position]
        with(holder.binding) {
            tvTitle.text = task.title

            if (task.timeLeftToComplete == 0) tvTimeToComplete.text = ""
            else tvTimeToComplete.text = buildString {
                append(minsToFormattedDuration(task.timeLeftToComplete))
                append(" left")
            }

            tvDueDate.text = task.due?.toLocalDate()?.format(getDateFormatter()) ?: ""
            pbProgress.progress = task.completionRate
            tvProgressPercentage.text = buildString {
                append(task.completionRate.toString())
                append("%")
            }
        }

        holder.binding.cvTask.setOnClickListener {
            val intent = Intent(holder.binding.cvTask.context, TaskDetailActivity::class.java)
            intent.putExtra(TASK_ID, task.id)

            holder.binding.cvTask.context.startActivity(intent)
        }
    }

    override fun getItemCount() = tasks.size

    fun getTaskAt(position: Int): Task = tasks[position]

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}