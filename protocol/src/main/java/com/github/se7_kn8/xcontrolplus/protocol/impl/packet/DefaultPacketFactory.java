package com.github.se7_kn8.xcontrolplus.protocol.impl.packet;

import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet;
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketFactory;
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketID;

import java.util.HashMap;

public class DefaultPacketFactory {

	private static HashMap<PacketID, PacketFactory<?>> factories = new HashMap<>();

	public static DefaultPacketFactory INSTANCE = new DefaultPacketFactory();

	private DefaultPacketFactory() {

	}

	public void registerPacket(PacketID id, PacketFactory<?> factory) {
		factories.put(id, factory);
	}

	@SuppressWarnings("unchecked")
	public <T extends Packet> PacketFactory<T> getFactoryById(PacketID id) {
		return (PacketFactory<T>) factories.get(id);
	}

	public <T extends Packet> PacketFactory<T> getFactoryById(int id) {
		return getFactoryById(PacketID.fromId(id));
	}


}
