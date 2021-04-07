package com.github.se7_kn8.xcontrolplus.protocol;

import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet;

import java.io.IOException;

public interface Connection {

	void openConnection() throws IOException;

	boolean testConnection(int timeout);

	void closeConnection();

	<T extends Packet> void sendPacket(T packet);

	<T extends Packet> T receivePacket();

	String getName();

}
