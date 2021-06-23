package com.github.se7_kn8.xcontrolplus.protocol.packet;

public interface PacketFactory<T extends Packet> {

	byte[] toData(T packet);

	T fromData(byte[] data);
}
