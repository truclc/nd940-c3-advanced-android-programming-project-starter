package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ANIMATOR_DURATION = 2000L
    private val PROGRESS_MIN = 0f
    private val PROGRESS_MAX = 720f

    private var widthSize = 0
    private var heightSize = 0

    private val rect = RectF()
    private val circleRect = RectF()
    private var topRect = RectF()
    private var progress: Float = PROGRESS_MAX

    private var buttonText = resources.getString(R.string.button_name)

    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

        when (new) {
            ButtonState.Loading -> {
                isClickable = false
                buttonText = resources.getString(R.string.button_loading)
                valueAnimator.start()
                valueAnimator.addUpdateListener { animation ->
//                    valueAnimator.repeatCount = ValueAnimator.INFINITE
//                    valueAnimator.repeatMode = ValueAnimator.REVERSE
                    val animatedValue = animation.animatedValue as Float
                    progress = animatedValue
                    valueAnimator.disableViewDuringAnimation(this)
                    invalidate()
                }
            }
            ButtonState.Completed -> {
                isClickable = true
                buttonText = resources.getString(R.string.button_downloaded)
                valueAnimator.end()
            }
            ButtonState.Clicked -> {
                isClickable = true
                buttonText = resources.getString(R.string.button_downloaded)
                valueAnimator.end()
            }
        }
    }

    /**
     * init
     */
    init {
        isClickable = true

        valueAnimator = ValueAnimator.ofFloat(PROGRESS_MIN, PROGRESS_MAX)
        valueAnimator.duration = ANIMATOR_DURATION

    }

    private val bgButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.colorPrimary)
    }

    private val paintButtonText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.DEFAULT_BOLD
        color = context.getColor(R.color.white)
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    private val progressBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.colorPrimaryDark)
        style = Paint.Style.FILL
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.colorAccent)

    }

    /**
     * updateButtonState
     */
    fun updateButtonState(state: ButtonState) {
        buttonState = state
    }

    /**
     * onDraw
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        rect.left = 0f
        rect.top = 0f
        rect.right = widthSize.toFloat()
        rect.bottom = heightSize.toFloat()
        topRect = rect
        canvas?.drawRect(rect, bgButtonPaint)

        canvas?.drawText(
            buttonText,
            (widthSize / 2).toFloat(),
            (heightSize / 1.5).toFloat(),
            paintButtonText
        )

        if (progress == PROGRESS_MAX) {

            topRect.right = 0f
        } else {

            circleRect.top = 0f + paintButtonText.descent()
            circleRect.left = rect.right - 100f
            circleRect.right = rect.right - 10f
            circleRect.bottom = (heightSize).toFloat() - paintButtonText.descent()
            canvas?.drawArc(circleRect, PROGRESS_MAX / 2, progress, true, circlePaint)

            topRect.right = widthSize * (progress / PROGRESS_MAX)
        }
        canvas?.drawRect(topRect, progressBarPaint)
    }

    /**
     * performClick
     */
    override fun performClick(): Boolean {
        if (super.performClick()) return true
        postInvalidate()
        return true
    }

    /**\
     * onMeasure
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    /**
     * disableViewDuringAnimation
     */
    fun ValueAnimator.disableViewDuringAnimation(view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.isEnabled = true
            }
        })
    }
}