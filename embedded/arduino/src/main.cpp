#include <Arduino.h>
#include <SoftwareSerial.h>

#define SRX 8
#define STX 9

SoftwareSerial output(8, 9);

#define ECHO_PACKET_ID "$;0"

#define PACKET_INTERVAL 2000
#define TURNOUT_PACKET_TURNED "$;10;id:22;turned:false;\r\n"
#define TURNOUT_PACKET_NOT_TURNED "$;10;id:22;turned:true;\r\n"

void returnCommand(const String &command) {
    output.print(command);
    output.println("\n");
}

void setup() {
    Serial.begin(19200);
    output.begin(19200);
}

void loop() {
    static unsigned long nextPacket = millis() + PACKET_INTERVAL;
    static bool flag = false;
    if (nextPacket < millis()) {
        nextPacket = millis() + PACKET_INTERVAL;
        flag = !flag;
        if (flag) {
            output.print(TURNOUT_PACKET_TURNED);
        } else {
            output.print(TURNOUT_PACKET_NOT_TURNED);
        }
    }

    if (output.available()) {
        auto line = output.readStringUntil('\n');
        Serial.println(line);
        if (line.startsWith(ECHO_PACKET_ID)) {
            returnCommand(line);
        }
    }
}