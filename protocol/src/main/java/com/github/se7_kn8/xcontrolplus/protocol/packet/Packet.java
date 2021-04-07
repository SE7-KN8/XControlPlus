package com.github.se7_kn8.xcontrolplus.protocol.packet;

import com.github.se7_kn8.xcontrolplus.protocol.impl.packet.DefaultPacketFactory;

public abstract class Packet {

	public static <T extends Packet> void registerPacket(int id, PacketFactory<T> factory) {
		if (id < 0) {
			throw new IllegalArgumentException("Packet id must be >= 0");
		}
		if (!DefaultPacketFactory.INSTANCE.isRegistered(id)) {
			DefaultPacketFactory.INSTANCE.registerPacket(id, factory);
		} else {
			throw new IllegalArgumentException("For id= " + id + " is already " + DefaultPacketFactory.INSTANCE.getFactoryById(id).getClass().getSimpleName() + " registered");
		}
	}

	public abstract int getId();

}
