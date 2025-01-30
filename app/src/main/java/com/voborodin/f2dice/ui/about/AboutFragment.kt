package com.voborodin.f2dice.ui.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import com.voborodin.f2dice.R
import com.voborodin.f2dice.databinding.FragmentAboutBinding
import com.voborodin.f2dice.viewModel.F2DiceViewModel
import kotlin.math.abs
import java.math.BigInteger
import java.security.MessageDigest

fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

interface OnSwipeListener {
    fun swipeRight()
    fun swipeTop()
    fun swipeBottom()
    fun swipeLeft()
}

@AndroidEntryPoint
class AboutFragment : Fragment(), OnSwipeListener {

    private lateinit var binding: FragmentAboutBinding
    private val viewModel: F2DiceViewModel by activityViewModels()
    private var onSwipeTouchListener: OnSwipeTouchListener? = null

    private val counters: MutableMap<String, Int> =
        mutableMapOf("up" to 0, "left" to 0, "down" to 0, "right" to 0)

    private fun buildPin(): String {
        return "${counters["up"]}${counters["left"]}${counters["down"]}${counters["right"]}"
    }

    private fun clearCounters() {
        counters["up"] = 0
        counters["left"] = 0
        counters["down"] = 0
        counters["right"] = 0
    }

    private fun changeCounter(counterName: String) {
        if (counters.containsKey(counterName)) {
            var pin = buildPin()

            if (pin === "6666") {
                Toast.makeText(
                    this.requireActivity(),
                    R.string.new_try_to_enter_pin,
                    Toast.LENGTH_SHORT
                ).show()

                clearCounters()
                return
            }

            if (counters[counterName]!! < 6) {
                counters[counterName] = counters.getOrDefault(counterName, 0) + 1
            }

            pin = buildPin()

            if (md5(pin) == "b59c67bf196a4758191e42f76670ceba") {
                clearCounters()

                findNavController().navigate(R.id.action_aboutFragment_to_secretFragment)
            }
        }
    }

    override fun swipeTop() {
        changeCounter("up")
    }

    override fun swipeLeft() {
        changeCounter("left")
    }

    override fun swipeBottom() {
        changeCounter("down")
    }

    override fun swipeRight() {
        changeCounter("right")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)
        binding.rateAppCV.setOnClickListener {
            openWebURI(viewModel.appURI)
        }
        binding.bugCV.setOnClickListener {
            openWebURI(viewModel.issuesURI)
        }
        binding.devCV.setOnClickListener {
            openWebURI(viewModel.developerURI)
        }
        binding.supportAppCV.setOnClickListener {
            Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        onSwipeTouchListener =
            OnSwipeTouchListener(this.requireActivity(), binding.aboutNestedScrollView, this)

        return binding.root
    }

    private fun openWebURI(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        startActivity(intent)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    class OnSwipeTouchListener internal constructor(
        ctx: FragmentActivity, mainView: View, onSwipeInitializer: OnSwipeListener
    ) : View.OnTouchListener {
        private val gestureDetector: GestureDetector
        private var context: Context
        private var onSwipe: OnSwipeListener = onSwipeInitializer

        init {
            gestureDetector = GestureDetector(ctx, GestureListener())
            mainView.setOnTouchListener(this)
            context = ctx
        }

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            v.performClick()
            return gestureDetector.onTouchEvent(event)
        }

        private companion object {
            private const val SWIPE_THRESHOLD = 100
            private const val SWIPE_VELOCITY_THRESHOLD = 100
        }

        inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                var result = false
                try {
                    if (e1 != null) {
                        val diffY = e2.y - e1.y
                        val diffX = e2.x - e1.x
                        if (abs(diffX) > abs(diffY)) {
                            if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                                if (diffX > 0) {
                                    onSwipeRight()
                                } else {
                                    onSwipeLeft()
                                }
                                result = true
                            }
                        } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeBottom()
                            } else {
                                onSwipeTop()
                            }
                            result = true
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return result
            }
        }

        internal fun onSwipeRight() {
            this.onSwipe.swipeRight()
        }

        internal fun onSwipeLeft() {
            this.onSwipe.swipeLeft()
        }

        internal fun onSwipeTop() {
            this.onSwipe.swipeTop()
        }

        internal fun onSwipeBottom() {
            this.onSwipe.swipeBottom()
        }
    }
}