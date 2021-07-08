
#include "Arduino.h"
#include "XpressNet.h"
#include "AltSoftSerial.h"

AltSoftSerial output;

void on_track_power(uint8_t new_track_power) {
    digitalWrite(LED_BUILTIN, new_track_power != XNET_TRACK_POWER_NORMAL);
    uint8_t data[] = {3, 11, new_track_power};
    output.write(data, 3);
}

void on_turnout_status(uint16_t address, uint8_t state) {
    uint8_t address_high = (address >> 8) & 0xff;
    uint8_t address_low = address & 0xff;
    uint8_t data[] = {5, 10, address_high, address_low, state}; // len, id, address_low, address_high, state
    output.write(data, 5);
}


#define READ_BUFFER_SIZE 10
uint8_t readBufferPos = 0;
int8_t bytesToRead = -1; // Not in packet
uint8_t readBuffer[READ_BUFFER_SIZE];

void parsePacket(const uint8_t data[]) {
    uint8_t length = data[0];
    switch (data[1]) {
        case 0x0: { // Echo packet
            if (length == 3) {
                uint8_t echo[] = {3, 0, data[2]};
                output.write(echo, 3);
            }
            break;
        }
        case 0x0A: { // Turnout packet
            if (length == 5) {
                uint16_t address = (data[2] << 8) | data[3];
                uint8_t state = data[4];
                if (state == XNET_TURNOUT_UNKNOWN) { // Request
                    XpressNet.requestTurnoutStatus(address);
                } else if (state == XNET_TURNOUT_STRAIGHT) {
                    XpressNet.requestTurnoutOperation(address, XNET_TURNOUT_ACTIVATE_2);
                    delay(100);
                    XpressNet.requestTurnoutOperation(address, XNET_TURNOUT_DEACTIVATE_2);
                } else if (state == XNET_TURNOUT_TURNED) {
                    XpressNet.requestTurnoutOperation(address, XNET_TURNOUT_ACTIVATE_1);
                    delay(100);
                    XpressNet.requestTurnoutOperation(address, XNET_TURNOUT_DEACTIVATE_1);
                }
            }
            break;
        }
        case 0x0B: {
            if (length == 3) {
                uint8_t newState = data[2];
                if (newState == XNET_TRACK_POWER_NORMAL) {
                    XpressNet.request(XNET_REQUEST_RESUME_OPERATIONS);
                } else if (newState == XNET_TRACK_POWER_EMERGENCY_STOP) {
                    XpressNet.request(XNET_REQUEST_EMERGENCY_OFF);
                }
            }
        }
    }
}

void clearReadBuffer() {
    bytesToRead = -1;
    memset(readBuffer, 0, 10);
    readBufferPos = 0;
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
            if (readBufferPos >= READ_BUFFER_SIZE) {
                // Drop packets that are too long
                clearReadBuffer();
            }
            readBuffer[readBufferPos] = readByte;
            bytesToRead--;
            readBufferPos++;
            if (bytesToRead == 0) {
                parsePacket(readBuffer);
                clearReadBuffer();
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
    XpressNet.begin(23, 7);
    AltSoftSerial::begin(19200); // Try with lower speeds at first
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
