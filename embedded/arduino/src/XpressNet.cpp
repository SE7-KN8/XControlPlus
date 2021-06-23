#include "XpressNet.h"

XNet XpressNet{};

void XpressNetPacket::fill(const uint8_t *newData) {
    for (int i = 0; i < length; ++i) {
        data[i] = newData[i];
    }
}

void XNet::begin(uint8_t address, uint8_t ioControlPin) {
    controlPin = ioControlPin;
    pinMode(controlPin, OUTPUT);
    digitalWrite(controlPin, LOW);
    inqByte = withParity(address | 0x40) | 0x100; // Add the ninth bit
    ackByte = withParity(address | 0x00) | 0x100;
    resByte = withParity(address | 0x60) | 0x100;
    initUart();
}

void XNet::update() {
    XpressNetPacket packet;
    while (readBuffer.pull(&packet)) {
#ifdef XNET_DEBUG
        if (on_packet) { on_packet(packet); }
#endif
        switch (packet.data[0]) {
            case 0x42: { // Accessory information response
                if (onTurnoutStatus) {
                    // TODO handle more things, like feedback decoders
                    uint16_t base_address = packet.data[1] * 4;
                    if ((packet.data[2] >> 4) & 0b1) { // If nibble byte is set add 2 the address
                        base_address += 2;
                    }
                    uint8_t first_state = (packet.data[2] >> 0) & 0b11;
                    uint8_t second_state = (packet.data[2] >> 2) & 0b11;
                    onTurnoutStatus(base_address + 0, first_state);
                    onTurnoutStatus(base_address + 1, second_state);
                }
                break;
            }
            case 0x61: { // Track status updates
                if (onTrackPower) { onTrackPower(packet.data[1]); }
                break;
            }
            case 0x62: { // Station status
                if (onStationStatus) { onStationStatus(packet.data[2]); }
                break;
            }
            case 0x63: { // Software version
                if (onSoftwareVersion) { onSoftwareVersion(packet.data[2], packet.data[3]); }
                break;
            }
            case 0x81: {// Track status updates
                if (onTrackPower) { onTrackPower(XNET_TRACK_POWER_EMERGENCY_STOP); }
                break;
            }
        }
    }

}

inline bool XNet::sendPacket(XpressNetPacket packet) {
    return sendBuffer.add(packet);
}

void XNet::request(uint8_t what) {
    uint8_t data[] = {0x21, what};
    sendFromData(data, 2);
}


void XNet::requestTurnoutStatus(uint16_t address) {
    uint8_t group = address / 4;
    uint8_t nibble = address % 4;
    if (nibble <= 1) {
        nibble = 0x80 + 0x0;
    } else {
        nibble = 0x80 + 0x1;
    }
    uint8_t data[] = {0x42, group, nibble};
    sendFromData(data, 3);
}


void XNet::requestTurnoutOperation(uint16_t address, uint8_t operation) {
    uint8_t group = address / 4;
    uint8_t rest_lsb = (address % 4) << 1;
    uint8_t data_2 = 0x80 + (rest_lsb | operation);
    uint8_t data[] = {0x52, group, data_2};
    sendFromData(data, 3);
}


// USART tx complete interrupt handler
inline void XNet::_txCompleted() {
    uint16_t data = readUart();
    if (data == inqByte) {
        XpressNetPacket packet;
        if (sendBuffer.pull(&packet)) {
            digitalWrite(controlPin, HIGH);
            writePacket(packet);
            digitalWrite(controlPin, LOW);
        }
    } else if (data == resByte) {
        bytesToRead = 1; // The packet size is currently unknown so just read the header
        receiveBufferPos = 0; // Just in case to prevent a index out of bound if there is a malformed packet
    } else if (data == ackByte) {
        writeAck();
    } else if (bytesToRead > 0) {
        bytesToRead--;

        if (receiveBufferPos == 0) { // This a header byte
            bytesToRead = (data & 0b1111) + 1; // Add the xor byte to the packet length
        }

        receiveBuffer[receiveBufferPos] = (uint8_t) data; // This cast is okay, because only the callbyte has 9bits
        receiveBufferPos++;
    } else if (bytesToRead == 0) {
        // Drop packet if buffer is full
        if (!readBuffer.isFull()) {
            // Add the read packet to the read buffer
            XpressNetPacket packet = XpressNetPacket(receiveBufferPos, receiveBuffer);
            readBuffer.add(packet);
        }
        // Packet has been completed
        bytesToRead = -1;
        receiveBufferPos = 0;
    } else if (data == 0x160) { // Track power broadcast
        bytesToRead = 1;
        receiveBufferPos = 0;
    }
#ifdef XNET_LISTEN_TO_EVERYTHING // When this is set we will listen to every response, even to other addresses
    else if(data >= 0x100){ // callbyte is set
        bytesToRead = 1;
        receiveBufferPos = 0;
    }
#endif
}

