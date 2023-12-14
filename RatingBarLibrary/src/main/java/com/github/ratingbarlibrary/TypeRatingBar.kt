package com.github.ratingbarlibrary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


class TypeRatingBar(context: Context, attrs: AttributeSet) : LinearLayout(context,attrs) {

    private val DF_TOTAL_TICKS = 5
    private val DF_DEFAULT_TICKS = 3
    private val DF_SIZE=R.dimen.trb_drawable_tick_default_star_size
    private val DF_STAR_TO_TEXT=R.dimen.trb_drawable_tick_default_star_to_text_view_size
    private val DF_CLICKABLE = true
    private val DF_SYMBOLIC_TICK_RES: Int = R.string.trb_default_symbolic_string
    private val DF_SYMBOLIC_TEXT_SIZE_RES: Int = R.dimen.trb_symbolic_tick_default_text_size
    private val DF_SYMBOLIC_TEXT_STYLE = Typeface.NORMAL
    private val DF_SYMBOLIC_TEXT_NORMAL_COLOR = Color.BLACK
    private val DF_SYMBOLIC_TEXT_SELECTED_COLOR = Color.GRAY
    private val DF_TICK_SPACING_RES: Int = R.dimen.trb_drawable_tick_default_spacing
    private val DF_RATING_BAR_TYPE=0

    private var totalTicks = 0
    private var lastSelectedTickIndex = 0
    private var clickable = false
    private var symbolicTick: String? = null
    private var customTextSize = 0
    private var customTextStyle = 0
    private var customTextNormalColor = 0
    private var customTextSelectedColor = 0
    private var tickNormalDrawable: Drawable? = null
    private var tickSelectedDrawable: Drawable? = null
    private var tickSpacing = 0
    private var ratingBarType= 0

