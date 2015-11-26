/**
 * The library is for Bluetooth Shield from Seeed Studio
 */

#ifndef __BTM222_H__
#define __BTM222_H__

#include "mbed.h"
#include "BufferedSerial.h"

#define BLUETOOTH_SERIAL_DEFAULT_BAUD       19200
#define BLUETOOTH_SERIAL_TIMEOUT            10000
#define BLUETOOTH_SERIAL_EOL                "\r\n"


class BTM222 {
public:
    BTM222 (PinName tx, PinName rx);
    
    void getInfo();


    void sendDataToBTM(char tag,float valueToSend);

    int write(const char* data);

    int isRxDataAvailable();
  
    inline int getDataFromRx(){
      return bufSerial.getc();     
    }
  

protected:
    
    BufferedSerial bufSerial;
    uint8_t    _buf[64];  
};

#endif // __BLUETOOTH_SERIAL_H__
