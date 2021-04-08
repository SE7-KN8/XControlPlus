package com.github.se7_kn8.xcontrolplus.protocol.impl.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.github.se7_kn8.xcontrolplus.protocol.Connection;
import com.github.se7_kn8.xcontrolplus.protocol.impl.packet.DefaultPacketFactory;
import com.github.se7_kn8.xcontrolplus.protocol.packet.EchoPacket;
import com.github.se7_kn8.xcontrolplus.protocol.packet.Packet;
import com.github.se7_kn8.xcontrolplus.protocol.packet.PacketFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SerialConnection implements Connection {

	private class TestConnectionTask implements Callable<Boolean>, Consumer<Packet> {

		private EchoPacket receivedPacket;

		@Override
		@SuppressWarnings("BusyWait")
		public Boolean call() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					EchoPacket testPacket = new EchoPacket();
					sendPacket(testPacket);
					Thread.sleep(250);
					if (receivedPacket != null && receivedPacket.getRandomNumber() == testPacket.getRandomNumber()) {
						return true;
					}
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					e.printStackTrace();
					try {
						// Give the serial connection some time
						Thread.sleep(250);
					} catch (InterruptedException interruptedException) {
						break;
					}
				}
			}
			return false;
		}

		@Override
		public void accept(Packet echoPacket) {
			if (echoPacket instanceof EchoPacket) {
				receivedPacket = (EchoPacket) echoPacket;
			}
		}

	}

	private class PacketParserTask implements Runnable {

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					String toParse = packetsToParse.take();
					try {
						packetConsumer.accept(parsePacket(toParse));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	private class PacketSenderTask implements Runnable {

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Packet packetToSend = packetsToSend.take();
					try {
						sendPacketBlocking(packetToSend);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	public static final int BAUDRATE = 19200;
	private final SerialPort port;
	private boolean active = false;
	private final BlockingQueue<String> packetsToParse = new LinkedBlockingQueue<>();
	private final BlockingQueue<Packet> packetsToSend = new LinkedBlockingQueue<>();
	private Thread parserThread;
	private Thread senderThread;
	private Consumer<? extends Packet> packetConsumer = packet -> {
		// NOP
	};

	public SerialConnection(SerialPort port) {
		this.port = port;
	}


	@Override
	public void openConnection() throws IOException {
		if (active) {
			return;
		}
		if (!port.openPort()) {
			throw new IOException("Could not open SerialPort: " + getName());
		}
		port.setComPortParameters(BAUDRATE, 8, 1, 0);
		parserThread = new Thread(new PacketParserTask());
		parserThread.setName(port.getSystemPortName() + "-Parser");
		parserThread.start();
		senderThread = new Thread(new PacketSenderTask());
		senderThread.setName(port.getSystemPortName() + "-Sender");
		senderThread.start();
		port.addDataListener(new SerialPortDataListener() {

			private StringBuilder builder = new StringBuilder();

			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				try {

					byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
					event.getSerialPort().readBytes(buffer, buffer.length);
					String readString = new String(buffer, StandardCharsets.US_ASCII);
					String[] parts = readString.split("\n");
					for (String part : parts) {
						if (part.startsWith("$")) {
							// New paket start
							builder = new StringBuilder();
						}
						builder.append(part);
						// Paket ends
						if (part.endsWith("\r")) {
							packetsToParse.put(builder.toString());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		active = true;
	}

	@Override
	public boolean testConnection(int timeout) {
		TestConnectionTask testConnectionTask = new TestConnectionTask();
		Consumer<? extends Packet> oldConsumer = packetConsumer;
		packetConsumer = testConnectionTask;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Boolean> future = executor.submit(testConnectionTask);
		boolean success;
		try {
			success = future.get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			success = false;
		}
		future.cancel(true);
		executor.shutdownNow();
		packetConsumer = oldConsumer;
		return success;
	}

	@Override
	public void closeConnection() {
		if (!active) {
			return;
		}
		senderThread.interrupt();
		parserThread.interrupt();
		port.removeDataListener();
		port.closePort();
		active = false;
	}

	@Override
	public void setOnPacketReceived(Consumer<? extends Packet> packetConsumer) {
		this.packetConsumer = packetConsumer;
	}


	@Override
	public <T extends Packet> void sendPacket(T packet) {
		packetsToSend.add(packet);
	}

	private synchronized <T extends Packet> void sendPacketBlocking(T packet) {
		HashMap<String, String> data = DefaultPacketFactory.INSTANCE.<T>getFactoryById(packet.getId()).toData(packet);
		StringBuilder sb = new StringBuilder();
		sb.append("$;");
		sb.append(packet.getId());
		sb.append(";");
		data.forEach((key, value) -> {
			sb.append(key);
			sb.append(":");
			sb.append(value);
			sb.append(";");
		});
		sb.append("\r\n");
		if (port.isOpen()) {
			byte[] bytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
			port.writeBytes(bytes, bytes.length);
		} else {
			throw new IllegalStateException("Serial port " + port.getSystemPortName() + " is closed");
		}
	}

	private <T extends Packet> T parsePacket(String packet) {
		String[] parts = packet.split(";");
		if (parts[0].equals("$")) {
			HashMap<String, String> data = new HashMap<>();
			PacketFactory<T> factory = DefaultPacketFactory.INSTANCE.getFactoryById(Integer.parseInt(parts[1]));
			for (int i = 2; i < parts.length; i++) {
				if (parts[i].contains(":")) {
					String[] keyValue = parts[i].split(":");
					data.put(keyValue[0], keyValue[1]);
				}
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
