package com.github.kr328.clash.design

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.core.util.trafficTotal
import com.github.kr328.clash.design.databinding.DesignAboutBinding
import com.github.kr328.clash.design.databinding.DesignMainBinding
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.resolveThemedColor
import com.github.kr328.clash.design.util.root
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainDesign(context: Context) : Design<MainDesign.Request>(context) {
    sealed class Request {
        object ToggleStatus : Request()
        object OpenProxy : Request()
        object OpenProfiles : Request()
        object OpenProviders : Request()
        object OpenLogs : Request()
        object OpenSettings : Request()
        object OpenHelp : Request()
        object OpenAbout : Request()
        data class SwitchMode(val mode: TunnelState.Mode) : Request()
    }

    private val binding = DesignMainBinding
        .inflate(context.layoutInflater, context.root, false)

    private var programmaticModeUpdate = false

    override val root: View
        get() = binding.root

    suspend fun setProfileName(name: String?) {
        withContext(Dispatchers.Main) {
            binding.profileName = name
        }
    }

    suspend fun setClashRunning(running: Boolean) {
        withContext(Dispatchers.Main) {
            binding.clashRunning = running
        }
    }

    suspend fun setForwarded(value: Long) {
        withContext(Dispatchers.Main) {
            binding.forwarded = value.trafficTotal()
        }
    }

    suspend fun setMode(mode: TunnelState.Mode) {
        withContext(Dispatchers.Main) {
            binding.mode = when (mode) {
                TunnelState.Mode.Direct -> context.getString(R.string.direct_mode)
                TunnelState.Mode.Global -> context.getString(R.string.global_mode)
                TunnelState.Mode.Rule -> context.getString(R.string.rule_mode)
                else -> context.getString(R.string.rule_mode)
            }

            val checkedId = when (mode) {
                TunnelState.Mode.Direct -> R.id.modeDirect
                TunnelState.Mode.Rule -> R.id.modeRule
                TunnelState.Mode.Global -> R.id.modeGlobal
                else -> R.id.modeRule
            }
            programmaticModeUpdate = true
            binding.modeSwitcher.check(checkedId)
            programmaticModeUpdate = false
        }
    }

    suspend fun setHasProviders(has: Boolean) {
        withContext(Dispatchers.Main) {
            binding.hasProviders = has
        }
    }

    suspend fun showAbout(versionName: String) {
        withContext(Dispatchers.Main) {
            val binding = DesignAboutBinding.inflate(context.layoutInflater).apply {
                this.versionName = versionName
            }

            AlertDialog.Builder(context)
                .setView(binding.root)
                .show()
        }
    }

    init {
        binding.self = this

        binding.colorClashStarted = context.resolveThemedColor(com.google.android.material.R.attr.colorPrimary)
        binding.colorClashStopped = context.resolveThemedColor(R.attr.colorClashStopped)

        binding.modeSwitcher.addOnButtonCheckedListener { _: com.google.android.material.button.MaterialButtonToggleGroup, checkedId: Int, isChecked: Boolean ->
            if (!isChecked || programmaticModeUpdate) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                com.github.kr328.clash.design.R.id.modeDirect -> TunnelState.Mode.Direct
                com.github.kr328.clash.design.R.id.modeRule -> TunnelState.Mode.Rule
                com.github.kr328.clash.design.R.id.modeGlobal -> TunnelState.Mode.Global
                else -> return@addOnButtonCheckedListener
            }
            request(Request.SwitchMode(mode))
        }
    }

    fun request(request: Request) {
        requests.trySend(request)
    }

    fun toggleStatus() {
        request(Request.ToggleStatus)
    }

    fun openProxy() {
        request(Request.OpenProxy)
    }

    fun openProfiles() {
        request(Request.OpenProfiles)
    }

    fun openProviders() {
        request(Request.OpenProviders)
    }

    fun openLogs() {
        request(Request.OpenLogs)
    }

    fun openSettings() {
        request(Request.OpenSettings)
    }

    fun openHelp() {
        request(Request.OpenHelp)
    }

    fun openAbout() {
        request(Request.OpenAbout)
    }
}