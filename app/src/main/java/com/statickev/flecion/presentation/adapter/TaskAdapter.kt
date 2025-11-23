package com.statickev.flecion.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.databinding.CardTaskBinding
import com.statickev.flecion.util.getDateFormatter
import com.statickev.flecion.util.minsToFormattedDuration

class TaskAdapter(
    private var tasks: List<Task>
): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
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

    // TODO: Update this to accommodate card modification
    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {
        val task = tasks[position]
        with(holder.binding) {
            tvTitle.text = task.title
            tvTimeToComplete.text = buildString {
                append(minsToFormattedDuration(task.actualTimeToCompleteMins))
                append(" left")
            }
            tvDueDate.text = task.due?.let { it.toLocalDate().format(getDateFormatter()) } ?: ""
            pbProgress.progress = task.progressPercentage
            tvProgressPercentage.text = buildString {
                append(task.progressPercentage.toString())
                append("%")
            }
        }
    }

    override fun getItemCount() = tasks.size

    fun submitList(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}