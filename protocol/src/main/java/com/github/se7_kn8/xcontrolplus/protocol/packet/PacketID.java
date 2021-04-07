package com.github.se7_kn8.xcontrolplus.protocol.packet;

public enum PacketID {

	ECHO;


	public int getId() {
		return this.ordinal();
	}

	public static PacketID fromId(int id) {
		return PacketID.values()[id];
	}

}
