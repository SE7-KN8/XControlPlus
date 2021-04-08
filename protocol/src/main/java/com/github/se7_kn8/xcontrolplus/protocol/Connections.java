package com.github.se7_kn8.xcontrolplus.protocol;

import com.github.se7_kn8.xcontrolplus.protocol.impl.serial.SerialConnectionType;

import java.util.ArrayList;
import java.util.List;

public class Connections {

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

	private Connections() {

	}

	private static List<ConnectionType> types = List.of(EmptyConnectionType.INSTANCE, new SerialConnectionType());

	public static List<ConnectionType> getTypes() {
		return types;
	}
}
