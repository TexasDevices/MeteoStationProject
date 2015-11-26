/*
  @file BMP180.h
  
  @brief Barometric Pressure and Temperature Sensor BMP180 Breakout I2C Library      

  @Author spiridion (http://mailme.spiridion.net)

  Tested on LPC1768 and FRDM-KL25Z
  
  Copyright (c) 2014 spiridion
  Released under the MIT License (see http://mbed.org/license/mit)

  Documentation regarding the BMP180 can be found here: 
  http://mbed.org/media/uploads/spiridion/bst-bmp180-ds000-09.pdf
*/

#ifndef BMP180_H
#define BMP180_H

#include "mbed.h"

///  default address is 0xEF 
#define BMP180_I2C_ADDRESS 0xEF 

// Oversampling settings
#define BMP180_OSS_ULTRA_LOW_POWER 0        // 1 sample  and  4.5ms for conversion
#define BMP180_OSS_NORMAL          1        // 2 samples and  7.5ms for conversion
#define BMP180_OSS_HIGH_RESOLUTION 2        // 4 samples and 13.5ms for conversion
#define BMP180_OSS_ULTRA_HIGH_RESOLUTION 3  // 8 samples and 25.5ms for conversion

#define UNSET_BMP180_PRESSURE_VALUE 0.F
#define UNSET_BMP180_TEMPERATURE_VALUE -273.15F // absolute zero

/** BMP180 class.
 *  Read Pressure and temperature from the BMP180 Breakout I2C sensor
 *
 * Example:
 * @code
 * #include "mbed.h"
 * #include "BMP180.h"
 * 
 * #if defined(TARGET_LPC1768)
 *     #define PIN_SDA p9
 *     #define PIN_SCL p10
 * #elif defined(TARGET_KL25Z) // watch out for the PTE0/PTE1 mixed up in the KL25Z doc 
 *     #define PIN_SDA PTE0
 *     #define PIN_SCL PTE1
 * #endif
 * 
 * int main() 
 * {    
 *     BMP180 bmp180(PIN_SDA, PIN_SCL);
 *     float pressure, temperature;
 *     
 *     // bmp180.Initialize(); // no altitude compensation and normal oversampling 
 *     bmp180.Initialize(64, BMP180_OSS_ULTRA_LOW_POWER); // 64m altitude compensation and low power oversampling
 *     
 *     while(1) 
 *     {        
 *         if (bmp180.ReadData(&pressure, &temperature))
 *             printf("Pressure(hPa): %8.2f \t Temperature(C): %8.2f\n", pressure, temperature);  
 *         wait(1);
 *     }
 * }
 * @endcode
 */
class BMP180 
{

public:

    /** Create a BMP180 instance
     * @param sda pin 
     * @param scl pin 
     * @param address: I2C slave address 
     */
    BMP180(PinName sda, PinName scl, int address = BMP180_I2C_ADDRESS); 

    /** Create a BMP180 instance
     * @param i2c object
     * @param address: I2C slave address 
     */
    BMP180(I2C& i2c, int address = BMP180_I2C_ADDRESS); 

    /** Initialization: set member values and read BMP180 calibration parameters
     * @param altitude (in meter)
     * @param overSamplingSetting 
     */
    int Initialize(float altitude = 0.F, int overSamplingSetting = BMP180_OSS_NORMAL);

    /** Read pressure and temperature from the BMP180.
     * @param pressure (hPa) 
     * @param temperature (C) 
     * @returns
     *   1 on success,
     *   0 on error
     */    
    int ReadData(float* pTemperature = NULL, float* pPressure = NULL);

    /** Get temperature from a previous measurement 
     *  
     * @returns
     *   temperature (C)
     */    
    float GetTemperature() {return m_temperature;};

     /** Get pressure from a previous measurement 
     *  
     * @returns
     *   pressure (hPa)
     */    
   float GetPressure() {return m_pressure;};

protected:

    /** Perform temperature measurement
     *  
     * @returns
     *   temperature (C)
     */    
    int ReadRawTemperature(long* pUt);

    /** Perform pressure measurement 
     *  
     * @returns
     *   temperature (C)
     */    
    int ReadRawPressure(long* pUp);

    /** Calculation of the temperature from the digital output
     */    
    float TrueTemperature(long ut);

    /** Calculation of the pressure from the digital output
     */    
    float TruePressure(long up);

    int m_oss;
    float m_temperature;     
    float m_pressure;
    float m_altitude;

    I2C m_i2c;   
    int m_addr;
    char m_data[4];    

    short ac1, ac2, ac3; 
    unsigned short ac4, ac5, ac6;
    short b1, b2;
    short mb, mc, md;
    long x1, x2, x3, b3, b5, b6;
    unsigned long b4, b7;

};

#endif