
#include "sensors.h"


/////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// /////////////////////////////////////////////// ///////////////////////////////////////////////

SENSORS::SENSORS():usbDebug(USBTX, USBRX)
{

#ifdef BMP180_ENABLED
    bmp180= new BMP180( BMP180_PIN_SDA, BMP180_PIN_SCL);
    bmp180->Initialize(64, BMP180_OSS_ULTRA_LOW_POWER); // 64m altitude compensation and low power oversampling
#endif

#ifdef DHT22_ENABLED
    humiditySensor=new  DHT(DHT22_PIN_DATA,DHT22);
#endif

#ifdef ADT7410_ENABLED
    adt7410_1= new  ADT7410( ADT7410_PIN_SDA,ADT7410_PIN_SCL,100000);
    adt7410_2 =new ADT7410( ADT7410_PIN_SDA,ADT7410_PIN_SCL,100000,ADT7410_2_I2C_ADDRESS);
#endif

#ifdef MAX44009_ENABLED
    max44009= new MAX44009( MAX44009_PIN_SDA, MAX44009_PIN_SCL ,100000);
#endif

    usbDebug.baud(9600);

#ifdef BTM222_ENABLED
    btm222=new BTM222(BTM222_PIN_TX , BTM222_PIN_RX);
#endif

#ifdef MAX4070_ENABLED
    max4070= new MAX4070(MAX4070_PIN_ANALOG_IN ,4 /*4mv ->1mA*/);
    max4070Voltage= new MAX4070Voltage(MAX4070_PIN_ANALOG_IN_VOLTAGE ,4 );
#endif

#ifdef USB_DEVICE_ENABLED
    msd = new  USBHostMSD("usb");
    if (!msd->connect()) {
        usbDebug.printf(" USB Flash drive not found.\r\n");
    } else {
        fileTemperature = fopen("/usb/pTemp.txt", "w"); //rewrite, or create, attempt to create file /usb/pTemp.txt
        if (fileTemperature) {
            usbDebug.printf("(/usb/pTemp.txt)... sucess file-open!\r\n\r\n");
            fprintf(fileTemperature, "LOG TEMPERATURE stopnie Celsjusza \r\n Numer czujnika:  1- ADT7410_1,   2-ADT7410_2,   3-DHT22,   4-BMP180\r\n INFO: Dane zapisywane sa co sek.\r\n\r\n");
            fclose(fileTemperature);
            fileTemperature=NULL;
        } else usbDebug.printf(" ... failed file-open (/usb/pTemp.txt)!\r\n\r\n");

        fileHumidity= fopen("/usb/pCurrent.txt", "w"); //rewrite, or create
        if (fileHumidity) {
            usbDebug.printf("(/usb/pCurrent.txt)... sucess file-open!\r\n\r\n");
            fprintf(fileHumidity, " LOG CURRENT [mA] - Prad ladowania akumulatora pobierany z generatora \r\n INFO: Dane zapisywane sa co 1 sek.\r\n\r\n");
            fclose(fileHumidity);
            fileHumidity=NULL;
        } else usbDebug.printf(" ... failed file-open (/usb/pCurrent.txt)!\r\n\r\n");

        filePressure=fopen("/usb/pVoltage.txt", "w");
        if (filePressure) {
            usbDebug.printf("(/usb/pVoltage.txt)... sucess file-open!\r\n\r\n");
            fprintf(filePressure, " LOG VOLTAGE [V]- Napiecie wytwarzane przez generator\r\n INFO: Dane zapisywane sa co 1 sek.\r\n\r\n");
            fclose(filePressure);
            filePressure=NULL;
        } else usbDebug.printf(" ... failed file-open (/usb/pVoltage.txt)!\r\n\r\n");
    }
#endif
}

SENSORS::~SENSORS()
{
    if(fileTemperature!=NULL) {
        fclose(fileTemperature);
        fileTemperature=NULL;
    }
    if(fileHumidity!=NULL) {
        fclose(fileHumidity);
        fileHumidity=NULL;
    }
    if(filePressure!=NULL) {
        fclose(filePressure);
        filePressure=NULL;
    }
    delete max44009;
    delete adt7410_1;
    delete adt7410_2;
    delete humiditySensor;
    delete bmp180;
    delete msd;
    delete max4070;
    delete max4070Voltage;
}





void SENSORS:: measurement (void const* args)
{
    while(1) {
       
#ifdef BMP180_ENABLED
        if (bmp180->ReadData( &results.BMP180temperature,&results.BMP180pressure)) {
        } else
            results.BMP180temperature=-999;
#endif

#ifdef DHT22_ENABLED
        if(humiditySensor->readData()) {
            results.DHThumidity= humiditySensor->ReadHumidity();
            results.DHTtemperature = humiditySensor->ReadTemperature(CELCIUS);
        } else {
            results.DHThumidity= -999;
            results.DHTtemperature = -999;
        }
#endif


#ifdef ADT7410_ENABLED
        if(adt7410_1->readTemp()!=-1) {
            results.ADT7410_1temperature=adt7410_1->getTemperature();
        } else {
            results.ADT7410_1temperature=-999;
        }


        if(adt7410_2->readTemp()!=-1) {
            results.ADT7410_2temperature=adt7410_2->getTemperature();
        } else {
            results.ADT7410_2temperature=-999;
        }
#endif


#ifdef MAX44009_ENABLED

        if(max44009->readLuxIntensity())
            results.MAX44009luxIntensity= max44009->getLuxIntensity();
        else
            results.MAX44009luxIntensity=-999;

#endif

#ifdef MAX4070_ENABLED

        max4070->readValueFromInput();
        results.MAX4070chargerCurrent=max4070->getResult();

        max4070Voltage->readValueFromInput();
        results.MAX4070chargerVoltage=max4070Voltage->getResult();
#endif

       
        Thread::wait(300);
    }
}






