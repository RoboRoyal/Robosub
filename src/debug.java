package robosub;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//import org.apache.log4j.Logger;

/*import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;*/

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
/**
* Helper class for extra debugging 
* @author Dakota
*
*/
class debug{//blinks LED and logs errors
	//private static Logger logger = Logger.getLogger(debug.class.getCanonicalName());
	public static void blink(){
		System.out.println("Blink");
	}
	public static void test(String me){
		/*Logger logger = Logger.getLogger("logger");
	    Layout layout = new PatternLayout();
		StringWriter stringWriter = new StringWriter();
	    WriterAppender writerAppender = new WriterAppender(layout, stringWriter);
	    logger.addAppender(writerAppender);
	    System.out.print(me);*/
	    //System.out.println(stringWriter.toString());
	}
	public static void print(String str){
		log(str);
		System.out.println(str);
	}
	public static void error(String str){
		movable.surface();
		movable.surface();
		log_err("Aborting: Error:"+str);
		blink();
		blink();
		System.out.print("Error: "+str);
		
		core.abort();
	}
	public static void log_err(String me){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
	    me += " - From thread: "+Thread.currentThread()+ " at time: "+dateFormat.format(date);
	    //System.out.println("This is it: "+me);
		try {
	        throw new IOException("Thrown by debug manager");
	    }
	    catch (IOException e) {
	    	StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	e.printStackTrace(pw);
	    	String sStackTrace = sw.toString();
	    	log("Error reported to debug manager: "+me);
	    	log("Stack trace: "+sStackTrace+"\n");
	    }
	}
	public static void logWithStack(String me){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
	    me += "\n - From thread: "+Thread.currentThread()+ " at time: "+dateFormat.format(date);
		log("[Log with stack] Stack trace: "+getStackTrace()+"\n"+me);
	}
	public static void log(String me) {
		String logFile = "output/logFile.txt";
		StringBuilder temp = new StringBuilder();
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(logFile),true))) {
			temp.append(me+"\n");
			logOut.write(temp.toString());
		} catch (IOException e) {
			System.out.print("Problem writing to log file(log): " + e);
		}finally{/*Finally*/}
	}
	public static void del__log__(boolean sure){
		String logFile = "output/logFile.txt";
		StringBuilder temp = new StringBuilder();
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(logFile)))) {
			if(sure) temp.append("\n");
			if(sure) logOut.write(temp.toString());
		} catch (IOException e) {
			System.out.print("Problem writing to log file(del_log_: " + e);
		}finally{/*Finally*/}
	}
	public static String getStackTrace(){
		try {
	        throw new IOException("Thrown by debug manager");
	    }
	    catch (IOException e) {
	    	StringWriter sw = new StringWriter();
	    	PrintWriter pw = new PrintWriter(sw);
	    	e.printStackTrace(pw);
	    	return sw.toString();
	    }
	}
}
