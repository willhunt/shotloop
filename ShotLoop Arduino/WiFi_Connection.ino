// SHOT LOOP Arduino Code
// WiFi connection using esp8266 WiFi module
//
// Will Hunt 2015


#include <SoftwareSerial.h>
#define ID "VM369865-2G"                            // SSID
//#define ID "BTHomeHub2-MQ3Z"                      // SSID
#define PASS "zsrannnx"                             // Router WiFi password
//#define PASS "6ecbaaf382"                         // Router WiFi password
#define BUFFER_SIZE 256

#define DEBUG_WIFI_MIN true                         // Display minimum setup and connection info
#define DEBUG_WIFI_MAX false                         // Display all wifi info

#define softRX 2                                    // Pins for communcation to the esp8266
#define softTX 3

// Define commands FROM android
const char request_on = 'n';                        // Turn machine on
const char request_off = 'f';                       // Turn machine off
const char request_connect = 'c';                   // Check devices are talking to each other
const char request_disconnect = 'd';                // Close connection
const char request_temp = 't';                      // Request temperature
const char change_gain = 'p';                       // Change proportional gain

// Define commands TO android
const char confirm_on = 'N';                        // Turn machine on
const char confirm_off = 'F';                       // Turn machine off
const char confirm_connect = 'C';                   // Check devices are talking to each other
const char confirm_disconnect = 'D';                // Close connection
const char send_temp = 'T';                         // Send temperature reading
const char confirm_gain = 'G';                      // Confirm gains are changed

char buffer[BUFFER_SIZE];                           // Buffer for reading in serial data
int connectionId, packetLen;                        // Channel id ad packet length
 
SoftwareSerial esp8266(softRX, softTX);             // Connect the TX line from the esp to the Arduino's pin softRX
                                                    // and the RX line from the esp to the Arduino's pin softTX

/* 
 *  Setup wifi connection
 */
void setupWifi() {
  
  esp8266.begin(38400);                             // Begin ESP8266 serial (Max 38400 for softwareserial)
  while (!esp8266);                                 // While the esp8266 serial stream is not open, do nothing
  
  debugPrintString("Connecting...", "", DEBUG_WIFI_MIN);
  sendCommand("AT+RST\r\n", 2000, DEBUG_WIFI_MIN);                              // Reset module
  sendCommand("AT+CWMODE=1\r\n", 1000, DEBUG_WIFI_MIN);                         // configure as access point
  String connectionAT = "AT+CWJAP=\"";                                          // Concatenate command including router details
  connectionAT += ID;
  connectionAT += "\",\"";
  connectionAT += PASS;
  connectionAT += "\"\r\n";
  sendCommand(connectionAT, 3000, DEBUG_WIFI_MIN);
  delay(10000);                                                                 // Wait for connction to be made
  String esp8266_IP = sendCommand("AT+CIFSR\r\n", 1000, DEBUG_WIFI_MIN);        // Get IP address
  sendCommand("AT+CIPMUX=1\r\n", 1000, DEBUG_WIFI_MIN);                         // Cconfigure for multiple connections
  sendCommand("AT+CIPSERVER=1,80\r\n", 1000, DEBUG_WIFI_MIN);                   // Turn on server on port 80

  debugPrintString("Arduino Ready", "", DEBUG_WIFI_MIN);
  
}

/* 
 *  Check esp8266 for http request and respond
 */
void wifiCheckSerial() {
  if(read_till_EOL())                                                            // If serial available fill buffer
  {
    char* command = parseLine(buffer);                                           // Parse buffer for command
    if (command != NULL)
    {
      switch(command[0])                                                         // Respond to command
      {
        case request_on:{
          turnOnOff(1);                                                          // Turn machine on
          sendHTTPResponse(connectionId, String(confirm_on));
          debugPrintString("Machine on", "", DEBUG_WIFI_MIN);
          break;
        }case request_off:{
          turnOnOff(0);                                                          // Turn machine off
          sendHTTPResponse(connectionId, String(confirm_off));
          debugPrintString("Machine off", "", DEBUG_WIFI_MIN);
          break;
        }case request_connect:{
          turnOnOff(0);                                                          // Ensure machine is off on connect
          sendHTTPResponse(connectionId, String(confirm_connect));
          debugPrintString("Devices connected", "", DEBUG_WIFI_MIN);
          break;
        }case request_disconnect:{
          turnOnOff(0);                                                          // Turn machine off
          sendHTTPResponse(connectionId, String(confirm_disconnect));
          String closeCommand = "AT+CIPCLOSE=";                                  // Make close command
          closeCommand += connectionId;                                          // Append connection id
          closeCommand += "\r\n";
          sendCommand(closeCommand, 1000, DEBUG_WIFI_MAX);                       // Close connection
          debugPrintString("Devices disconnected", "", DEBUG_WIFI_MIN);
          break;
        }case request_temp:{
          String tempString = String(send_temp);                                 // Include inditifier char at start
          tempString += String(getLastTemp(), 2);                                // Get latest temp to 2 decimal places
          sendHTTPResponse(connectionId, tempString);                            // Send temp reading to android
          debugPrintString("Temp sent:", tempString, DEBUG_WIFI_MIN);
          break;
        }case change_gain:{
          changeGains(command);                                                  // Change PID gains
          sendHTTPResponse(connectionId, String(confirm_gain));                  // Send to android
          break;
        }default:{

        debugPrintString("Command not recognised --> ", command, DEBUG_WIFI_MIN);
        }
      }
    }
  }
}

