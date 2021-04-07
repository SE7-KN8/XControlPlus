package com.github.se7_kn8.xcontrolplus.protocol;

import com.github.se7_kn8.xcontrolplus.protocol.impl.serial.SerialConnectionType;

import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler {

	private static final class EmptyConnectionType implements ConnectionType {

		private static final EmptyConnectionType INSTANCE = new EmptyConnectionType();

		private EmptyConnectionType() {

		}

		@Override
		public List<Connection> listConnections() {
			return new ArrayList<>();
		}

		@Override
		public String getName() {
			return "None";
		}
	}

	public static List<ConnectionType> getTypes() {
		return List.of(EmptyConnectionType.INSTANCE, new SerialConnectionType());
	}

	private ConnectionType currentType = EmptyConnectionType.INSTANCE;

	public void switchType(ConnectionType newBackend) {
		currentType = newBackend;
	}

	public List<Connection> getConnections() {
		return currentType.listConnections();
	}

	public ConnectionType getCurrentType() {
		return currentType;
	}
}
