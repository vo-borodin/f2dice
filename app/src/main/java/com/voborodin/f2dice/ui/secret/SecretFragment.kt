package com.voborodin.f2dice.ui.secret

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.voborodin.f2dice.ChangePinActivity
import com.voborodin.f2dice.DevicesActivity
import com.voborodin.f2dice.R
import com.voborodin.f2dice.databinding.FragmentSecretBinding
import com.voborodin.f2dice.types.BTDevice
import com.voborodin.f2dice.types.Role
import com.voborodin.f2dice.utils.dumpBTDevice
import com.voborodin.f2dice.utils.parseBTDevice
import com.voborodin.f2dice.viewModel.F2DiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecretFragment : Fragment() {
    private lateinit var binding: FragmentSecretBinding
    private val viewModel: F2DiceViewModel by activityViewModels()

    private val btManager by lazy {
        this.requireActivity().getSystemService(BluetoothManager::class.java)
    }
    private val btAdapter by lazy {
        btManager?.adapter
    }

    private val isBTEnabled: Boolean
        get() = btAdapter?.isEnabled == true

    private val enableBluetoothDevice = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        //
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        var canEnableBluetooth = true

        requiredPermissions.forEach {
            if (perms[it] == false) canEnableBluetooth = false
        }

        if (canEnableBluetooth && !isBTEnabled) {
            enableBluetoothDevice.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }
    }

    private val requiredPermissions = ArrayList<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }.toTypedArray()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_secret, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(false)

        val changePinActivityLauncher = registerForActivityResult(ChangePinContracts) { result ->
            if (result != null) {
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
            }
        }

        binding.changePinButton.setOnClickListener {
            changePinActivityLauncher.launch("b59c67bf196a4758191e42f76670ceba")
        }

        if (viewModel.role.value != null) {
            binding.roleRadioGroup.check(
                if (viewModel.role.value == Role.Master) {
                    R.id.master_radio_button
                } else {
                    R.id.slave_radio_button
                }
            )
        } else {
            binding.roleRadioGroup.clearCheck()
        }
        applyRole(viewModel.role.value)
        viewModel.role.observe(viewLifecycleOwner) { value: Role? ->
            applyRole(value)
        }
        applyConnectedDevice(viewModel.connectedDevice.value)
        viewModel.connectedDevice.observe(viewLifecycleOwner) { value: BTDevice? ->
            applyConnectedDevice(value)
        }
        binding.roleRadioGroup.setOnCheckedChangeListener { _: RadioGroup, i: Int ->
            val newRole: Role? = when (i) {
                R.id.master_radio_button -> {
                    Role.Master
                }

                R.id.slave_radio_button -> {
                    Role.Slave
                }

                else -> {
                    null
                }
            }
            viewModel.setRole(newRole)
        }

        binding.startReceivingButton.setOnClickListener {
            viewModel.waitForIncomingConnection()
        }

        val devicesActivityLauncher = registerForActivityResult(DevicesContracts) { result ->
            if (result != null) {
                parseBTDevice(result)?.let { viewModel.setConnectedDevice(it) }
            }
        }

        binding.startSendingButton.setOnClickListener {
            if (viewModel.role.value != null) {
                if (viewModel.connectedDevice.value != null) {
                    viewModel.connectToDevice(viewModel.connectedDevice.value!!)
                } else {
                    Toast.makeText(
                        requireContext(), R.string.set_connected_device, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.selectDeviceButton.setOnClickListener {
            devicesActivityLauncher.launch(dumpBTDevice(viewModel.connectedDevice.value))
        }
        binding.clearConnectedDeviceButton.setOnClickListener {
            viewModel.setConnectedDevice(null)
        }

        turnOnBt()
        binding.switchBtBtn.setOnClickListener {
            turnOnBt()
        }

        binding.launchButtonMaster.setOnClickListener { onLaunchMaster() }
        binding.launchButtonSlave.setOnClickListener { onLaunchSlave() }

        return binding.root
    }

    private fun turnOnBt() {
        if (!hasBTPermission()) {
            permissionLauncher.launch(requiredPermissions)
        } else if (!isBTEnabled) {
            enableBluetoothDevice.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }
    }

    private fun applyRole(role: Role?) {
        when (role) {
            Role.Master -> {
                binding.roleMasterLayout.visibility = VISIBLE
                binding.roleSlaveLayout.visibility = GONE
            }

            Role.Slave -> {
                binding.roleMasterLayout.visibility = GONE
                binding.roleSlaveLayout.visibility = VISIBLE
            }

            else -> {
                binding.roleMasterLayout.visibility = GONE
                binding.roleSlaveLayout.visibility = GONE
            }
        }
    }

    private fun applyConnectedDevice(device: BTDevice?) {
        if (device != null) {
            viewModel.connectToDevice(device)
        } else {
            viewModel.disconnectFromDevice()
        }
        binding.clearConnectedDeviceButton.visibility =
            if (device != null && device.address.isNotBlank()) VISIBLE
            else INVISIBLE
        binding.slaveDevice.setText(
            if (device != null && !device.name.isNullOrBlank()) device.name
            else null
        )
    }

    private fun hasBTPermission(): Boolean {
        var hasPermission = true
        requiredPermissions.forEach {
            if (ContextCompat.checkSelfPermission(
                    this.requireActivity(), it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                hasPermission = false
            }
        }
        return hasPermission
    }

    private fun onLaunchMaster() {
        Navigation.findNavController(
            requireActivity(), R.id.nav_host_fragment_container
        ).navigate(R.id.action_secretFragment_to_trinityFragment)
    }

    private fun onLaunchSlave() {
        Navigation.findNavController(
            requireActivity(), R.id.nav_host_fragment_container
        ).navigate(R.id.action_secretFragment_to_boardTwoFragment)
    }

    object DevicesContracts : ActivityResultContract<String, String?>() {
        override fun createIntent(context: Context, input: String): Intent {
            val intent = Intent(context, DevicesActivity::class.java)
            intent.putExtra("selected_device", input)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            if (resultCode == RESULT_OK) {
                return intent?.getStringExtra("selected_device")
            }
            return null
        }
    }

    object ChangePinContracts : ActivityResultContract<String, String?>() {
        override fun createIntent(context: Context, input: String): Intent {
            val intent = Intent(context, ChangePinActivity::class.java)
            intent.putExtra("pin_hash", input)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            if (resultCode == RESULT_OK) {
                return intent?.getStringExtra("pin_hash")
            }
            return null
        }
    }
}
