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
import java.util.ArrayList;
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
					byte[] packetData = packetsToParse.take();
					try {
						packetConsumer.accept(parsePacket(packetData));
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
	private final BlockingQueue<byte[]> packetsToParse = new LinkedBlockingQueue<>();
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

			private int bytesToRead = -1;// -1 equals not in packet
			private ArrayList<Byte> readBuffer = new ArrayList<>();

			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				try {
					byte[] buffer = new byte[event.getSerialPort().bytesAvailable()];
					event.getSerialPort().readBytes(buffer, buffer.length);

					for (byte b : buffer) {
						if (bytesToRead == -1) {
							readBuffer.clear();
							bytesToRead = Byte.toUnsignedInt(b) - 1;// One byte was already read
							readBuffer.add(b);
						} else if (bytesToRead >= 1) {
							readBuffer.add(b);
							bytesToRead--;
							if (bytesToRead == 0) { // Finish packet
								byte[] packetData = new byte[readBuffer.size()];
								for (int j = 0; j < readBuffer.size(); j++) {
									packetData[j] = readBuffer.get(j);
								}
								packetsToParse.put(packetData);
								bytesToRead = -1;
							}
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
		byte[] data = DefaultPacketFactory.INSTANCE.<T>getFactoryById(packet.getId()).toData(packet);
		byte[] bytesToSend = new byte[data.length + 2]; // Add length and id byte
		if (bytesToSend.length >= 255) {
			throw new IllegalStateException("Packet is too long");
		}
		if (packet.getId() >= 255 || packet.getId() < 0) {
			throw new IllegalStateException("Illegal packet id");
		}
		bytesToSend[0] = (byte) bytesToSend.length;
		bytesToSend[1] = (byte) packet.getId();

		System.arraycopy(data, 0, bytesToSend, 2, bytesToSend.length - 2);

		if (port.isOpen()) {
			port.writeBytes(bytesToSend, bytesToSend.length);
		} else {
			throw new IllegalStateException("Serial port " + port.getSystemPortName() + " is closed");
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Packet> T parsePacket(byte[] packet) {
		if (packet.length <= 1) {
			throw new IllegalStateException("Error while parsing packet");
		}
		int length = Byte.toUnsignedInt(packet[0]);
		int packetId = Byte.toUnsignedInt(packet[1]);
		byte[] packetData = new byte[length - 2]; // Remove length and id byte
		System.arraycopy(packet, 2, packetData, 0, length - 2);
		return ((PacketFactory<T>) DefaultPacketFactory.INSTANCE.getFactoryById(packetId)).fromData(packetData);

	}

	@Override
	public String getName() {
		return port.getSystemPortName();
	}

	@Override
	public boolean isOpen() {
		return port.isOpen();
	}
}