    private var useSymbolicTick = false
    private var rating = 0
    private var widthHeightSize=0
    private var starToTextViewSize=0
    private var listener: RatingListener? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TypeRatingBar)
        //
        totalTicks = a.getInt(R.styleable.TypeRatingBar_trb_totalTicks, DF_TOTAL_TICKS)
        rating = a.getInt(R.styleable.TypeRatingBar_trb_defaultRating, DF_DEFAULT_TICKS)
        ratingBarType = a.getInt(R.styleable.TypeRatingBar_trb_type, DF_RATING_BAR_TYPE)
        //
        clickable = a.getBoolean(R.styleable.TypeRatingBar_trb_clickable, DF_CLICKABLE)
        //
        symbolicTick = a.getString(R.styleable.TypeRatingBar_trb_symbolicTick)
        if (symbolicTick == null) symbolicTick = context.getString(DF_SYMBOLIC_TICK_RES)
        //
        customTextSize = a.getDimensionPixelSize(
            R.styleable.TypeRatingBar_android_textSize,
            context.resources.getDimensionPixelOffset(DF_SYMBOLIC_TEXT_SIZE_RES)
        )
        customTextStyle =
            a.getInt(R.styleable.TypeRatingBar_android_textStyle, DF_SYMBOLIC_TEXT_STYLE)
        customTextNormalColor = a.getColor(
            R.styleable.TypeRatingBar_trb_symbolicTickNormalColor,
            DF_SYMBOLIC_TEXT_NORMAL_COLOR
        )
        customTextSelectedColor = a.getColor(
            R.styleable.TypeRatingBar_trb_symbolicTickSelectedColor,
            DF_SYMBOLIC_TEXT_SELECTED_COLOR
        )
        //
        tickNormalDrawable = a.getDrawable(R.styleable.TypeRatingBar_trb_tickNormalDrawable)
        tickSelectedDrawable = a.getDrawable(R.styleable.TypeRatingBar_trb_tickSelectedDrawable)
        tickSpacing = a.getDimensionPixelOffset(
            R.styleable.TypeRatingBar_trb_tickSpacing,
            context.resources.getDimensionPixelOffset(DF_TICK_SPACING_RES)
        )

        starToTextViewSize = a.getDimensionPixelOffset(
            R.styleable.TypeRatingBar_trb_starToTextSize,
            context.resources.getDimensionPixelOffset(DF_STAR_TO_TEXT)
        )

        widthHeightSize = a.getDimensionPixelOffset(
            R.styleable.TypeRatingBar_trb_totalSize,
            context.resources.getDimensionPixelOffset(DF_SIZE)
        )

        afterInit()

        a.recycle()
    }

    private fun afterInit() {
        if (rating > totalTicks) rating = totalTicks
        lastSelectedTickIndex = rating - 1
        addTicks(this.context)
    }

    private fun addTicks(context: Context) {
        removeAllViews()
        this.gravity=Gravity.CENTER
        for (i in 0 until totalTicks) {
            addTick(context, i)
        }
        redrawTicks()
    }

    private fun addTick(context: Context, position: Int) {
//        if (useSymbolicTick) {
//            addSymbolicTick(context, position)
//        } else {
//            addDrawableTick(context, position)
//        }
        when(ratingBarType){
            0 -> {
                addSymbolicTick(context, position)
            }

            1 -> {
                addDrawableTick(context, position)
            }
        }
    }

    private fun addSymbolicTick(context: Context, position: Int) {
        val tv = TextView(context)
        tv.text = symbolicTick
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, customTextSize.toFloat())
        if (customTextStyle != 0) {
            tv.setTypeface(Typeface.DEFAULT, customTextStyle)
        }
        updateTicksClickParameters(tv, position)
        this.addView(tv)
    }

    @SuppressLint("SetTextI18n")
    private fun addDrawableTick(context: Context, position: Int) {
        val vertialLinearLayout = LinearLayout(context)
        val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0,0,tickSpacing,0)
        vertialLinearLayout.layoutParams= params
        vertialLinearLayout.gravity=Gravity.CENTER
        vertialLinearLayout.orientation=VERTICAL
        val iv = ImageView(context)
        iv.layoutParams= LayoutParams(widthHeightSize,widthHeightSize)
        iv.layoutParams= LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        updateTicksClickParameters(iv, position)
        vertialLinearLayout.addView(iv)
        val textView = TextView(context)
        val textParams =LayoutParams(widthHeightSize,ViewGroup.LayoutParams.WRAP_CONTENT)
        textParams.setMargins(0,starToTextViewSize,0,0)
        textView.layoutParams= textParams
        textView.gravity=Gravity.CENTER

        textView.text = (position+1).toString()
        textView.setTag(R.id.trb_tick_text_tag_id,position+1)
        vertialLinearLayout.addView(textView)
        this.addView(vertialLinearLayout)
    }

    private fun updateTicksClickParameters(tick: View, position: Int) {
        Log.e("TAG", "updateTicksClickParameters: $clickable and $position", )
        if (clickable) {
            tick.setTag(R.id.trb_tick_tag_id, position)
            tick.setOnClickListener{
                lastSelectedTickIndex = it.getTag(R.id.trb_tick_tag_id) as Int
                rating = lastSelectedTickIndex + 1
                Log.e("TAG", ": $lastSelectedTickIndex and $rating", )
                redrawTicks()
                listener?.onRatePicked(this@TypeRatingBar)
            }
        } else {
            tick.setOnClickListener(null)
        }
    }

    private val mTickClickedListener =
        OnClickListener { v ->
            lastSelectedTickIndex = v.getTag(R.id.trb_tick_tag_id) as Int
            rating = lastSelectedTickIndex + 1
            Log.e("TAG", ": $lastSelectedTickIndex and $rating", )
            redrawTicks()
            listener?.onRatePicked(this@TypeRatingBar)
        }

    private fun redrawTicks() {
        iterateTicks(object : TicksIterator{
            override fun onTick(tick: View?, position: Int) {
                if (useSymbolicTick) {
                    redrawTickSelection(
                        tick as TextView,
                        position <= lastSelectedTickIndex
                    )
                } else {
                    redrawTickSelection(
                        tick as ImageView,
                        position <= lastSelectedTickIndex
                    )
                }
            }

        })

    }

    private fun redrawTickSelection(tick: ImageView, isSelected: Boolean) {
        if (isSelected) {
            tick.setImageDrawable(tickSelectedDrawable)
        } else {
            tick.setImageDrawable(tickNormalDrawable)
        }
    }

    private fun redrawTickSelection(tick: TextView, isSelected: Boolean=false) {
        if (isSelected) {
            tick.setTextColor(customTextSelectedColor)
        } else {
            tick.setTextColor(customTextNormalColor)
        }
    }

    private fun iterateTicks(iterator: TicksIterator?) {
        requireNotNull(iterator) { "Iterator can't be null!" }
        for (i in 0 until childCount) {
            val checkView=getChildAt(i)
            if(checkView is LinearLayout){
                for (j in 0 until checkView.childCount) {
                    val checkViews2 = checkView.getChildAt(j)
                    if (checkViews2 is ImageView) {
                        iterator.onTick(checkViews2, i)
                    }
                }
            }
        }
    }

    private interface TicksIterator {
        fun onTick(tick: View?, position: Int)
    }


    /* override fun onSaveInstanceState(): Parcelable? {
         val savedState = SavedState(super.onSaveInstanceState())
         savedState.rating = rating
         savedState.clickable = clickable
         return savedState
     }

     override fun onRestoreInstanceState(state: Parcelable) {
         if (state !is SavedState) {
             super.onRestoreInstanceState(state)
             return
         }
         val savedState: SavedState = state as SavedState
         super.onRestoreInstanceState(savedState.getSuperState())
         setRating(savedState.rating)
     }

     internal class SavedState : BaseSavedState {
         var rating = 0
         var clickable = false

         constructor(superState: Parcelable?) : super(superState)
         private constructor(`in`: Parcel) : super(`in`) {
             rating = `in`.readInt()
             clickable = `in`.readByte().toInt() == 1
         }

         override fun writeToParcel(out: Parcel, flags: Int) {
             super.writeToParcel(out, flags)
             out.writeInt(rating)
             out.writeByte((if (clickable) 1 else 0).toByte())
         }

         companion object {
             @JvmField
             val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
                 override fun createFromParcel(`in`: Parcel): SavedState {
                     return SavedState(`in`)
                 }

                 override fun newArray(size: Int): Array<SavedState?> {
                     return arrayOfNulls(size)
                 }
             }
         }
     }*/

    override fun isClickable(): Boolean {
        return clickable
    }

    /**
     * Nifty sugar method to just toggle clickable to opposite state.
     */
    fun toggleClickable() {
        isClickable = !clickable
    }

    override fun setClickable(clickable: Boolean) {
        this.clickable = clickable
        iterateTicks(object : TicksIterator {
            override fun onTick(tick: View?, position: Int) {
                updateTicksClickParameters(tick!!, position)
            }
        })
    }

    /**
     * Get the attached [RatingListener]
     * @return listener or null if none was set
     */
    fun getListener(): RatingListener? {
        return listener
    }

    /**
     * Set the [RatingListener] to be called when user taps rating bar's ticks
     * @param listener listener to set
     *
     * @throws IllegalArgumentException if listener is **null**
     */
    fun setListener(listener: RatingListener?) {
        requireNotNull(listener) { "listener cannot be null!" }
        this.listener = listener
    }

    /**
     * Remove listener
     */
    fun removeRatingListener() {
        listener = null
    }

    /**
     * Get the current rating shown
     * @return rating
     */
    fun getRating(): Int {
        return rating
    }

    /**
     * Set the rating to show
     * @param rating new rating value
     */
    fun setRating(rating: Int) {
        var rating = rating
        if (rating > totalTicks) rating = totalTicks
        this.rating = rating
        lastSelectedTickIndex = rating - 1
        redrawTicks()
    }

    fun setSymbolicTick(tick: String?) {
        symbolicTick = tick
        afterInit()
    }

    fun getSymbolicTick(): String? {
        return symbolicTick
    }

}