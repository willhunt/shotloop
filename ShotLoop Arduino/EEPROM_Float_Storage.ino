// SHOT LOOP Arduino Code
// Simple extension to the EEPROM library
//
// Will Hunt 2015

#include <avr/EEPROM.h>

float readFloatEeprom(int address) {
  float out;
  eeprom_read_block((void *) &out, (unsigned char *) address ,4 );
  return out;
}

void writeFloatEeprom(float value, int address) {
  eeprom_write_block((void *) &value, (unsigned char *) address ,4);
}

// END EEPROM Float
