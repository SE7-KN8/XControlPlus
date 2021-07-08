#ifndef XPRESSNET_H
#define XPRESSNET_H

#include "Arduino.h"
#include "RingBufCPP.h"

#define PARITY_EVEN 0
#define PARITY_ODD 1

#ifndef __AVR_ATmega328P__
#error "This library only currently supports ATmega328p (Arduino UNO)"
#endif

#define XNET_PACKET_SIZE 8

#ifndef XNET_RECEIVE_BUFFER
#define XNET_RECEIVE_BUFFER XNET_PACKET_SIZE
#endif

#ifndef XNET_READ_BUFFER
#define XNET_READ_BUFFER 10
#endif

#ifndef XNET_SEND_BUFFER
#define XNET_SEND_BUFFER 10
#endif

#define XNET_REQUEST_RESUME_OPERATIONS 0x81
#define XNET_REQUEST_EMERGENCY_OFF 0x80
#define XNET_REQUEST_SOFTWARE_VERSION 0x21
#define XNET_REQUEST_STATUS 0x24

#define XNET_STATION_STATUS_EMERGENCY_OFF 0
#define XNET_STATION_STATUS_EMERGENCY_STOP 1
#define XNET_STATION_STATUS_START_MODE 2
#define XNET_STATION_STATUS_SERVICE_MODE 3
#define XNET_STATION_STATUS_POWER_UP 6
#define XNET_STATION_STATUS_RAM_ERROR 7

#define XNET_STATION_VERSION_MAJOR(v) (((v) >> 4) & 0b1111)
#define XNET_STATION_VERSION_MINOR(v) ((v) & 0b1111)

#define XNET_TRACK_POWER_NORMAL 0x01
#define XNET_TRACK_POWER_OFF 0x00
#define XNET_TRACK_POWER_SHORT_CIRCUIT 0x08 // Roconet extension
#define XNET_TRACK_POWER_EMERGENCY_STOP 0xAB // Default value is 0x00 but since this is already used by 'off' we a different value
#define XNET_TRACK_POWER_SERVICE_MODE 0x02

#define XNET_TURNOUT_UNKNOWN 0x0
#define XNET_TURNOUT_TURNED  0x1
#define XNET_TURNOUT_STRAIGHT 0x2
#define XNET_TURNOUT_ILLEGAL 0x3

#define XNET_TURNOUT_ACTIVATE_1 0b1000
#define XNET_TURNOUT_DEACTIVATE_1 0b0000
#define XNET_TURNOUT_ACTIVATE_2 0b1001
#define XNET_TURNOUT_DEACTIVATE_2 0b0001

struct XpressNetPacket {

    explicit XpressNetPacket() = default;

    explicit XpressNetPacket(uint8_t length) : length(length) {};

    explicit XpressNetPacket(uint8_t length, const uint8_t *data) : length(length) {
        fill(data);
    };

    uint8_t length = 0;
    uint8_t data[XNET_PACKET_SIZE]{0};

public:
    void fill(const uint8_t *newData);
};

class XNet {
public:
    void begin(uint8_t address, uint8_t ioControlPin);

    void update();

    inline bool sendPacket(XpressNetPacket packet);

    // Request some basic things from the station
    void request(uint8_t what);

    // Address is zero based; Turnout 1 == Address 0
    void requestTurnoutStatus(uint16_t address);

    // Address is zero based; Turnout 1 == Address 0
    void requestTurnoutOperation(uint16_t address, uint8_t operation);

    inline void _rxCompleted();

    inline void _txCompleted();

private:
    inline void initUart();

    uint8_t withParity(uint8_t data);

    inline void flush();

    void addXorByte(XpressNetPacket &packet);

    void writeAck();

    void sendFromData(const uint8_t *data, uint8_t len);

    void write(uint16_t data);

    inline uint16_t readUart();

public:
    void (*onSoftwareVersion)(uint8_t stationVersion, uint8_t stationId) = nullptr;

    void (*onStationStatus)(uint8_t statusBits) = nullptr;

    void (*onTrackPower)(uint8_t newTrackPower) = nullptr;

    void (*onTurnoutStatus)(uint16_t address, uint8_t status) = nullptr;

#ifdef XNET_DEBUG
    void (*on_packet)(XpressNetPacket packet) = nullptr;
#endif

private:
    // These need to be 9bit since the call flag must be set
    volatile uint16_t inqByte;
    volatile uint16_t ackByte;
    volatile uint16_t resByte;

    // The control pin for the rs485 transceiver
    volatile uint8_t controlPin;

    // Counter how many bytes to read from uart
    volatile int8_t bytesToRead;

    // Current position in receiver buffer
    volatile uint8_t receiveBufferPos;
    uint8_t receiveBuffer[XNET_RECEIVE_BUFFER];

    XpressNetPacket packetToSend;
    volatile uint8_t sentPos;

    // ISR-Safe buffers to allow async sending and processing of packets
    RingBufCPP<XpressNetPacket, XNET_SEND_BUFFER> sendBuffer;
    RingBufCPP<XpressNetPacket, XNET_READ_BUFFER> readBuffer;
};


extern XNet XpressNet;

#endif //XPRESSNET_H