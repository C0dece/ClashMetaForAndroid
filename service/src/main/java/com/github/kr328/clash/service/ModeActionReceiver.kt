package com.github.kr328.clash.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.kr328.clash.common.constants.Intents
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.core.model.TunnelState
import com.github.kr328.clash.service.util.sendOverrideChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModeActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intents.ACTION_SWITCH_MODE) return
        val modeName = intent.getStringExtra(Intents.EXTRA_MODE) ?: return
        val mode = try {
            TunnelState.Mode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            return
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val override = Clash.queryOverride(Clash.OverrideSlot.Session)
                override.mode = mode
                Clash.patchOverride(Clash.OverrideSlot.Session, override)
                context.sendOverrideChanged()
            } finally {
                pending.finish()
            }
        }
    }
}
