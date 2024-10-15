package com.gmail.sanyamsoni226.memestation

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout

class LogoAnimator(private val context: Context) {

    // Convert DP to Pixels
    private fun convertDpToPx(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    // Function to animate the logo
    @SuppressLint("ObjectAnimatorBinding")
    fun animateLogo(logo: View, loadMemes: () -> Unit, setupInfiniteScroll: () -> Unit) {
        // Get initial width and height of the logo
        logo.post {
            val originalWidth = logo.width
            val originalHeight = logo.height

            // Get screen width and height for center calculation
            val screenWidth = context.resources.displayMetrics.widthPixels
            val screenHeight = context.resources.displayMetrics.heightPixels

            // Calculate the starting position (off-screen left) and center position
            val startFromLeftX = -(originalWidth).toFloat() // Off-screen to the left
            val centerX = (screenWidth / 2 - originalWidth / 2).toFloat() // Center of the screen
            val centerY = (screenHeight / 2 - originalHeight / 2).toFloat() // Vertically center

            // Set initial translation to start from the left
            logo.translationX = startFromLeftX
            logo.translationY = centerY

            // Step 1: Move logo from left (off-screen) to center
            val moveToCenterX = ObjectAnimator.ofFloat(
                logo, "translationX", startFromLeftX, centerX
            ).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
            }

            // Step 2: Move logo from center to top-left corner while resizing
            val moveToTopLeftX = ObjectAnimator.ofFloat(
                logo, "translationX", centerX, 0f
            ).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
            }

            val moveToTopLeftY = ObjectAnimator.ofFloat(
                logo, "translationY", centerY, 0f
            ).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
            }

            // Step 3: Animate width and height to custom sizes during the transition
            val resizeWidth = ObjectAnimator.ofInt(logo, "layoutParams.width", originalWidth, convertDpToPx(140))
            val resizeHeight = ObjectAnimator.ofInt(logo, "layoutParams.height", originalHeight, convertDpToPx(50))

            resizeWidth.addUpdateListener { logo.requestLayout() }
            resizeHeight.addUpdateListener { logo.requestLayout() }

            // Animate margin changes (top and start) during transition
            val layoutParams = logo.layoutParams as ConstraintLayout.LayoutParams

            // Initial and final margins (top and start)
            val initialTopMargin = layoutParams.topMargin
            val initialStartMargin = layoutParams.marginStart
            val finalTopMargin = convertDpToPx(20) // e.g., top margin of 20dp
            val finalStartMargin = convertDpToPx(10) // e.g., start margin of 10dp

            val marginAnimatorTop = ValueAnimator.ofInt(initialTopMargin, finalTopMargin).apply {
                duration = 1000
                addUpdateListener { animation ->
                    val value = animation.animatedValue as Int
                    layoutParams.topMargin = value
                    logo.layoutParams = layoutParams
                    logo.requestLayout() // Ensure the layout is updated during the animation
                }
            }

            val marginAnimatorStart = ValueAnimator.ofInt(initialStartMargin, finalStartMargin).apply {
                duration = 1000
                addUpdateListener { animation ->
                    val value = animation.animatedValue as Int
                    layoutParams.marginStart = value
                    logo.layoutParams = layoutParams
                    logo.requestLayout() // Ensure the layout is updated during the animation
                }
            }

            // Step 4: Combine animations using AnimatorSet
            val animatorSet = AnimatorSet()
            animatorSet.playSequentially(
                moveToCenterX, // First, move from left to center
                AnimatorSet().apply { // Then, move from center to top-left with resizing
                    playTogether(
                        moveToTopLeftX, moveToTopLeftY, resizeWidth, resizeHeight, marginAnimatorTop, marginAnimatorStart
                    )
                }
            )

            // Step 5: Start animation
            animatorSet.start()

            // Step 6: Listener for post-animation layout adjustments
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    // Load memes after animation
                    loadMemes()
                    setupInfiniteScroll()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }
}


