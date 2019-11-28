// SHOT LOOP Arduino Code
// Espresso machine PID temperature control
// App control from Android based SHOT LOOP
//
// Will Hunt 2015
//
// PID Control based upon BBC, Tim Hirzel, 2008
//
// Tabs are loaded as if written below this main sketch
//   Variables cannot be referenced beofre they are initialised
//   Functions can be called to return these variables



// Addresses to EEPROM memory
#define PGAIN_ADR 0               // Store as floats which are 4 bytes each, thus 0,4,8,12,...
#define IGAIN_ADR 4
#define DGAIN_ADR 8
#define ESPTEMP_ADR 12

#define PID_UPDATE_INTERVAL 500   // milliseconds (Was 200 - changed because of http://mimsywabe.blogspot.co.uk/2008/03/temperature-problem-solved.html

// Define global variables
float targetTemp;                 // Current temperature goal
float heatPower;                  // 0 - 1000  milliseconds on per second
unsigned long lastPIDTime;        // Most recent PID update time in ms 

void setup() {
  setupDefault();
  resetDefault(PGAIN_ADR, IGAIN_ADR, DGAIN_ADR, ESPTEMP_ADR);
  
  setupPID(PGAIN_ADR, IGAIN_ADR, DGAIN_ADR );         // Send addresses to the PID module
  targetTemp = readFloatEeprom(ESPTEMP_ADR);      // from EEPROM. load the saved value
  lastPIDTime = millis();
  // Module setup calls
  setupDebugSerial();
  setupHeater();
  setupTempSensor();
  setupOnOff();
  setupWifi();
  
}


void loop() {  

  updateTempSensor();

  // Every second, udpate the current heat control, and print out current status
  if (millis() < lastPIDTime) {                                  // This checks for rollover with millis()
    lastPIDTime = 0;
  }
  if ((millis() - lastPIDTime) > PID_UPDATE_INTERVAL) {         // Execute if time passed since last update is great than the update interval
    lastPIDTime +=  PID_UPDATE_INTERVAL;                        // Increase time until next update by interval      
    heatPower = updatePID(targetTemp, getAveTemp());            // Calculate heat power based upon PID control
    setHeatPowerPercentage(heatPower);
  }  
  
  updateHeater();
  wifiCheckSerial();
  debugCheckSerial();
  
} // END LOOP