// Calculates the parity bit and sets it to the last bit of the given byte
uint8_t XNet::withParity(uint8_t data) {
    uint16_t parity = PARITY_EVEN;
    // Discord the 8th bit
    data &= 0x7F;
    uint16_t dataCopy = data;

    while (data) {
        parity ^= (data & 0b1);
        data >>= 1;
    }

    if (parity) {
        dataCopy |= 0x80;
    }
    return dataCopy;
}

//
void XNet::initUart() {
    cli(); // Disable interrupts while initializing the uart

    // Set baudrate
    // TODO make this independent from the cpu frequency
    // This result in 16 as clock pre-scaler
    // 16_000_000 Hz / 16 = 1_000_000 Baud (Atmega328p uart pre-scaler)
    // 1_000_000 / 16 = 62_500 (The desired frequency)
    UBRR0H = 0x0;
    UBRR0L = 0x0F;

    // Enable receiver, transmitter, receive complete interrupt and 9th bit
    UCSR0B = (1 << RXEN0) | (1 << TXEN0) | (1 << RXCIE0) | (1 << UCSZ02);

    sei(); // Enable interrupts
}

void XNet::sendFromData(const uint8_t *data, uint8_t len) {
    XpressNetPacket packet = XpressNetPacket(len, data);
    addXorByte(packet);
    sendPacket(packet);
}

void XNet::addXorByte(XpressNetPacket &packet) {
    uint8_t xor_byte = 0;
    for (int i = 0; i < packet.length; ++i) {
        xor_byte ^= packet.data[i];
    }

    packet.data[packet.length] = xor_byte;
    packet.length++;
}


void XNet::writeAck() {
    uint8_t data[] = {0x20, 0x20};
    XpressNetPacket packet = XpressNetPacket(2, data);
    digitalWrite(controlPin, HIGH);
    writePacket(packet);
    digitalWrite(controlPin, LOW);
}

inline void XNet::flush() {
    __attribute__((unused)) uint8_t dummy;
    while (UCSR0A & (1 << RXC0)) {
        dummy = UDR0;
    }
}

inline void XNet::write(uint16_t data) {
    // Wait for empty transmit buffer
    while (!(UCSR0A & (1 << UDRE0))) {}
    // Copy 9th bit to TXB8
    UCSR0B &= ~(1 << TXB80);
    if (data & 0x0100) {
        UCSR0B |= (1 << TXB80);
    }
    // Put data into buffer, sends the data
    UDR0 = data;

    // Wait for complete transmit
    while (!(UCSR0A & (1 << TXC0))) {};
    UCSR0A = (1 << TXC0);
    UCSR0A = 0;
}

void XNet::writePacket(XpressNetPacket &packet) {
    for (int i = 0; i < packet.length; ++i) {
        write(packet.data[i]);
    }
}

inline uint16_t XNet::readUart() {
    uint8_t status, resh, resl;
    // TODO check if this necessary
    // Wait for data to be received
    while (!(UCSR0A & (1 << RXC0))) {}

    // Get status and 9th bit, then data
    status = UCSR0A;
    resh = UCSR0B;
    resl = UDR0;

    // If error, return -1
    if (status & ((1 << FE0) | (1 << DOR0) | (1 << UPE0))) {
        return -1;
    }

    resh = (resh >> 1) & 0x01;
    return ((resh << 8) | resl);
}


ISR(USART_RX_vect) {
    XpressNet._txCompleted();
}