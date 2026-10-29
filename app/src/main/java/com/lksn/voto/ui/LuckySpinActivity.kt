package com.lksn.voto.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lksn.voto.R
import com.lksn.voto.data.ApiClient
import com.lksn.voto.data.ApiResult
import com.lksn.voto.data.apiCall
import com.lksn.voto.databinding.ActivityLuckySpinBinding
import com.lksn.voto.util.toast
import kotlinx.coroutines.launch
import kotlin.random.Random

class LuckySpinActivity : BaseActivity() {

    companion object {
        private const val SPIN_COST = 2200
        private const val MIN_TURNS = 3 // the needle must rotate at least 3 times
        private const val SEGMENT_ANGLE = 60f
    }

    /** Segments in the wheel image, clockwise, starting at 12 o'clock. */
    private enum class Prize { PLUS_3000, JACKPOT, PLUS_2500, DRAW_AGAIN, PLUS_1500, TRY_AGAIN }

    private val prizes = Prize.values()

    private lateinit var binding: ActivityLuckySpinBinding
    private var votoken: Int? = null
    private var spinning = false
    private var animator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLuckySpinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { if (!spinning) goHome() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!spinning) goHome()
            }
        })
        binding.ivWheel.setOnClickListener { onWheelTapped() }

        refreshVotoken()
    }

    override fun onDestroy() {
        animator?.cancel()
        super.onDestroy()
    }

    private fun showVotoken(value: Int) {
        votoken = value
        binding.tvVotoken.text = value.toString()
    }

    private fun refreshVotoken() {
        lifecycleScope.launch {
            when (val result = apiCall(this@LuckySpinActivity) { ApiClient.api.getMe() }) {
                is ApiResult.Success -> showVotoken(result.data.votoken)
                is ApiResult.Failure -> showFailure(result)
            }
        }
    }

    private fun onWheelTapped() {
        if (spinning) return
        val current = votoken ?: return // balance not loaded yet
        if (current < SPIN_COST) {
            toast(getString(R.string.token_insufficient))
            return
        }
        spin(current)
    }

    private fun spin(current: Int) {
        spinning = true

        // 1. Decide the outcome up front, from the angle the needle will stop at.
        val currentRotation = ((binding.ivNeedle.rotation % 360f) + 360f) % 360f
        val targetAngle = Random.nextInt(prizes.size) * SEGMENT_ANGLE +
                SEGMENT_ANGLE / 2f + Random.nextInt(-22, 23) // stay clear of the segment borders
        val extraTurns = MIN_TURNS + Random.nextInt(0, 3)
        val delta = extraTurns * 360f + ((targetAngle - currentRotation + 360f) % 360f)
        val finalAngle = (currentRotation + delta) % 360f
        val prize = prizes[(finalAngle / SEGMENT_ANGLE).toInt().coerceIn(0, prizes.size - 1)]

        val reward = when (prize) {
            Prize.PLUS_3000 -> 3000
            Prize.PLUS_2500 -> 2500
            Prize.PLUS_1500 -> 1500
            Prize.JACKPOT -> Random.nextInt(15000, 30001) // 15000..30000 inclusive
            Prize.DRAW_AGAIN -> SPIN_COST // the spin is free: cost refunded
            Prize.TRY_AGAIN -> 0
        }
        val afterCost = current - SPIN_COST
        val finalBalance = afterCost + reward

        // 2. Persist the final balance once (server value is absolute). Only then animate.
        lifecycleScope.launch {
            when (val result = apiCall(this@LuckySpinActivity) { ApiClient.api.updateVotoken(finalBalance) }) {
                is ApiResult.Success -> {
                    showVotoken(afterCost) // the cost is deducted immediately, the prize shows after the spin
                    startAnimation(currentRotation, delta) {
                        showVotoken(finalBalance)
                        spinning = false
                        showResult(prize, reward)
                    }
                }
                is ApiResult.Failure -> {
                    spinning = false
                    showFailure(result)
                }
            }
        }
    }

    private fun startAnimation(from: Float, delta: Float, onEnd: () -> Unit) {
        animator = ObjectAnimator.ofFloat(binding.ivNeedle, View.ROTATION, from, from + delta).apply {
            duration = 4500L + (delta / 360f).toLong() * 250L
            interpolator = DecelerateInterpolator(1.6f)
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!cancelled) onEnd()
                }
            })
            start()
        }
    }

    private fun showResult(prize: Prize, reward: Int) {
        val message = when (prize) {
            Prize.JACKPOT -> getString(R.string.spin_jackpot_format, reward)
            Prize.PLUS_3000, Prize.PLUS_2500, Prize.PLUS_1500 -> getString(R.string.spin_win_format, reward)
            Prize.DRAW_AGAIN -> getString(R.string.spin_draw_again)
            Prize.TRY_AGAIN -> getString(R.string.spin_try_again)
        }
        if (isFinishing || isDestroyed) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.lucky_spin)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}