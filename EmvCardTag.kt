package com.freakyaxel.emvreader

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.freakyaxel.emvparser.api.CardTag

class EmvCardTag private constructor(tag: Tag) : CardTag {

    private val isoDep: IsoDep by lazy { IsoDep.get(tag) }

    override fun transceive(command: ByteArray): ByteArray = isoDep.transceive(command)
    override fun connect() = isoDep.connect()
    override fun disconnect() = isoDep.close()

    override val connected: Boolean
        get() = isoDep.isConnected

    companion object {
        fun get(tag: Tag) = EmvCardTag(tag)
    }
}