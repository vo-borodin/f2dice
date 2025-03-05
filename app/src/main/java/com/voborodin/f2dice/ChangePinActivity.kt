package com.voborodin.f2dice

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.voborodin.f2dice.databinding.ActivityChangePinBinding
import com.voborodin.f2dice.utils.md5
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePinBinding

    private val currentPinHash: MutableLiveData<String> = MutableLiveData<String>("")
    private val newPinHash: MutableLiveData<String> = MutableLiveData<String>("")
    private val confirmationHash: MutableLiveData<String> = MutableLiveData<String>("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_pin)

        binding.currentPinField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentPinHash.value = md5((s ?: "").toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.newPinField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                newPinHash.value = md5((s ?: "").toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.confirmationField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                confirmationHash.value = md5((s ?: "").toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.saveNewPinButton.setOnClickListener {
            if (currentPinHash.value?.isNotBlank() == true) {
                if (currentPinHash.value == intent.getStringExtra("pin_hash")) {
                    if (newPinHash.value?.isNotBlank() == true) {
                        if (newPinHash.value != currentPinHash.value) {
                            if (confirmationHash.value?.isNotBlank() == true) {
                                if (newPinHash.value == confirmationHash.value) {
                                    intent.putExtra("pin_hash", newPinHash.value)
                                    setResult(RESULT_OK, intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@ChangePinActivity,
                                        R.string.confirmation_is_failed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@ChangePinActivity,
                                    R.string.enter_confirmation,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@ChangePinActivity,
                                R.string.pin_is_not_changed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@ChangePinActivity,
                            R.string.enter_new_pin,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ChangePinActivity,
                        R.string.incorrect_current_pin,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@ChangePinActivity,
                    R.string.enter_current_pin,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.cancelChangePinButton.setOnClickListener {
            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }
}