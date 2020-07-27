/*

iTampol
Affordable farming product sorter.
*/
#include <Servo.h>

Servo rejectorServo;

void setup() {
    Serial.begin(9600);
    while (!Serial);
    Serial.println("-------------------------");
    Serial.println("ARos is loading....");
    delay(1000);
    Serial.println("Calibrating servo");
    Serial.println("-------------------------");
    // attach servo to pin D6
    rejectorServo.attach(D6);
    delay(1000);
    rejectorServo.write(0);
    Serial.println("------------------------- ready");
}

void loop() {
  if (Serial.available()) {
    char cmd = Serial.read();
    if (cmd == 'r') {
      Serial.println("rejecting...");
      rejectorServo.write(90);
      delay(100);
      rejectorServo.write(0);
    }
  }
}