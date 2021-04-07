package com.github.se7_kn8.xcontrolplus.protocol.packet;

import java.util.HashMap;
import java.util.Random;

public class EchoPacket extends Packet {

	public static final int ECHO_PACKET_ID = 0;

	public static class EchoPacketFactory implements PacketFactory<EchoPacket> {
		@Override
		public HashMap<String, String> toData(EchoPacket packet) {
			HashMap<String, String> data = new HashMap<>();
			data.put("RANDOM", String.valueOf(packet.random));
			return data;
		}

		@Override
		public EchoPacket fromData(HashMap<String, String> data) {
			EchoPacket packet = new EchoPacket();
			packet.random = Integer.parseInt(data.get("RANDOM"));
			return packet;
		}
	}

	private int random;

	public EchoPacket() {
		random = new Random().nextInt();
	}

	public int getRandomNumber() {
		return random;
	}

	@Override
	public int getId() {
		return 0;
	}
}
