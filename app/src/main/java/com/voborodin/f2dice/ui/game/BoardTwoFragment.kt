package com.voborodin.f2dice.ui.game

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import com.voborodin.f2dice.R
import com.voborodin.f2dice.databinding.FragmentBoardTwoBinding
import com.voborodin.f2dice.types.Role
import com.voborodin.f2dice.viewModel.F2DiceViewModel

@AndroidEntryPoint
class BoardTwoFragment : Fragment() {

    private lateinit var binding: FragmentBoardTwoBinding
    private val viewModel: F2DiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppCompatActivity).supportActionBar?.show()
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.black)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_board_two, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(true)

        viewModel.resetData()
        viewModel.result.observe(viewLifecycleOwner, Observer { value ->
            if (value != "") {
                animateDice(binding)
                animateDiceResult(binding)
            }
        })

        binding.rollButton.setOnClickListener {
            binding.confetti.visibility = View.VISIBLE
            binding.rollButton.isEnabled = false
            binding.rollButton.isClickable = false

            viewModel.rollBoard()

            binding.rollButton.postDelayed(Runnable {
                binding.rollButton.isEnabled = true
                binding.rollButton.isClickable = true
            }, 2000)
            binding.rollButton.postDelayed(Runnable {
                binding.confetti.visibility = View.INVISIBLE
            }, 2000)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.appTheme.observe(viewLifecycleOwner) {}
        viewModel.role.observe(viewLifecycleOwner) {}
        viewModel.connectedDevice.observe(viewLifecycleOwner) {}

        when (viewModel.appTheme.value) {
            "Light Mode" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark Mode" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "System Default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        when (viewModel.role.value) {
            Role.Master -> {
                val slave = viewModel.connectedDevice.value
                if (slave != null) {
                    viewModel.connectToDevice(slave)
                }
            }

            Role.Slave -> {
                viewModel.waitForIncomingConnection()
            }

            else -> {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aboutFragment -> findNavController().navigate(R.id.action_boardTwoFragment_to_aboutFragment)
            R.id.settings -> findNavController().navigate(R.id.action_boardTwoFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun animateDice(binding: FragmentBoardTwoBinding) {
        binding.dice1ImageView.animate().apply {
            duration = 200
            rotationYBy(360f)
        }.withEndAction {
            binding.dice1ImageView.animate().apply {
                duration = 100
                rotationYBy(3600f)
            }
        }
        binding.dice2ImageView.animate().apply {
            duration = 200
            rotationYBy(360f)
        }.withEndAction {
            binding.dice1ImageView.animate().apply {
                duration = 100
                rotationYBy(3600f)
            }
        }
    }

    private fun animateDiceResult(binding: FragmentBoardTwoBinding?) {
        binding?.resultImageView?.animate()?.apply {
            duration = 200
            rotationYBy(360f)
        }?.withEndAction {
            binding.resultImageView.animate().apply {
                duration = 200
                rotationYBy(3600f)
            }
        }
    }
}