void SENSORS::getResults (void const* args)
{
    while(1) {
        measPrintMutex.lock();
#ifdef BMP180_ENABLED
#ifdef USB_DEBUG
        if(results.BMP180pressure!=-999&&results.BMP180temperature!=-999)
            usbDebug.printf("Pressure(hPa):   %5.2f\r\nTemperatureBMP180(C):   %5.2f\r\n", results.BMP180pressure, results.BMP180temperature);
        else
            usbDebug.printf("BMP180_ERROR\r\n");
#endif
#endif

#ifdef DHT22_ENABLED
#ifdef USB_DEBUG
        if(results.DHThumidity!=-999&&results.DHTtemperature!=-999)
            usbDebug.printf("Humidity(%): %5.2f\r\nTemperatureDHT22(C): %5.2f\r\n", results.DHThumidity, results.DHTtemperature);
        else
            usbDebug.printf("DHT22_ERROR\r\n");
#endif
#endif

#ifdef ADT7410_ENABLED
#ifdef USB_DEBUG
        if(results.ADT7410_1temperature!=-999)
            usbDebug.printf("TemperatureADT7410_1(C):  %5.2f\r\n",  results.ADT7410_1temperature);
        else
            usbDebug.printf("ADT7410_1ERROR\r\n");
        if(results.ADT7410_2temperature!=-999)
            usbDebug.printf("TemperatureADT7410_2(C):  %5.2f\r\n",  results.ADT7410_2temperature);
        else
            usbDebug.printf("ADT7410_2ERROR\r\n");
#endif
#endif

#ifdef MAX44009_ENABLED
#ifdef USB_DEBUG
        if(results.MAX44009luxIntensity!=-999)
            usbDebug.printf("MAX44009_LUX_INTESITY: %5.2f\r\n",results.MAX44009luxIntensity);
        else
            usbDebug.printf("MAX44009_ERROR\r\n");
#endif
#endif


#ifdef MAX4070_ENABLED
#ifdef USB_DEBUG 
        usbDebug.printf("MAX4070_CurrentValue: %5.2f\r\n",results.MAX4070chargerCurrent);
        usbDebug.printf("MAX4070_VoltageValue: %5.2f\r\n",results.MAX4070chargerVoltage);
#endif
#endif

#ifdef BTM222_ENABLED
        btm222->sendDataToBTM('T',results.ADT7410_2temperature);
        btm222->sendDataToBTM('P',results.BMP180pressure);
        btm222->sendDataToBTM('H',results.DHThumidity);
        btm222->sendDataToBTM('L',results.MAX44009luxIntensity);
        btm222->sendDataToBTM('C',results.MAX4070chargerCurrent);
        btm222->sendDataToBTM('V',results.MAX4070chargerVoltage);
#endif

#ifdef BTM222_ENABLED
   
        while(btm222->isRxDataAvailable())
         usbDebug.printf("data:  %c\r\n",btm222->getDataFromRx());
         



#endif
        measPrintMutex.unlock();
        Thread::wait(500);
    }
}







int SENSORS::saveDataToUSB(void const* args)
{
    while(1) {
          measPrintMutex.lock();
        
        
        if(!msd->connect()) {
#ifdef USB_DEBUG
            usbDebug.printf("ERROR!: USB Flash drive not found. is not connected or broken -> just try to connect it\r\n");
#endif
        } else {
            usbDebug.printf("PENDRIVE IS CONNECTED\r\n");
            if(!fileTemperature) {
                fileTemperature=fopen("/usb/pTemp.txt", "a");
            } else {
                //fprintf(fileTemperature,"1: %5.2f\t\t", results.ADT7410_1temperature); //save to USB
                fprintf(fileTemperature,"2: %5.2f\t\t", results.ADT7410_2temperature);
                fclose(fileTemperature);
                fileTemperature=NULL;
            }
            if(!fileHumidity) {
                fileHumidity=fopen("/usb/pCurrent.txt", "a");
            } else {
                fprintf(fileHumidity,"%5.2f\r\n", results.MAX4070chargerCurrent);
                fclose(fileHumidity);
                fileHumidity=NULL;
            }
            if(!filePressure) {
                filePressure=fopen("/usb/pVoltage.txt", "a");
            } else {
                fprintf(filePressure,"%5.2f\r\n", results.MAX4070chargerVoltage);
                fclose(filePressure);
                filePressure=NULL;
            }
        } 
         measPrintMutex.unlock();
        Thread::wait(1000);
    }
}