package com.github.se7_kn8.xcontrolplus.protocol.impl.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.github.se7_kn8.xcontrolplus.protocol.Connection;
import com.github.se7_kn8.xcontrolplus.protocol.ConnectionType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SerialConnectionType implements ConnectionType {

	@Override
	public List<Connection> listConnections() {
		return Arrays.stream(SerialPort.getCommPorts()).map((Function<SerialPort, Connection>) SerialConnection::new).collect(Collectors.toList());
	}

	@Override
	public String getFullName() {
		return "SerialConnection-jSerialComm" + SerialPort.getVersion();
	}

	@Override
	public String getSimpleName() {
		return "serial";
	}
}
