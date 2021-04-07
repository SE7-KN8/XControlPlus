package com.github.se7_kn8.xcontrolplus.protocol.packet;

import java.util.HashMap;
import java.util.Random;

public class EchoPacket extends Packet {

	private int random;

	public EchoPacket() {
		random = new Random().nextInt();
	}

	public int getRandomNumber() {
		return random;
	}

	@Override
	public PacketID getId() {
		return PacketID.ECHO;
	}

	@Override
	public PacketFactory<EchoPacket> getFactory() {
		return new PacketFactory<>() {
			@Override
			public HashMap<String, String> toData(EchoPacket packet) {
				HashMap<String, String> data = new HashMap<>();
				data.put("RANDOM", String.valueOf(random));
				return data;
			}

			@Override
			public EchoPacket fromData(HashMap<String, String> data) {
				EchoPacket packet = new EchoPacket();
				packet.random = Integer.parseInt(data.get("RANDOM"));
				return packet;
			}
		};
	}
}
