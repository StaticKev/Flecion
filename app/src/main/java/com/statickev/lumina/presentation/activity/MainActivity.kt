package com.statickev.lumina.presentation.activity

import android.os.Build
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.statickev.lumina.R
import com.statickev.lumina.databinding.ActivityMainBinding
import com.statickev.lumina.databinding.CardStatusOngoingBinding
import com.statickev.lumina.databinding.CardStatusOnholdBinding
import com.statickev.lumina.databinding.CardStatusPendingBinding
import com.statickev.lumina.presentation.utility.highlight
import com.statickev.lumina.presentation.utility.generalSetup
import com.statickev.lumina.presentation.utility.unhighlight
import com.statickev.lumina.presentation.viewModel.TaskViewModel
import com.statickev.lumina.util.getFormattedDate
import java.time.LocalDate

private const val popupMenuDuration: Long = 300

class MainActivity : AppCompatActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var pendingCardBinding: CardStatusPendingBinding
    private lateinit var onHoldCardBinding: CardStatusOnholdBinding
    private lateinit var ongoingCardBinding: CardStatusOngoingBinding
    private var tempCardBinding: ViewBinding? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        generalSetup(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        pendingCardBinding = CardStatusPendingBinding.bind(binding.root.findViewById(R.id.cv_pending))
        onHoldCardBinding = CardStatusOnholdBinding.bind(binding.root.findViewById(R.id.cv_onhold))
        ongoingCardBinding = CardStatusOngoingBinding.bind(binding.root.findViewById(R.id.cv_ongoing))

        cvStatusSetOnClickListener(pendingCardBinding)
        cvStatusSetOnClickListener(onHoldCardBinding)
        cvStatusSetOnClickListener(ongoingCardBinding)

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

        binding.tvDate.text = getFormattedDate(LocalDate.now())

        setContentView(binding.root)
    }

    private fun cvStatusSetOnClickListener(binding: ViewBinding) {
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

            if (tempCardBinding?.let { tempBinding::class == it::class } == true) {
                when (binding) {
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
                tempCardBinding = null

                this.binding.root.setBackgroundColor(ContextCompat.getColor(
                    this,
                    R.color.light_gray_sec
                ))

                this.binding.fabAdd.backgroundTintList = ContextCompat.getColorStateList(
                    this.binding.root.context,
                    R.color.light_gray_pri
                )

                this.binding.popupMenu.apply {
                    animate()
                        .translationY(0f)
                        .setDuration(popupMenuDuration)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }

                return@setOnClickListener
            } else {
                with (this.binding) {
                    popupMenu.animate()
                        .translationY(popupMenu.height.toFloat())
                        .setDuration(popupMenuDuration)
                        .setInterpolator(AccelerateInterpolator())
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
                is CardStatusPendingBinding -> binding.highlight(
                    this.binding,
                    onHoldCardBinding,
                    ongoingCardBinding
                )
                is CardStatusOnholdBinding -> binding.highlight(
                    this.binding,
                    pendingCardBinding,
                    ongoingCardBinding
                )
                is CardStatusOngoingBinding -> binding.highlight(
                    this.binding,
                    pendingCardBinding,
                    onHoldCardBinding
                )
            }

            tempCardBinding = binding
        }
    }
}