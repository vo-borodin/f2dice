package com.voborodin.f2dice

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voborodin.f2dice.types.BTDevice
import com.voborodin.f2dice.types.BTUiState
import com.voborodin.f2dice.utils.dumpBTDevice
import com.voborodin.f2dice.utils.parseBTDevice
import com.voborodin.f2dice.viewModel.F2DiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DevicesActivity : ComponentActivity() {

    private var selectedDevice: BTDevice? = null

    @Composable
    fun DeviceScreen(
        state: BTUiState,
        onStartScan: () -> Unit,
        onStopScan: () -> Unit,
        onDeviceClick: (BTDevice) -> Unit,
        onStartServer: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            BTDeviceList(
                pairedDevices = state.pairedDevices,
                scannedDevices = state.scannedDevices,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onDeviceClick = onDeviceClick
            )
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = onStartScan) {
                    Text(text = "Start scan")
                }
                Button(onClick = onStopScan) {
                    Text(text = "Stop scan")
                }
                Button(onClick = onStartServer) {
                    Text(text = "Start server")
                }
            }
        }
    }

    @Composable
    private fun DeviceListItem(
        index: Int, device: BTDevice, onDeviceClick: (BTDevice) -> Unit
    ) {

        var columnModifier = Modifier.clickable {
            onDeviceClick(device)
            intent.putExtra(
                "selected_device", dumpBTDevice(device)
            )
            setResult(RESULT_OK, intent)
            finish()
        }
        if (selectedDevice != null && selectedDevice?.address == device.address) {
            columnModifier = columnModifier.background(color = Color.Green)
        }
        Column(modifier = columnModifier) {
            if (index != 0) {
                HorizontalDivider()
            }

            Text(
                text = device.address,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 4.dp),
                color = Color.Magenta,
                fontSize = TextUnit(8.0F, TextUnitType.Sp)
            )
            Text(
                text = device.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp, 16.dp, 8.dp),
                color = Color.Unspecified,
                fontSize = TextUnit(16.0F, TextUnitType.Sp)
            )
        }
    }

    @Composable
    private fun BTDeviceList(
        pairedDevices: List<BTDevice>,
        scannedDevices: List<BTDevice>,
        modifier: Modifier,
        onDeviceClick: (BTDevice) -> Unit
    ) {

        LazyColumn(modifier = modifier) {
            item {
                Text(
                    text = "Paired Devices",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            itemsIndexed(pairedDevices) { index: Int, item: BTDevice ->
                DeviceListItem(index, item, onDeviceClick)
            }

            item {
                Text(
                    text = "Scanned Devices",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            itemsIndexed(scannedDevices) { index: Int, item: BTDevice ->
                DeviceListItem(index, item, onDeviceClick)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedDevice = intent.getStringExtra("selected_device")?.let { parseBTDevice(it) }

        setContent {
            val viewModel = hiltViewModel<F2DiceViewModel>()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(key1 = state.errorMsg) {
                state.errorMsg?.let {
                    Toast.makeText(this@DevicesActivity, it, Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(key1 = state.isConnected) {
                if (state.isConnected) {
                    Toast.makeText(
                        this@DevicesActivity.applicationContext,
                        "You're connected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
            ) {
                /*when {
                    state.isConnecting -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text(text = "Connecting...")
                        }
                    }

                    state.isConnected -> {
                        ChatScreen(
                            state = state,
                            onDisconnect = viewModel::disconnectFromDevice,
                            onSendMsg = viewModel::sendMsg
                        )
                    }

                    else -> */DeviceScreen(
                state = state,
                onStartScan = viewModel::startScan,
                onStopScan = viewModel::stopScan,
                onDeviceClick = viewModel::connectToDevice,
                onStartServer = viewModel::waitForIncomingConnection
            )
                //}
            }
        }
    }
}