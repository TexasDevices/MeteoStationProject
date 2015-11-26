#ifndef __DS1621_H
#define __DS1621_H

#define VERSION 1.0

#include <mbed.h>

//Temperature conversion commands
#define READ_TEMPERATURE    0xAA
#define READ_COUNTER        0xA8
#define READ_SLOPE          0xA9
#define START_CONVERT_T     0xEE
#define STOP_CONVERT_T      0x22

//Thermostat commands
#define ACCESS_TH           0xA1
#define ACCESS_TL           0xA2
#define ACCESS_CONFIG       0xAC

//Config register
#define DONE                0x80
#define THF                 0x40
#define TLF                 0x20
#define NVB                 0x10
#define POL                 0x02
#define ONESHOT             0x01

class DS1621 {
public:
    DS1621(I2C* interface, unsigned char address);

    // Set configuration register
    void SetConfig(unsigned char cfg);

    // Read a DS1621 register
    unsigned char ReadReg(unsigned char reg);

    // Sets temperature limit
    // -- works only with ACCESS_TL, and ACCESS_TH
    void SetLimit(unsigned char reg, float temp);

    // Start/Stop DS1621 temperature conversion
    void StartConversion(bool start);

    // Reads temperature or limit
    // -- works only with READ_TEMPERATURE, ACCESS_TL, and ACCESS_TH
    // -- for actual temperature, set ONESHOT mode off and start conversion
    // -- returns true if reg valid
    bool GetTemp(unsigned char reg, float *Temp);

    // Read high resolution temperature
    // -- returns temperature in 1/100ths degrees
    // -- DS1621 is set in ONESHOT mode
    // -- returns true if conversion has finished, new temperature is calculated
    // -- returns false if conversion is busy, previous temperature is given
    bool GetHResTemp(float *Temp);

private:
    I2C* i2c; // Communication interface
    unsigned char address;
    void ReadChipTemp(unsigned char cmd, unsigned char *data);
    bool  conversion_busy;
    float LastTemp;
};
#endif