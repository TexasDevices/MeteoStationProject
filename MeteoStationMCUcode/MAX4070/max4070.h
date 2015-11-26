#include "mbed.h"


class MAX4070 {
    
 public: 
 MAX4070(AnalogIn sensorInput, int resolution);
 float getResult(void);
 
 virtual int readValueFromInput(void);   
  virtual ~MAX4070(){};  
   
   
    
protected:   
 AnalogIn  mSensorInput; 
 int mResolution; //it means how many (number of) milivolts we get on the output of the sensor for 1[mA]
 
 float result;
    
   
};


class MAX4070Voltage: public MAX4070{
   public: 
    MAX4070Voltage(AnalogIn sensorInput, int resolution);
    virtual int readValueFromInput(void);   
};