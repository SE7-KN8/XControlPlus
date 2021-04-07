package com.github.se7_kn8.xcontrolplus.protocol.packet;

import com.github.se7_kn8.xcontrolplus.protocol.impl.packet.DefaultPacketFactory;

public abstract class Packet {

	public Packet() {
		DefaultPacketFactory.INSTANCE.registerPacket(getId(), getFactory());
	}

	public abstract PacketID getId();

	@SuppressWarnings("rawtypes")
	public abstract PacketFactory getFactory();


}
