package com.pi4j.io.serial;



import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EventObject;

/**
 * @see com.pi4j.io.serial.Serial
 * @see SerialDataEventListener
 * @see com.pi4j.io.serial.SerialDataReader
 * @see com.pi4j.io.serial.SerialFactory
 *
 * @see <a href="http://www.pi4j.com/">http://www.pi4j.com/</a>
 * @author Robert Savage (<a
 *         href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 *@author Dakota A.
 *Modified for use in windows and mac env
 */
public class SerialDataEvent extends EventObject {

    private static final long serialVersionUID = 1L;
    private final Serial serial;
    private byte[] cachedData = null;

    /**
     * Default event constructor.
     */
    public SerialDataEvent(Serial serial) {
        super(serial);
        this.serial = serial;
    }

    /**
     * Default event constructor.
     */
    public SerialDataEvent(Serial serial, byte[] data) {
        this(serial);
        this.cachedData = data;
    }

    /**
     * Get the serial interface instance
     *
     * @return serial interface
     */
    public Serial getSerial(){
        return this.serial;
    }


    /**
     * Get an ASCII string representation of the bytes available in the serial data receive buffer
     *
     * @return ASCII string of data from serial data receive buffer
     * @throws IOException
     */
  

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public String getAsciiString() throws IOException{
		// TODO Auto-generated method stub
		return null;
	}

}
