#include "ds1621.h"

DS1621::DS1621(I2C* _interface, unsigned char _address): i2c(_interface), address(_address) {
    conversion_busy=false;
    LastTemp=0.0;
    StartConversion(false);

    printf("Searching for I2C devices...\n");

    int count = 0;
    for (int address=4; address<256; address+=2) {
        if (!i2c->write(address, NULL, 0)) { // 0 returned is ok
            printf(" - I2C device found at address 0x%02X\r\n", address);
            count++;
        }
    }
    printf("%d devices found\r\n", count);
}

// Set configuration register
void DS1621::SetConfig(unsigned char cfg) {
    char data[2];
    data[0]=ACCESS_CONFIG;
    data[1]=cfg;
    i2c->write(address,data,2);
}

// Read a DS1621 register
unsigned char DS1621::ReadReg(unsigned char reg) {
    char data[1];
    data[0]=reg;
    i2c->write(address,data,1,true);
    i2c->read(address,data,1);
    return (data[0]);
}

// Sets temperature limit
// -- works only with ACCESS_TL and ACCESS_TH
void DS1621::SetLimit(unsigned char reg, float temp) {
    if (reg == ACCESS_TL || reg == ACCESS_TH) {
        char data[3];
        data[0]=reg;
        if (temp <0.0) {
            temp = ceil(temp * 2.0 - 0.5) / 2.0; //round to 1/2 degree
            if (floor(temp) < temp ) { // check for extra half
                data[1]=(~char(temp * -1.0)); //one's complement
                data[2]=0x80; // -25.5 = -26 + 0.5
            } else {
                data[1]=(~char(temp * -1.0)) + 1; //two's complement
                data[2]=0;   // -25 = -25
            }
        } else {
            temp = floor(temp * 2.0 + 0.5) / 2.0; //round to 1/2 degree
            data[1]=char(temp); //whole degrees
            if (ceil(temp) > temp) { // check for extra half
                data[2]=0x80;
            } else {
                data[2]=0x00;
            }
        }
        i2c->write(address,data,3);
        wait_ms(10);
    }
}

// Start/Stop DS1621 temperature conversion
void DS1621::StartConversion(bool start) {
    char data[1];
    if (start == true) {
        data[0]=START_CONVERT_T;
        i2c->write(address,data,1);
    } else {
        data[0]=STOP_CONVERT_T;
        i2c->write(address,data,1);
    }
}

// Reads temperature or threshold
// -- works only with READ_TEMPERATURE, ACCESS_TL, and ACCESS_TH
// -- DS1621 must be in continouis mode and started for the actual temperature
bool DS1621::GetTemp(unsigned char reg, float *Temp) {
    unsigned char data[2];
    float Tc;

    if (reg == READ_TEMPERATURE || reg == ACCESS_TL || reg == ACCESS_TH) {
        ReadChipTemp(reg,data);

        Tc=float(data[1]>>7) * 0.5;     // decimal part = +0.5 if bit7=1

        if (data[0] >= 0x80) {          // negative? -> make two's complement
            *Temp = float((char (~data[0])+1)*-1) + Tc;
        } else {
            *Temp = float(data[0])+ Tc;
        }
        return (true);
    }
    return (false);
}

// Read high resolution temperature
// -- returns temperature in 1/100ths degrees
// -- DS1620 must be in 1-shot mode
bool DS1621::GetHResTemp(float *Temp) {

    if (conversion_busy==false) {
        SetConfig(ONESHOT);
        StartConversion(true);               // initiate conversion
        conversion_busy=true;
        *Temp = LastTemp;
        return (false);
    }

    if (!(ReadReg(ACCESS_CONFIG) & DONE)) {
        *Temp = LastTemp;
        return (false);
    } else {
        unsigned char data[2];
        // get the results
        ReadChipTemp(READ_TEMPERATURE,data);        // get whole degrees reading
        float cRem = (float)ReadReg(READ_COUNTER); // get counts remaining
        float cSlope = (float)ReadReg(READ_SLOPE);  // get counts per degree
        if (data[0] >= 0x80) {          // negative? -> make two's complement
            LastTemp = float((char (~data[0])+1)*-1);
        } else {
            LastTemp = float(data[0]);
        }
        LastTemp = LastTemp - 0.25 + (cSlope - cRem)/cSlope;
        conversion_busy=false;
        *Temp = LastTemp;
        return (true);
    }

}

void DS1621::ReadChipTemp(unsigned char reg, unsigned char *data) {
    if (reg == READ_TEMPERATURE || reg == ACCESS_TL || reg == ACCESS_TH) {
        char cmd[1];
        cmd[0]=reg;
        i2c->write(address,cmd,1,true);
        i2c->read(address, (char *)data, 2);
    }
}