package com.voborodin.f2dice.ui.secret

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.voborodin.f2dice.R
import com.voborodin.f2dice.databinding.FragmentSecretBinding
import com.voborodin.f2dice.types.Role
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
        binding.layoutOnEnabledBt.setEnabled(it.resultCode == RESULT_OK)
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
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_secret, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setHasOptionsMenu(false)

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
            viewModel.role.value = newRole
        }
        binding.layoutOnEnabledBt.setEnabled(isBTEnabled)

        turnOnBt()
        binding.switchBtBtn.setOnClickListener {
            turnOnBt()
        }

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
        binding.launchButton.setEnabled(role != null)
        binding.launchButton.setText(
            when (role) {
                Role.Master -> R.string.launch_master_button_caption
                Role.Slave -> R.string.launch_slave_button_caption
                else -> R.string.set_role_of_device
            }
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
}
