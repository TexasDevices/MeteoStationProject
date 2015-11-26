/**
 * @file    BufferedSerial.cpp
 * @brief   Software Buffer - Extends mbed Serial functionallity adding irq driven TX and RX
 * @author  sam grove
 * @version 1.0
 * @see
 *
 * Copyright (c) 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "BufferedSerial.h"
#include <stdarg.h>

BufferedSerial::BufferedSerial(PinName tx, PinName rx, const char* name)
    : Serial(tx, rx, name)
{
    Serial::attach(this, &BufferedSerial::rxIrq, Serial::RxIrq);

    return;
}

BufferedSerial::~BufferedSerial(void)
{
    Serial::attach(NULL, Serial::RxIrq);
    Serial::attach(NULL, Serial::TxIrq);

    return;
}

int BufferedSerial::readable(void)
{
    return _rxbuf.available();  // note: look if things are in the buffer
}

int BufferedSerial::writeable(void)
{
    return 1;   // buffer allows overwriting by design, always true
}

int BufferedSerial::getc(void)
{
    return _rxbuf;
}

int BufferedSerial::putc(int c)
{
    _txbuf = (char)c;
    BufferedSerial::prime();

    return c;
}

int BufferedSerial::puts(const char *s)
{
    const char* ptr = s;

    while(*(ptr) != 0) {
        _txbuf = *(ptr++);
    }
    _txbuf = '\n';  // done per puts definition
    BufferedSerial::prime();

    return (ptr - s) + 1;
}

int BufferedSerial::printf(const char* format, ...)
{
    char buf[256] = {0};
    int r = 0;

    va_list arg;
    va_start(arg, format);
    r = vsprintf(buf, format, arg);
    // this may not hit the heap but should alert the user anyways
    if(r > sizeof(buf)) {
        error("%s %d buffer overwrite!\n", __FILE__, __LINE__);
    }
    va_end(arg);
    r = write(buf, r);

    return r;
}

ssize_t BufferedSerial::write(const void *s, size_t length)
{
    const char* ptr = (const char*)s;
    const char* end = ptr + length;

    while (ptr != end) {
        _txbuf = *(ptr++);
    }
    BufferedSerial::prime();

    return ptr - (const char*)s;
}


void BufferedSerial::rxIrq(void)
{
    // read from the peripheral and make sure something is available
    if(serial_readable(&_serial)) {
        _rxbuf = serial_getc(&_serial); // if so load them into a buffer
    }

    return;
}

void BufferedSerial::txIrq(void)
{
    // see if there is room in the hardware fifo and if something is in the software fifo
    while(serial_writable(&_serial)) {
        if(_txbuf.available()) {
            serial_putc(&_serial, (int)_txbuf.get());
        } else {
            // disable the TX interrupt when there is nothing left to send
            Serial::attach(NULL, Serial::TxIrq);
            break;
        }
    }

    return;
}

void BufferedSerial::prime(void)
{
    // if already busy then the irq will pick this up
    if(serial_writable(&_serial)) {
        Serial::attach(NULL, Serial::TxIrq);    // make sure not to cause contention in the irq
        BufferedSerial::txIrq();                // only write to hardware in one place
        Serial::attach(this, &BufferedSerial::txIrq, Serial::TxIrq);
    }

    return;
}


