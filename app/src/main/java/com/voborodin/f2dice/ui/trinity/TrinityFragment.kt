package com.voborodin.f2dice.ui.trinity

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.voborodin.f2dice.R
import com.voborodin.f2dice.databinding.FragmentTrinityBinding
import com.voborodin.f2dice.viewModel.F2DiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrinityFragment : Fragment() {
    private lateinit var binding: FragmentTrinityBinding
    private val viewModel: F2DiceViewModel by activityViewModels()

    private val waitForSending: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trinity, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        applyForgery(viewModel.forgery.value)
        viewModel.forgery.observe(viewLifecycleOwner) { value: Int? ->
            applyForgery(value)
        }
        waitForSending.observe(viewLifecycleOwner) {
            applyForgery(viewModel.forgery.value)
        }

        binding.incrementButton.setOnClickListener { onIncrement() }
        binding.commitButton.setOnClickListener { onCommit() }
        binding.decrementButton.setOnClickListener { onDecrement() }

        return binding.root
    }

    private fun applyForgery(value: Int?) {
        if (value == null) {
            binding.commitButton.setText(R.string.commit_button_caption)
        } else {
            val res: Resources = resources
            val path = if (waitForSending.value!!) R.string.commit_button_caption else R.string.commit_button_sent
            binding.commitButton.text = "${res.getString(path)} $value"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.forgery.value == null) {
            viewModel.forgery.value = 7
        }
    }

    private fun onIncrement() {
        viewModel.provideHapticFeedback()
        if (!waitForSending.value!!) {
            waitForSending.value = true
        }
        if (viewModel.forgery.value!! < 12) {
            viewModel.forgery.value = viewModel.forgery.value!! + 1
        }
    }

    private fun onCommit() {
        viewModel.provideHapticFeedback()
        if (waitForSending.value!!) {
            waitForSending.value = false
        }
        viewModel.sendForgery()
    }

    private fun onDecrement() {
        viewModel.provideHapticFeedback()
        if (!waitForSending.value!!) {
            waitForSending.value = true
        }
        if (viewModel.forgery.value!! > 2) {
            viewModel.forgery.value = viewModel.forgery.value!! - 1
        }
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000
    }
}