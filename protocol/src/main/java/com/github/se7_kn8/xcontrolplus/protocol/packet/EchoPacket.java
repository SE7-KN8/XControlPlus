package com.github.se7_kn8.xcontrolplus.protocol.packet;

import java.util.Random;

public class EchoPacket extends Packet {

	public static final int ECHO_PACKET_ID = 0;

	public static class EchoPacketFactory implements PacketFactory<EchoPacket> {
		@Override
		public byte[] toData(EchoPacket packet) {
			return new byte[]{packet.random};
		}

		@Override
		public EchoPacket fromData(byte[] data) {
			if (data.length != 1) {
				throw new IllegalStateException("Wrong packet length");
			}
			EchoPacket packet = new EchoPacket();
			packet.random = data[0];
			return packet;
		}
	}

	private byte random;

	public EchoPacket() {
		random = (byte) new Random().nextInt();
	}

	public byte getRandomNumber() {
		return random;
	}

	@Override
	public int getId() {
		return 0;
	}
}
