#include "max4070.h"


 MAX4070::MAX4070(AnalogIn sensorInput, int resolution):mSensorInput(sensorInput),mResolution(resolution){
     }
 
 
 float MAX4070::getResult(void){
     
     return result;
     }
 
 int MAX4070::readValueFromInput(void){
     
     uint16_t resValue = mSensorInput.read_u16();
     
     //for 3.3 Vref - resolution is 0.00005[V] 
     float realMeasuredVoltage=( (float)((resValue *(3.3/0xFFFF)-1.013) /*correction for our sensor*/)*1000);
     result=realMeasuredVoltage/mResolution;
     return 1; //TODO handling error 
}



MAX4070Voltage::MAX4070Voltage(AnalogIn sensorInput, int resolution):MAX4070(sensorInput,resolution){};
    
int MAX4070Voltage::readValueFromInput(void){
     
     uint16_t resValue = mSensorInput.read_u16();
     //for 3.3 Vref - resolution is 0.00005[V] 
     float realMeasuredVoltage=(float)(resValue *0.00005*((3.3+4.3)/3.3));
     result=realMeasuredVoltage;
     return 1; //TODO handling error 
}
