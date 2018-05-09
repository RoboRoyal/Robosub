#from __future__ import absolute_import

#from Adafruit_GPIO.GPIO import *

import socket
import time

# Import SPI library (for hardware SPI) and MCP3008 library.
#import Adafruit_GPIO.SPI as SPI
#from Adafruit_GPIO.GPIO import *
#import Adafruit_MCP3008
#from .MCP3008 import MCP3008
import Adafruit_GPIO.SPI as SPI
import Adafruit_MCP3008


from multiprocessing import Pool

SPI_PORT   = 0
SPI_DEVICE = 0
HOST = "localhost"
PORT = 9901

def read(file):
    #y = 6
    SPI_PORT  = 0
    if(file == "right.txt"):
        SPI_PORT = 0#change to 1
    print("Reading")
    mcp = Adafruit_MCP3008.MCP3008(spi=SPI.SpiDev(SPI_PORT, SPI_DEVICE))
    #samples = 10 * 1000
    #data = [0] * (samples)
    data = [0]
    start = time.time()

    while(time.time()-start < 2):
    #for i in range(len(data)):
        #x = mcp.read_adc(0)
        #data[i] = x + 1024 * y
        #y = mcp.read_adc(1)
        data.append(mcp.read_adc(0))
    print(file," samples: ",len(data))
    f = open(file, "w+")
    for i in data:
        f.write("%s\n" % i)
    f.close()
    print("done")
    return "done"


def startMe():
    print("now")
    with Pool(5) as p:
        p.map(read, ["left.txt","right.txt"])
    return 1


def main():
    RUN = True
    print("Starting Py")
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.close()
    s.close()
    s.close()
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print('Socket created')
    try:
        s.bind((HOST, PORT))
    except socket.error as err:
        print('Bind failed. Error Code : ',err)
    s.listen(10)
    print("Socket Listening")
    conn, addr = s.accept()
    while (RUN):
        data2 = conn.recv(1024).decode(encoding='UTF-8')
        print(data2)
        if (data2 == "start"):
            conn.send(bytes("Started!" + "\r\n", 'UTF-8'))
            #conn.send("Started!")
            startMe()
            print("end")
            conn.send(bytes("Finished" + "\r\n", 'UTF-8'))
        elif(data2 == "close"):
            print("Closing")
            s.close()
            RUN = False
        else:
            conn.send(bytes("Hello there!" + "\r\n", 'UTF-8'))
        data2 = "noDat"


if __name__ == '__main__':
    main()

