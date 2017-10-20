package robosub;

import java.io.IOException;

import org.apache.log4j.Logger;

/*//TODO
 *https://github.com/Pi4J/pi4j/blob/master/pi4j-example/src/main/java/BlinkGpioExample.java
 * import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
 */

//@SuppressWarnings("unused")
class debug{//blinks LED and logs errors
	private static Logger logger = Logger.getLogger(debug.class.getCanonicalName());
	public static void blink(){
		System.out.println("Blink");
	}
	public static void error(String str){
		movable.surface();
		movable.surface();
		parser.log("Aborting: Error:"+str);
		blink();
		blink();
		System.out.println("Error: "+str);
		logger.error("me");
		try {
	        throw new IOException();
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	    }
		movable.abort();
	}
}
