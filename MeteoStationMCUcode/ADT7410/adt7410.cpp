#include "adt7410.h"

 
ADT7410::ADT7410(PinName sda, PinName scl, int i2cFrequencyHz, int address,CONF_RESOLUTION res ):i2c(sda,scl), i2cAddr(address){
     i2c.frequency(i2cFrequencyHz);
     if(res==_16_BIT){
      setResolution(_16_BIT);  
     }
    }




bool ADT7410::write(uint8_t regAddress, uint8_t data){
        
    char temp[2];
    temp[0]=regAddress;
    temp[1]=data;
    return i2c.write(i2cAddr,temp,2)==0;
}



//read data from the sensor
int ADT7410::read(uint8_t regAddress, uint8_t *data,int length){
    i2c.write(i2cAddr,(char*)&regAddress,1);
    if(i2c.read(i2cAddr,(char*)data,length)==0) return 1;
    return -1;
}

//configuration of ADT7410 sensor
int ADT7410::initADT7410(CONF_FAULT_QUEUE faultQueue, CONF_CT_PIN_POLARITY ctPinPolarity, CONF_INT_PIN_POLARITY intPinPolarity, CONF_INT_CT_MODE intCtMode, CONF_OPERATION_MODE operMode, CONF_RESOLUTION res){
    
    uint8_t confByte=0;
    int retVal=0;
    
    confByte=(res<<7|operMode<<5|intCtMode<<4|intPinPolarity<<3|ctPinPolarity<<2|faultQueue);
    if(!write(0x03,confByte))retVal=-1;
    else retVal=1;
    return retVal;
    
    }

// read 13 bit temperature
int ADT7410::readTemp(){
    
    char rData[2] = {0,0};
    float tempFin = 0;
    int tempRaw = 0;

    // set address pointer to temperature register
    if(i2c.write(i2cAddr, 0x00, 1))return -1;

    // read temperature register, two bytes
    if(i2c.read(i2cAddr, rData, 2))return -1;

    // temperature returned is only 13 bits
    // discard alarm flags in lower bits
    tempRaw = (rData[0] << 8) | (rData[1]);
    if(resolution==_13_BIT){ ////resolution 13 --- bit 0.0625°C
        tempRaw >>= 3;
        if ( tempRaw & 0x1000) {
            tempFin = (float) (tempRaw - 8192) / 16;
        } 
        else {
            tempFin = (float) tempRaw / 16;     
        }
    }
    else{  //resolution 16bit --- 0.0078°C.
        
        if(tempRaw &0x8000){
            tempFin =(float) (tempRaw-65536)/128;
        }
        else
            tempFin =(float)tempRaw/128;
    }
    
     currentTemperature=tempFin;
    return 1;
}


int ADT7410::setResolution(CONF_RESOLUTION res){
    
    uint8_t temp=0;
    
    int retVal=0;
    if(!read(0x03,&temp,1)) return -1;
         temp|=res<<7;
        if( !write(0x03,temp))
         
      retVal=0;
     else retVal=1;
      
      return retVal; 
    }


int ADT7410::getIdNumber(void){

    uint8_t data=0;
    uint8_t regAddress=0x0B;
    
      if(i2c.write(i2cAddr,(char*)&regAddress,1,true)) return -1;
      if(i2c.read(i2cAddr,(char*)&data,1)) return -1;
      
      return data;
  
    
}