// SHOT LOOP Arduino Code
// This is a module that implements a PID control loop
//
// Will Hunt 2015
//
// this was written based on a great PID by Tim Wescott:
// http://www.embedded.com/2000/0010/0010feat3.htm


#define WINDUP_GUARD_GAIN 100.0

float iState = 0;        // Sum of errors for integral term
float lastTemp = 0;      // Previous temp for derivative term

float pgain;
float igain;
float dgain;

float pTerm, iTerm, dTerm; 

int pgainAddress, igainAddress, dgainAddress;

void setupPID(unsigned int padd, unsigned int iadd, unsigned int dadd) {
  // with this setup, you pass the addresses for the PID algorithm to use to 
  // for storing the gain settings.  This way wastes 6 bytes to store the addresses,
  // but its nice because you can keep all the EEPROM address allocaton in once place.

  pgainAddress = padd;
  igainAddress = iadd;
  dgainAddress = dadd;

  pgain = readFloatEeprom(pgainAddress);
  igain = readFloatEeprom(igainAddress);
  dgain = readFloatEeprom(dgainAddress);
}

/*
 *  Functions to return gain terms
 */
float getP() {
  return pgain;                             // get the P gain 
}
float getI() {
  return igain;                             // get the I gain
}
float getD() {
  return dgain;                             // get the D gain
}


void setP(float p) {
  // set the P gain and store it to eeprom
  pgain = p; 
  writeFloatEeprom(p, pgainAddress);
}

void setI(float i) {
  // set the I gain and store it to eeprom
  igain = i; 
  writeFloatEeprom(i, igainAddress);
}

void setD(float d) {
  // set the D gain and store it to eeprom
  dgain = d; 
  writeFloatEeprom(d, dgainAddress);
}

float updatePID(float targetTemp, float curTemp)
{
  // these local variables can be factored out if memory is an issue, 
  // but they make it more readable
  double result;
  float error;
  float windupGaurd;

  // determine how badly we are doing
  error = targetTemp - curTemp;

  // the pTerm is the view from now, the pgain judges 
  // how much we care about error we are this instant.
  pTerm = pgain * error;

  // iState keeps changing over time; it's 
  // overall "performance" over time, or accumulated error
  iState += error;

  // to prevent the iTerm getting huge despite lots of 
  //  error, we use a "windup guard" 
  // (this happens when the machine is first turned on and
  // it cant help be cold despite its best efforts)

  // not necessary, but this makes windup guard values 
  // relative to the current iGain
  windupGaurd = WINDUP_GUARD_GAIN / igain;  

  if (iState > windupGaurd) 
    iState = windupGaurd;
  else if (iState < -windupGaurd) 
    iState = -windupGaurd;
  iTerm = igain * iState;

  // the dTerm, the difference between the temperature now
  //  and our last reading, indicated the "speed," 
  // how quickly the temp is changing. (aka. Differential)
  dTerm = (dgain* (curTemp - lastTemp));

  // now that we've use lastTemp, put the current temp in
  // our pocket until for the next round
  lastTemp = curTemp;

  // the magic feedback bit
  return  pTerm + iTerm - dTerm;
}

// END PID
