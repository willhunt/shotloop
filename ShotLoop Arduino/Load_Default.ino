// SHOT LOOP Arduino Code
// Code for loading default control values
//
// Will Hunt 2015

// Addresses in EEPROm of default values
#define PGAIN_DEFAULT_ADR 80
#define IGAIN_DEFAULT_ADR 84
#define DGAIN_DEFAULT_ADR 88
#define ESPTEMP_DEFAULT_ADR 92

static const float pgain_default = 1.5;
static const float igain_default = 0.001;
static const float dgain_default = 2500.0;
static const float esptemp_default = 100.0;

/*
 *  Setup default values to EEPROM
 */
void setupDefault() {
  writeFloatEeprom(pgain_default, PGAIN_DEFAULT_ADR);
  writeFloatEeprom(igain_default, IGAIN_DEFAULT_ADR);
  writeFloatEeprom(dgain_default, DGAIN_DEFAULT_ADR);
  writeFloatEeprom(esptemp_default, ESPTEMP_DEFAULT_ADR);
}

/*
 *  Set default values to EEPROM
 */
void resetDefault(unsigned int pAdd, unsigned int iAdd, unsigned int dAdd, unsigned int esptempAdd){
   writeFloatEeprom(readFloatEeprom(PGAIN_DEFAULT_ADR), pAdd);
   writeFloatEeprom(readFloatEeprom(IGAIN_DEFAULT_ADR), iAdd);
   writeFloatEeprom(readFloatEeprom(DGAIN_DEFAULT_ADR), dAdd);
   writeFloatEeprom(readFloatEeprom(ESPTEMP_DEFAULT_ADR), esptempAdd);
}

/*
 *  Set target temperature
 */
void setTargetTemp(float t) {
  targetTemp = t;
  writeFloatEeprom(t, ESPTEMP_ADR);
}

/*
 *  Return target temperature
 */
float getTargetTemp() {
  return targetTemp;
}

