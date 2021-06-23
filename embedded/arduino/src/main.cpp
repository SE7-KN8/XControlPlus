
#include "Arduino.h"
#include "XpressNet.h"
#include "SoftwareSerial.h"

SoftwareSerial output(11, 12);

void on_track_power(uint8_t new_track_power) {
    digitalWrite(LED_BUILTIN, new_track_power != XNET_TRACK_POWER_NORMAL);
}

void on_turnout_status(uint16_t address, uint8_t state) {
    uint8_t address_high = (address >> 8) & 0xff;
    uint8_t address_low = address & 0xff;
    uint8_t data[] = {5, 10, address_high, address_low, state}; // len, id, address_low, address_high, state
    output.write(data, 5);
}

uint8_t readBufferPos = 0;
int8_t bytesToRead = -1; // Not in packet
uint8_t readBuffer[10];

void parsePacket(const uint8_t data[]) {
    uint8_t length = data[0];
    switch (data[1]) {
        case 0x0: {
            if (length == 3) {
                uint8_t echo[] = {3, 0, data[2]};
                output.write(echo, 3);
            }
            break;
        }
    }
}

void receivePackets() {
    if (output.available()) {
        uint8_t readByte = output.read();
        if (bytesToRead == -1) {
            bytesToRead = readByte;
            readBuffer[readBufferPos] = readByte;
            readBufferPos++;
            bytesToRead--;
        } else if (bytesToRead >= 1) {
            readBuffer[readBufferPos] = readByte;
            bytesToRead--;
            readBufferPos++;
            if (bytesToRead == 0) {
                parsePacket(readBuffer);
                bytesToRead = -1;
                memset(readBuffer, 0, 10);
                readBufferPos = 0;
            }
        }
    }
}

#ifdef XNET_DEBUG

void on_packet(XpressNetPacket packet) {
    output.write(packet.length);
}

#endif

void setup() {
    pinMode(13, OUTPUT);
    XpressNet.begin(28, 8);
    output.begin(19200); // Try with lower speeds at first
    XpressNet.onTrackPower = on_track_power;
    XpressNet.onTurnoutStatus = on_turnout_status;
#ifdef XNET_DEBUG
    XpressNet.on_packet = on_packet;
#endif
}

void loop() {
    XpressNet.update();
    receivePackets();
}
