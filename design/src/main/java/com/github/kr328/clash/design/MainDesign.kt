package com.github.kr328.clash.design

import android.content.Context
import android.graphics.Color
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

            val selected = context.resolveThemedColor(com.google.android.material.R.attr.colorPrimary)
            val deselected = Color.TRANSPARENT
            binding.modeDirect.setBackgroundColor(if (mode == TunnelState.Mode.Direct) selected else deselected)
            binding.modeRule.setBackgroundColor(if (mode == TunnelState.Mode.Rule) selected else deselected)
            binding.modeGlobal.setBackgroundColor(if (mode == TunnelState.Mode.Global) selected else deselected)
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

        binding.modeDirect.setOnClickListener { request(Request.SwitchMode(TunnelState.Mode.Direct)) }
        binding.modeRule.setOnClickListener { request(Request.SwitchMode(TunnelState.Mode.Rule)) }
        binding.modeGlobal.setOnClickListener { request(Request.SwitchMode(TunnelState.Mode.Global)) }
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