package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketFactory

class TrackPowerPacket(val state: Int) : Packet() {

    companion object {
        fun newResumeRequest() = TrackPowerPacket(1) // XNET_TRACK_POWER_NORMAL
        fun newEmergencyStopRequest() = TrackPowerPacket(0xAB) // XNET_TRACK_POWER_EMERGENCY_STOP
    }

    override fun getId() = PacketIDs.TRACK_POWER_PACKET

    override fun toString(): String {
        return "TrackPowerPacket(state=$state)"
    }

    class TrackPowerPacketFactory : PacketFactory<TrackPowerPacket> {
        override fun toData(packet: TrackPowerPacket): ByteArray {
            return byteArrayOf(packet.state.toByte())
        }

        override fun fromData(data: ByteArray): TrackPowerPacket {
            return TrackPowerPacket(data[0].toInt())
        }

    }

}
