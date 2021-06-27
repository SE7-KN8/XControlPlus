package com.github.se7_kn8.xcontrolplus.protocol;

import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet;

import java.io.IOException;
import java.util.function.Consumer;

public interface Connection {

	void openConnection() throws IOException;

	boolean testConnection(int timeout);

	boolean isOpen();

	void closeConnection();

	void setOnPacketReceived(Consumer<? extends Packet> packetConsumer);

	<T extends Packet> void sendPacket(T packet);

	String getFullName();

	String getSimpleName();

}
