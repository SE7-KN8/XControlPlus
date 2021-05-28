package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketFactory

class TurnoutPacket(val turnoutId: Int, val turned: Boolean) : Packet() {

    override fun getId(): Int {
        return PacketIDs.TURNOUT_PACKET
    }

    class TurnoutPacketFactory : PacketFactory<TurnoutPacket> {
        override fun toData(packet: TurnoutPacket): HashMap<String, String> {
            val data = HashMap<String, String>()
            data["id"] = packet.turnoutId.toString()
            data["turned"] = packet.turned.toString()

            return data
        }

        override fun fromData(data: HashMap<String, String>): TurnoutPacket {
            return TurnoutPacket(data.getOrDefault("id", "0").toInt(), data.getOrDefault("turned", "false").toBoolean())
        }
    }
}
