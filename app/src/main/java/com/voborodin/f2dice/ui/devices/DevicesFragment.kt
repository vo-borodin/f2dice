package com.voborodin.f2dice.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnLifecycleDestroyed
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.voborodin.f2dice.R
import com.voborodin.f2dice.databinding.FragmentDevicesBinding
import com.voborodin.f2dice.types.BTDevice
import com.voborodin.f2dice.viewModel.F2DiceViewModel

class DevicesFragment : Fragment() {

    private var _binding: FragmentDevicesBinding? = null
    val viewModel: F2DiceViewModel by activityViewModels()

    private val binding get() = _binding!!

    @Composable
    fun DeviceScreen(
        section: String, onDeviceClick: (BTDevice) -> Unit
    ) {
        val state by viewModel.state.collectAsState()

        val devices = if (section == "paired") state.pairedDevices else state.scannedDevices

        BTDeviceList(
            devices = devices, modifier = Modifier.fillMaxSize(), onDeviceClick = onDeviceClick
        )
    }

    @Composable
    fun BTDeviceList(
        devices: List<BTDevice>, modifier: Modifier, onDeviceClick: (BTDevice) -> Unit
    ) {
        Column(modifier = modifier) {
            for(d in devices) {
                Column(modifier = Modifier.clickable {
                    onDeviceClick(d)

                    viewModel.connectToDevice(d)

                    Navigation.findNavController(
                        requireActivity(),
                        R.id.nav_host_fragment_container
                    ).navigate(R.id.action_devicesFragment_to_secretFragment)
                }) {
                    Text(
                        text = d.address, modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp, 16.dp, 4.dp),
                        color = Color.Magenta,
                        fontSize = TextUnit(8.0F, TextUnitType.Sp)
                    )
                    Text(
                        text = d.name ?: "(No name)", modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 4.dp, 16.dp, 8.dp),
                        color = Color.Unspecified,
                        fontSize = TextUnit(16.0F, TextUnitType.Sp)
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)

        binding.pairedDevicesList.apply {
            setViewCompositionStrategy(DisposeOnLifecycleDestroyed(viewLifecycleOwner))
            setContent {
                DeviceScreen(section = "paired") { device: BTDevice ->
                    Toast.makeText(
                        requireContext(),
                        "Paired device '${device.name}' connected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.scannedDevicesList.apply {
            setViewCompositionStrategy(DisposeOnLifecycleDestroyed(viewLifecycleOwner))
            setContent {
                DeviceScreen(section = "scanned") { device: BTDevice ->
                    Toast.makeText(
                        requireContext(),
                        "Scanned device '${device.name}' connected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.cancelButton.setOnClickListener {
            Navigation.findNavController(
                requireActivity(),
                R.id.nav_host_fragment_container
            ).navigate(R.id.action_devicesFragment_to_secretFragment)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}