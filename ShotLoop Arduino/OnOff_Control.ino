// SHOT LOOP Arduino Code
// Control for turning machine on and off
//
// Will Hunt 2015

#define PIN_ON 8                                   // Pin controlling on/off relay

/* 
 *  Setup on / off control of espresso machine
 */
void setupOnOff() {
  
  pinMode(PIN_ON, OUTPUT);      // Set on/off relay pin as output
  digitalWrite(PIN_ON, LOW);    // Set machine to off as default
  
}

/* 
 *  Turn espresso machine on or off
 */
void turnOnOff(bool on) {
  if (on) {
    digitalWrite(PIN_ON, HIGH);   // Set machine to on
  } else {
    digitalWrite(PIN_ON, LOW);    // Set machine to off
  }
}

