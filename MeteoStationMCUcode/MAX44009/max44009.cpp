#include "max44009.h"
#include <math.h>

 
MAX44009::MAX44009(PinName sda, PinName scl, int i2cFrequencyHz, int address):i2c(sda,scl), i2cAddr(address){
     i2c.frequency(i2cFrequencyHz);
     this->luxIntensity=0;
    }



int MAX44009::write(uint8_t regAddress, uint8_t data){
        
    char temp[2];
    temp[0]=regAddress;
    temp[1]=data;
    if(i2c.write(i2cAddr,temp,2)==0)return 1;
    return -1;
}



//read data from the sensor
int MAX44009::read(uint8_t regAddress, uint8_t *data,int length){
    
    i2c.write(i2cAddr,(char*)&regAddress,1);
    if(i2c.read(i2cAddr,(char*)data,length)==0)return 1;
    return -1;
}




int MAX44009::getStatus(void){

    uint8_t data=0;
    uint8_t regAddress=0x03 ;
    if(i2c.write(i2cAddr,(char*)&regAddress,1,true)) return -1;
    if(i2c.read(i2cAddr,(char*)&data,1)) return -1;
    return data;
     
}
      
      
int MAX44009::readLuxIntensity(void){
    /*According to datasheet of MAX44009 -> { Lux Intensity = 2^(exponent)*mantissa*0.045 } */
    uint8_t exponent;
    uint8_t mantissa;
    
    uint8_t highAndLowByte[2];
    uint8_t regAddress=0x03 ;
    if(i2c.write(i2cAddr,(char*)&regAddress,1,true)) return -1;
    if(i2c.read(i2cAddr,(char*)highAndLowByte,2,true)) return -1;
    
    mantissa=(((highAndLowByte[0]&0x0F)<<4)|(highAndLowByte[1]&0x0F));
    exponent= (highAndLowByte[0]&0xF0)>>4;
    this->luxIntensity= (float)(pow(2,(float)exponent)*mantissa*0.045);
    return 1;
}