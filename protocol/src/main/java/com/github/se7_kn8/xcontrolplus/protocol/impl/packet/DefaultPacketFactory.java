package com.github.se7_kn8.xcontrolplus.protocol.impl.packet;

import com.github.se7_kn8.xcontrolplus.protocol.packet.EchoPacket;
import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet;
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketFactory;

import java.util.HashMap;

public class DefaultPacketFactory {

	public static DefaultPacketFactory INSTANCE = new DefaultPacketFactory();
	private final static HashMap<Integer, PacketFactory<?>> factories = new HashMap<>();

	static {
		INSTANCE.registerPacket(EchoPacket.ECHO_PACKET_ID, new EchoPacket.EchoPacketFactory());
	}


	private DefaultPacketFactory() {

	}

	public void registerPacket(Integer id, PacketFactory<?> factory) {
		factories.put(id, factory);
	}


	@SuppressWarnings("unchecked")
	public <T extends Packet> PacketFactory<T> getFactoryById(int id) {
		return (PacketFactory<T>) factories.get(id);
	}

	public boolean isRegistered(int id) {
		return factories.get(id) != null;
	}


}
