package com.github.se7_kn8.xcontrolplus.protocol.packet;

import java.util.HashMap;

public interface PacketFactory<T extends Packet> {

	HashMap<String, String> toData(T packet);

	T fromData(HashMap<String, String> data);
}
