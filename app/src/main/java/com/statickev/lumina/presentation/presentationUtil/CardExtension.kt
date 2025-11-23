package com.statickev.lumina.presentation.presentationUtil

import androidx.core.content.ContextCompat
import com.statickev.lumina.R
import com.statickev.lumina.databinding.ActivityMainBinding
import com.statickev.lumina.databinding.CardStatusOngoingBinding
import com.statickev.lumina.databinding.CardStatusOnholdBinding
import com.statickev.lumina.databinding.CardStatusPendingBinding

private const val shiftDistance = 40f
private const val shiftDuration: Long = 150

fun CardStatusPendingBinding.highlight(
    mainActivityBinding: ActivityMainBinding,
    cardOnholdBinding: CardStatusOnholdBinding,
    cardOngoingBinding: CardStatusOngoingBinding
) {
    cvStatus.scaleX = 1.15f
    cvStatus.scaleY = 1.15f

    cardOnholdBinding.cvStatus.animate()
        .translationX(-shiftDistance)
        .setDuration(shiftDuration)
        .start()
    cardOngoingBinding.cvStatus.animate()
        .translationX(-shiftDistance)
        .setDuration(shiftDuration)
        .start()

    cvStatus.setCardBackgroundColor(ContextCompat.getColor(
        cvStatus.context,
        R.color.md_theme_primaryContainer
    ))
    mainActivityBinding.fabAdd.backgroundTintList = ContextCompat.getColorStateList(
        mainActivityBinding.root.context,
        R.color.md_theme_primary
    )
}

fun CardStatusPendingBinding.unhighlight(
    cardOnholdBinding: CardStatusOnholdBinding,
    cardOngoingBinding: CardStatusOngoingBinding
) {
    cvStatus.scaleX = 1f
    cvStatus.scaleY = 1f

    cardOnholdBinding.cvStatus.animate()
        .translationX(0f)
        .setDuration(shiftDuration)
        .start()
    cardOngoingBinding.cvStatus.animate()
        .translationX(0f)
        .setDuration(shiftDuration)
        .start()

    cvStatus.setCardBackgroundColor(ContextCompat.getColor(
        cvStatus.context,
        R.color.md_theme_outlineVariant
    ))
}

fun CardStatusOnholdBinding.highlight(
    mainActivityBinding: ActivityMainBinding,
    cardPendingBinding: CardStatusPendingBinding,
    cardOngoingBinding: CardStatusOngoingBinding
) {
    cvStatus.scaleX = 1.15f
    cvStatus.scaleY = 1.15f

    cardPendingBinding.cvStatus.animate()
        .translationX(shiftDistance)
        .setDuration(150)
        .start()
    cardOngoingBinding.cvStatus.animate()
        .translationX(-shiftDistance)
        .setDuration(150)
        .start()

    cvStatus.setCardBackgroundColor(ContextCompat.getColor(
        cvStatus.context,
        R.color.md_theme_primaryContainer
    ))
    mainActivityBinding.fabAdd.backgroundTintList = ContextCompat.getColorStateList(
        mainActivityBinding.root.context,
        R.color.md_theme_primary
    )
}

fun CardStatusOnholdBinding.unhighlight(
    cardPendingBinding: CardStatusPendingBinding,
    cardOngoingBinding: CardStatusOngoingBinding
) {
    cvStatus.scaleX = 1f
    cvStatus.scaleY = 1f

    cardPendingBinding.cvStatus.animate()
        .translationX(0f)
        .setDuration(150)
        .start()
    cardOngoingBinding.cvStatus.animate()
        .translationX(0f)
        .setDuration(150)
        .start()

    cvStatus.setCardBackgroundColor(ContextCompat.getColor(
        cvStatus.context,
        R.color.md_theme_outlineVariant
    ))
}

fun CardStatusOngoingBinding.highlight(
    mainActivityBinding: ActivityMainBinding,
    cardPendingBinding: CardStatusPendingBinding,
    cardOnHoldBinding: CardStatusOnholdBinding
) {
    cvStatus.scaleX = 1.15f
    cvStatus.scaleY = 1.15f

    cardPendingBinding.cvStatus.animate()
        .translationX(shiftDistance)
        .setDuration(150)
        .start()
    cardOnHoldBinding.cvStatus.animate()
        .translationX(shiftDistance)
        .setDuration(150)
        .start()

    cvStatus.setCardBackgroundColor(ContextCompat.getColor(
        cvStatus.context,
        R.color.md_theme_primaryContainer
    ))
    mainActivityBinding.fabAdd.backgroundTintList = ContextCompat.getColorStateList(
        mainActivityBinding.root.context,
        R.color.md_theme_primary
    )
}

fun CardStatusOngoingBinding.unhighlight(
    cardPendingBinding: CardStatusPendingBinding,
    cardOnHoldBinding: CardStatusOnholdBinding
) {
    cvStatus.scaleX = 1f
    cvStatus.scaleY = 1f

    cardPendingBinding.cvStatus.animate()
        .translationX(0f)
        .setDuration(150)
        .start()
    cardOnHoldBinding.cvStatus.animate()
        .translationX(0f)
        .setDuration(150)
        .start()

    cvStatus.setCardBackgroundColor(ContextCompat.getColor(
        cvStatus.context,
        R.color.md_theme_outlineVariant
    ))
}