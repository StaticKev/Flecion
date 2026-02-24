package com.statickev.flecion.presentation.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.statickev.flecion.R
import com.statickev.flecion.data.model.Task
import com.statickev.flecion.databinding.CardRecurringTaskBinding
import com.statickev.flecion.presentation.activity.RecurringTaskDetailActivity
import com.statickev.flecion.presentation.adapter.TaskAdapter.Companion.TASK_ID
import com.statickev.flecion.util.getDateTimeFormatter

class RecurringTaskAdapter(
    private var tasks: List<Task> = emptyList(),
    private val onToggleNotification: (Task) -> Unit
) : RecyclerView.Adapter<RecurringTaskAdapter.RecurringTaskViewHolder>() {

    class RecurringTaskViewHolder(val binding: CardRecurringTaskBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecurringTaskViewHolder {
        val binding = CardRecurringTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecurringTaskViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecurringTaskViewHolder,
        position: Int
    ) {
        val task = tasks[position]
        with(holder.binding) {
            tvTitle.text = task.title

            task.remindAt?.let {
                tvNextReminder.text = it.format(getDateTimeFormatter())
            }
            if (task.sendNotification) btnEnableNotification.setIconResource(R.drawable.baseline_notifications_active_24)
            else btnEnableNotification.setIconResource(R.drawable.baseline_notifications_off_24)

            cvTask.setOnClickListener {
                val intent = Intent(holder.binding.cvTask.context, RecurringTaskDetailActivity::class.java)
                intent.putExtra(TASK_ID, task.id)

                holder.binding.cvTask.context.startActivity(intent)
            }
            btnEnableNotification.setOnClickListener {
                onToggleNotification(task)
            }
        }
    }

    override fun getItemCount() = tasks.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}