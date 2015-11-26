
#ifndef MAX44009_H
#define MAX44009_H

#include "mbed.h"



#define MAX44009_I2C_ADDRESS 0x97  //1001 1011 -- A0 PIN is conected to VDD




class MAX44009{   
    
    
    
 public:   
 
   /** Create an MAX44009 instance
     * @param sda pin 
     * @param scl pin 
     * @param address: I2C slave address 
     */
    MAX44009(PinName sda, PinName scl,int i2cFrequency=100000,int address = MAX44009_I2C_ADDRESS); 

    /** Create a MAX44009 instance
     * @param i2c object
     * @param address: I2C slave address 
     */
    MAX44009(I2C& i2c, int address = MAX44009_I2C_ADDRESS); 
   
    int getStatus(void);
    
    int readLuxIntensity(void);
    
    
    inline float getLuxIntensity(void){
        return this->luxIntensity;
        }
    
 private:
    
    I2C i2c;   
    int i2cAddr;
    float luxIntensity;
 
    /** Write data to the given register
     *  
     * @returns
     *   1 on success,
     *   0 on error
     */  

    int write(uint8_t regAddress, uint8_t data);
    
    
    /** Write data to the given register
     * @param register Address
     * @param data to read
     * @param length of data to read 
     * @returns
     *   1 on success,
     *   0 on error
     */  
   int read(uint8_t regAddress, uint8_t* data,int length); 
    
    
    
    

};


#endif 