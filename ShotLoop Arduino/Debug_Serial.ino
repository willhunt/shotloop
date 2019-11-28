// SHOT LOOP Arduino Code
// Serial interface to serial monitor
// Use for debugging code and viewing information (e.g. IP address)
//
// Will Hunt 2015

/* 
 *  Define levels of debug information to display
 */
#define DEBUG_TEMP true                             // Print temperature readings to serial monitor
#define DEBUG_COMMAND true                         // Print temperature readings to serial monitor

void setupDebugSerial() {
  Serial.begin(115200);         // Begin PC serial
  while (!Serial) ;
}

/* 
 *  Check serial for debug commands
 */
void debugCheckSerial() {
  if (Serial.available() > 0) {                    // Check if there is a serial input
    delay(10);                                     // Allow buffer to fill up
    String debugInput = "";                        // Initialise string for recieved command
    
    while (Serial.available() > 0) {
      char inChar = (char)Serial.read();           // Read in next char
      debugInput += inChar;                        // Concatenate string
    } 
    debugPrintString("", debugInput, DEBUG_COMMAND);   // Print command
    // Check commands for actions
    if (debugInput == "info\r\n" || debugInput == "i\r\n") {
      debugPrintInfo();                            // Print info
    }else {
      Serial.println(F("Command not recognised"));
    } 
  }
}

/* 
 *  Print debug info if debug is true
 */
void debugPrintString(String infoString, String variable, bool debug) {
  // Process wifi min prints
  if (debug) {
    String debugString = infoString; debugString += variable;
    Serial.println(debugString);
  }
}

/* 
 *  Return selection of current values if commanded over serial
 */
 void debugPrintInfo() {
   Serial.print(F("Current temp = ")); Serial.println(getLastTemp());
   Serial.print(F("Target temp = ")); Serial.println(targetTemp);
   String gainString = "PID Gains = ["; 
   gainString += getP(); gainString += " "; gainString += getI(); gainString += " "; gainString += getD();
   gainString += "]";
   Serial.println(gainString);
   
 }
