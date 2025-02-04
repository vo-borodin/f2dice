package com.voborodin.f2dice.ui.devices

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import com.voborodin.f2dice.R
import com.voborodin.f2dice.databinding.FragmentDevicesBinding
import com.voborodin.f2dice.databinding.FragmentDevicesItemBinding
import com.voborodin.f2dice.types.BTDevice
import com.voborodin.f2dice.types.BTUiState
import com.voborodin.f2dice.viewModel.F2DiceViewModel

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

        items(pairedDevices) {
            Text(
                text = it.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(it) }
                    .padding(16.dp)
            )
        }
        item {
            Text(
                text = "Scanned Devices",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(scannedDevices) {
            Text(
                text = it.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(it) }
                    .padding(16.dp)
            )
        }
    }

}

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    DevicesFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 */
class DevicesFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDevicesBinding? = null
    private val viewModel: F2DiceViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.findViewById<RecyclerView>(R.id.list)?.layoutManager =
            LinearLayoutManager(context)
        activity?.findViewById<RecyclerView>(R.id.list)?.adapter =
            arguments?.getInt(ARG_ITEM_COUNT)?.let { ItemAdapter(it) }
    }

    private inner class ViewHolder(binding: FragmentDevicesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        internal val text: TextView = binding.text
    }

    private inner class ItemAdapter(private val mItemCount: Int) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                FragmentDevicesItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = position.toString()
        }

        override fun getItemCount(): Int {
            return mItemCount
        }
    }

    companion object {

        // TODO: Customize parameters
        fun newInstance(itemCount: Int): DevicesFragment = DevicesFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ITEM_COUNT, itemCount)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}