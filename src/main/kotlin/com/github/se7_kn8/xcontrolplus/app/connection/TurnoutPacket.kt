package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketFactory

// Response or request packet
// Structure:
// LEN ID ADDR_HIGH ADDR_LOW STATE
// For request use STATE == 100
class TurnoutPacket(val address: Int, val state: Int) : Packet() {

    companion object {
        fun newRequest(id: Int) = TurnoutPacket(id, 100)
    }

    override fun getId(): Int {
        return PacketIDs.TURNOUT_PACKET
    }

    fun isTurned() = state == 1 // XNET_TURNOUT_TURNED

    override fun toString(): String {
        return "TurnoutPacket(address=$address, state=$state)"
    }

    class TurnoutPacketFactory : PacketFactory<TurnoutPacket> {


        override fun toData(packet: TurnoutPacket): ByteArray {
            val data = ByteArray(3)

            val address = packet.address - 1// Since turnout ids are internal 0 based
            // This is okay because addresses are unsigned and only have a range from 0 to 1023
            data[0] = ((address shr 8) and 0xff).toByte() // High address byte
            data[1] = (address and 0xff).toByte() // Low address byte
            data[2] = packet.state.toByte()
            return data
        }

        override fun fromData(data: ByteArray): TurnoutPacket {
            if (data.size != 3) {
                throw IllegalStateException("Invalid data length")
            }
            val state = data[2].toInt()
            val address: Int = ((data[0].toInt() shl 8).or(data[1].toInt())) + 1 // Same as above
            return TurnoutPacket(address, state)
        }
    }
}
