
/*meteo station ver 0.0.01 uszko l.*/

#include "mbed.h"
#include "sensors.h"
#include "rtos.h"


#define PRESS_ON  0
#define PRESS_OFF 1
DigitalIn  sw1(PTC3);  //if(sw1) Release else Press
DigitalIn  sw3(PTC12); //while(sw3); wait for Press
  
SENSORS sensors;
void SensorsMeasurementFuncWrapper(void const* args){
    sensors.measurement(args);
} 
void SensorsPrintfDataFuncWrapper(void const* args){
    sensors.getResults(args);
} 

void SensorsSaveDataToUSBdeviceFuncWrapper(void const* args){
    sensors.saveDataToUSB(args);
} 
int main(){
// DigitalOut gpo(D0);
// DigitalOut led(LED_RED);

Thread measThread(SensorsMeasurementFuncWrapper,NULL,osPriorityAboveNormal);
Thread printResultsThread(SensorsPrintfDataFuncWrapper,NULL,osPriorityHigh);
Thread saveResultsOnUSBdeviceThread(SensorsSaveDataToUSBdeviceFuncWrapper,NULL,osPriorityNormal);

 while(1){
      }
}

