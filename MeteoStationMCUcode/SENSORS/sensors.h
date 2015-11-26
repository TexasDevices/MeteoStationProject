#include "mbed.h"
#include "BMP180.h"
//#include "USBSerial.h" //for debuger
#include "DHT.h"
#include "adt7410.h"
#include "max44009.h"
#include "btm222.h"
#include "max4070.h"
#include "rtos.h"
#include "USBHostMSD.h"

#define BMP180_ENABLED
#define DHT22_ENABLED
#define ADT7410_ENABLED
#define MAX44009_ENABLED
#define MAX4070_ENABLED
#define BTM222_ENABLED
#define USB_DEVICE_ENABLED
#define USB_DEBUG

#define BMP180_PIN_SDA PTC9    // I2C0
#define BMP180_PIN_SCL PTC8 

#define DHT22_PIN_DATA PTE17

#define ADT7410_PIN_SDA PTE0    //I2C1
#define ADT7410_PIN_SCL PTE1

#define MAX44009_PIN_SDA PTE0    //I2C1
#define MAX44009_PIN_SCL PTE1

#define BTM222_PIN_TX PTE22   //UART2
#define BTM222_PIN_RX PTE23 // czerwony

#define MAX4070_PIN_ANALOG_IN PTB0 //AIN 0 charging current sensor
#define MAX4070_PIN_ANALOG_IN_VOLTAGE PTB1 //AIN 1 charging  sensor



class SENSORS {

public: 

SENSORS();
    
~SENSORS();
    
//public methods to handle in other threads    
void measurement (void const* args);  
void getResults (void const* args);  // get or print all results from the sensors 
int saveDataToUSB(void const* args);
    
private:     
 USBHostMSD* msd;
 BMP180*  bmp180;
 DHT* humiditySensor;
 ADT7410* adt7410_1;
 ADT7410* adt7410_2;
 MAX44009* max44009;
 MAX4070* max4070;
 MAX4070* max4070Voltage;
 BTM222* btm222; 
 
 FILE* fileTemperature;
 FILE* fileHumidity;
 FILE* filePressure;
 Mutex measPrintMutex;


 struct Results{
      Results():BMP180pressure(0),BMP180temperature(0),DHThumidity(0), DHTtemperature(0),
                ADT7410_1temperature(0),
                ADT7410_2temperature(0),
                MAX44009luxIntensity(0),
                MAX4070chargerCurrent(0),
                MAX4070chargerVoltage(0){}
      float BMP180pressure, BMP180temperature;
      float DHThumidity, DHTtemperature;
      float ADT7410_1temperature, ADT7410_2temperature;
      float MAX44009luxIntensity;
      float MAX4070chargerCurrent;
      float MAX4070chargerVoltage;
};
Results results;

#ifdef USB_DEBUG 
    Serial usbDebug;
#endif

    
};