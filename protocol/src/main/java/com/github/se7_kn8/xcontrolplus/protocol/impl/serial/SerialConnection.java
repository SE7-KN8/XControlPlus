package com.github.se7_kn8.xcontrolplus.protocol.impl.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.github.se7_kn8.xcontrolplus.protocol.Connection;
import com.github.se7_kn8.xcontrolplus.protocol.impl.packet.DefaultPacketFactory;
import com.github.se7_kn8.xcontrolplus.protocol.packet.EchoPacket;
import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet;
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.*;

public class SerialConnection implements Connection {

	private class TestConnectionTask implements Callable<Boolean> {
		@SuppressWarnings("BusyWait")
		@Override
		public Boolean call() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					EchoPacket testPacket = new EchoPacket();
					sendPacket(testPacket);
					Thread.sleep(250);
					EchoPacket receivePacket = receivePacket();
					if (receivePacket.getRandomNumber() == testPacket.getRandomNumber()) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					try {
						// Give the serial connection some time
						Thread.sleep(250);
					} catch (InterruptedException interruptedException) {
						interruptedException.printStackTrace();
					}
				}
			}
			return true;
		}
	}

	// Maybe change this to allow faster communication
	public static final int BAUDRATE = 9600;
	private final SerialPort port;

	public SerialConnection(SerialPort port) {
		this.port = port;
	}

	@Override
	public void openConnection() throws IOException {
		if (!port.openPort()) {
			port.setComPortParameters(BAUDRATE, 8, 1, 0);
			port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 500, 500);
			throw new IOException("Could not open SerialPort: " + getName());
		}
	}

	@Override
	public boolean testConnection(int timeout) {
		ExecutorService service = Executors.newSingleThreadExecutor();
		Future<Boolean> future = service.submit(new TestConnectionTask());
		boolean success;
		try {
			success = future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		future.cancel(true);
		service.shutdownNow();
		return success;
	}

	@Override
	public void closeConnection() {
		port.closePort();
	}

	@Override
	public <T extends Packet> void sendPacket(T packet) {
		HashMap<String, String> data = DefaultPacketFactory.INSTANCE.<T>getFactoryById(packet.getId()).toData(packet);
		StringBuilder sb = new StringBuilder();
		sb.append("C;");
		sb.append(packet.getId());
		sb.append(";");
		data.forEach((key, value) -> {
			sb.append(key);
			sb.append(":");
			sb.append(value);
			sb.append(";");
		});
		sb.append("\n");
		if (port.isOpen()) {
			byte[] bytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
			port.writeBytes(bytes, bytes.length);
		} else {
			throw new IllegalStateException("Serial port " + port.getSystemPortName() + " is closed");
		}
	}

	@Override
	public <T extends Packet> T receivePacket() {
		if (!port.isOpen()) {
			throw new IllegalStateException("Serial port " + port.getSystemPortName() + " is closed");
		}

		String packet = "";
		try (Scanner portScanner = new Scanner(port.getInputStream(), StandardCharsets.US_ASCII)) {
			packet = portScanner.nextLine();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Error while sending packet to serial port " + port.getSystemPortName());
		}
		System.out.println("PACKET: " + packet);

		String[] parts = packet.split(";");
		System.out.println(parts[0]);
		System.out.println(parts[0].equals("C"));
		if (parts[0].equals("C")) {
			HashMap<String, String> data = new HashMap<>();
			PacketFactory<T> factory = DefaultPacketFactory.INSTANCE.<T>getFactoryById(Integer.parseInt(parts[1]));
			for (int i = 2; i < parts.length; i++) {
				String[] keyValue = parts[i].split(":");
				data.put(keyValue[0], keyValue[1]);
			}
			return factory.fromData(data);
		}
		throw new IllegalStateException("Error while parsing packet");
	}

	@Override
	public String getName() {
		return port.getSystemPortName();
	}

}
