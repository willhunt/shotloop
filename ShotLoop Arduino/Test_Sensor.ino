//// SHOT LOOP Arduino Code
//// Pot read for testing program
////
//// Will Hunt 2015
//
//#define PIN_POT A1
//#define filterVal .2 //between 0 and 1 for smoothing function (small=more smooth)
//float accum; //storage for smoothing function
//
//float tcSum = 0.0;
//float latestReading = 0.0;
//int readCount = 0;
//
//
//void setupPot() {
//  pinMode(PIN_POT, INPUT);     // Set on/off relay pin as output
//}
//
///*
// *  Sum up and count temperature reaings for averaging in getAveTemp() function
// */
//void updateTempSensor() {
//    tcSum += analogRead(PIN_POT);                                     // Sum up temperature readings to average
//    readCount +=1;                                                    // Count number of readings for average
//}
//
///*
// *  Return average of temperature readings since last called 
// */
//float getAveTemp() { 
//  
//  float multiplier = (100.0/1023.0);                                 // My sensor (lm35)
//  float offset = 20.0;
//  latestReading = tcSum * multiplier/readCount + offset;             // Average temperatures (tcSum/read count)
//  accum = (latestReading * filterVal) + (accum * (1 - filterVal));    //smoothing function from  http://mimsywabe.blogspot.co.uk/2008/03/temperature-problem-solved.html
//  latestReading = accum;
//  readCount = 0;                                                     // Reset read count and sum
//  tcSum = 0.0;
//  
//  //Serial.print("Temp is = ");
//  //Serial.println(latestReading);
//  
//  return latestReading;
//
//}
//
///*
// *  Returns last averaged temperature 
// */
//float getLastTemp() {
//  return latestReading;
//
//}
//
//// END Temperature Sensor