/*
 *  Function that sends HTTP 200, HTML UTF-8 response
 */
void sendHTTPResponse(int connectionId, String content) {

     String httpResponse;                                                   // Build HTTP response
     String httpHeader;                                                     // HTTP Header
     httpHeader = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n"; 
     httpHeader += "Content-Length: ";
     httpHeader += content.length();
     httpHeader += "\r\n";
     httpHeader += "Connection: close\r\n\r\n";
     httpResponse = httpHeader + content + " ";                             // Bug: the last character of "content" is not sent
                                                                            // I cheated by adding this extra space
     debugPrintString("Sending -> ", content, DEBUG_WIFI_MAX);
     sendCIPData(connectionId, httpResponse);
}

/*
 *  Sends a CIPSEND=<connectionId>,<data> command
 */
void sendCIPData(int connectionId, String data) {
   String cipSend = "AT+CIPSEND=";
   cipSend += connectionId;
   cipSend += ",";
   cipSend += data.length();
   cipSend += "\r\n";
   
   debugPrintString("Cip Data -> ", cipSend, DEBUG_WIFI_MAX);

   sendCommand(cipSend, 1000, DEBUG_WIFI_MAX);                            // Tell esp8266 where to send data
   sendData(data, 1000, DEBUG_WIFI_MAX);                                  // Send data (HTTP)
}

/*
 *  Function used to send data to ESP8266
 */
String sendCommand(String command, const int timeout, boolean debug) {
    
    String response = "";
    esp8266.print(command);                                               // Send the read character to the esp8266
    
    long int time = millis();
    while( (time + timeout) > millis()) {
      while(esp8266.available()) {                                        // The esp has data so display its output to the serial window 
        char c = esp8266.read();                                          // read the next character.
        response += c;
      }  
    }
    debugPrintString("ESP8266 Returned -> ", response, debug);
    
    return response;
}

/* 
 *  Used to send data to the esp8266
 */
String sendData(String command, const int timeout, boolean debug) {
  
    String response = "";

    int dataSize = command.length();
    char data[dataSize];
    command.toCharArray(data, dataSize);

           
    esp8266.write(data, dataSize); // send the read character to the esp8266
   
    if(debug) {
      Serial.println("\r\nHTTP Response From Arduino:");
      Serial.write(data,dataSize);
      Serial.println("\r\n");
    } 

    long int time = millis();
    // Run for time period equal to "timeout" (millis will increase on each iteration)
    while( (time + timeout) > millis()) {
      while(esp8266.available()) {
        // The esp has data so read
        char c = esp8266.read(); // read the next character.
        response += c;
      }  
    }

    if(debug) {
      Serial.print(response);
    }
    
    return response;
}

/*
 *  Read data until end of line
 */
bool read_till_EOL() {
  static int i = 0;                                                     // Buffer char index
  if(esp8266.available()) {
    buffer[i++] = esp8266.read();                                       // Fill up buffer
    if(i == BUFFER_SIZE)  i = 0;                                        // Avoid buffer overflow error
    if(i > 1 && buffer[i - 2] == 13 && buffer[i - 1] == 10) {           // Check end characters
      buffer[i] = 0;                                              
      i = 0;
      debugPrintString("", buffer, DEBUG_WIFI_MAX);
      return true;
    }
  }
  return false;
}

/*
 *  Returns command from parsed line
 */
char* parseLine(char* buffer) {
  // Data format is ->  +IPD,channel,length: type?command: HTTP/1.1
  if(strncmp(buffer, "+IPD,", 5) == 0) {                                // Compare first 5 chars of buffer
    sscanf(buffer+5, "%d,%d", &connectionId, &packetLen);               // Scan channel id and packet length
    
    if (packetLen > 0) {
      char* startCommand = strchr(buffer, '?') + 1;                     // Find start of command
      if (startCommand == NULL) {                                       // Error check
        Serial.println("Command not recognised (could not find '?')");
        return NULL;
      }
      //char command = startCommand[0];
      char command[12];                                                 // Create command string
      int i = 0;
      while ((startCommand[i]) != ':') {                                // Check for colon terminator
        command[i] = startCommand[i];                                   // Save command
        i++;
      }
      command[i] = '\0';                                                // Null terminate
      
      debugPrintString("Recieved -> ", command, DEBUG_WIFI_MAX);
      return command;
      
    }else {
      Serial.println("0 packet length");
      return NULL;
    }
    
  }else {
    return NULL;
  }
}

/*
 *  Gets time/date from router
 */
String requestRouterTime(){
  
}

/*
 *  Parse command and change gains
 */
void changeGains(char* command) {

  char* delimiter = "pid";                    // Delimiter array
  String debugString = "Gains -> ["; 
  
  int i = 0;                                  // Index along command string
  for (int j = 0; j < 3; j++){                // Index trhough P -> I -> D
    char gain[12];                           // Gain string
    int k = 0;                                // Index though gain string
    while (command[i] != delimiter[j]){
      gain[k] = command[i];
      k++;
      i++;
    }  
    gain[j] = '\0';                           // NULL terminate gain string
    
    float fGain = atof(gain);                 // Set relevant PID value
    if (j == 0) {
      setP(fGain);
    }else if (j == 1){
      setI(fGain);  
    }else{
      setD(fGain);
    }
    debugString += fGain;                    // Add to debug string
  }
  debugString += "]";
  debugPrintString("", debugString, DEBUG_WIFI_MAX);
}


                                                    
