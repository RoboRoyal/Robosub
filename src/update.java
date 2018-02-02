package robosub;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;


/**
 * Directly interfaces with Arduino
 * A dummy library is avalable for Win, Mac, and Linx when not running on the PI. This
 * will simply print the output packets to a text file. 
 * DO NOT put port these dummy libraries to the PI.
 * Be careful when changing anything in this class, espeshally setUp, parseIn, self_test and run
 * This code is not all that well written. Its not efficient, no error correction, or anything like that.
 * With that said, it is rubbost and never crashes. Also, its pretty darn accurate, even with no error correction.
 * @author Dakota
 *
 */
@SuppressWarnings("unused")
class update implements Runnable{//interface with sensors
	static boolean IS_PI;
    private static Thread t;
    private static int mod = 0;
    static int init = 0;
    private static String ard;
    private static String port;
    private static int br;
    private static Serial serial;
    private static String output = "[v";
    private static String input = "def";
    static boolean ready = false;
    static int IMU_pitch = 0, IMU_roll = 0, IMU_YAW = 0, depth = 0, waterLvl = 0, direction = 0;
    static boolean run = false;
    //static double[] motor_stop = {0,0,0,0,0,0};
    public static boolean logTraffic = true;
    public static boolean useReal = true;
    static int packetNum = 0;
    private static String last = null;
    //private static Logger logger = Logger.getLogger(update.class.getCanonicalName());
    public static double get_depth(){
        return depth;
    }
    public static double getWaterSensor() {
        //checks if we are taking on water
        return waterLvl;
    }
    public static int currentDepth(){
        return depth;
    }
    public static double sonar_dist(double freq){
        return -1;
    }
    public static double sonar_dir(double freq){
        return -1;
    }
    public static double sonar_depth(double freq, double current_depth){
        return -1;
    }
    public static double IMU_roll(){
        return IMU_roll;
    }
    public static double IMU_pitch(){
        return IMU_pitch;
    }
    public static double IMU_yaw(){
    	IMU_YAW = direction;
        return IMU_YAW;
    }
    
    @Override
    public void run(){
    	/*while(run){
    		mod++;
    		if(mod%1==0 && basic.debug_lvl > 5) System.out.println("Sending packet: "+mod+" as: "+output);
    		try{
        		Thread.sleep(100);
        	}catch(Exception e){
                debug.error(e.getMessage());
                System.out.print(e);
            }
    	}*/
    	long start = System.currentTimeMillis();
        while(run){
            if(serial.isOpen()){
                try{
                    if(ready && (System.currentTimeMillis() >= start+100)){
                    	start = System.currentTimeMillis();
                        //System.out.println("writing");
                        mod++;
    					if(mod%20==0 && basic.debug_lvl > 9) System.out.println("Sending packet: "+mod+" as: "+output);
    					if(useReal){
                            serial.write(output);
    					}
    					if(logTraffic){
    						log(output);
    					}
                        Thread.sleep(80);
                    }
                }catch(Exception e){
                    debug.logWithStack("Problem with serial in updater: "+e.getMessage());
                    System.out.print(e);
                }
            } 
        }
        synchronized(t){
            t.notify();
        }
    	try{
    		Thread.sleep(100);
    	}catch(Exception e){
            debug.error(e.getMessage());
            System.out.print(e);
        }
    }
    /**
     * Sets up port and event listener. 
     * @param PI If the computer running program is an actual Pi or not
     */
    public static void setUp(boolean PI){
    	IS_PI = PI;//sets global var
        System.out.println("Setting up serial coms...");
        ard = "/dev/ttyACM0";//defualt location for arduino
        port = System.getProperty("serial.port", ard);//sets port object
        br = Integer.parseInt(System.getProperty("baud.rate", "4800"));//gets baud rate
        serial = SerialFactory.createInstance();//creates serial instance 
        if(IS_PI && useReal){//only adds even listener if a true pi
        	serial.addListener(event -> {//add event listener to get data from port
                String payload;
                try {
                    payload = event.getAsciiString();
                } catch (IOException ioe) {
                    System.out.println("Failed to connect to arduino "+ioe.getMessage());
                    debug.log_err("Failed to connect to arduino "+ioe.getMessage());
                    throw new RuntimeException(ioe);
                }
                parseIn(payload);//parser input from port(Arduino)
            });
        }
        System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
        try {
            serial.open(port, br);//actually opens port
            ready = true;//ready for output
            run = true;//Successfully started port
        } catch (IOException ioe) {
        	System.out.println("Error with opening port");
            throw new RuntimeException(ioe);
        
    }catch(Exception e){
    	System.out.println("Error is: "+e.getMessage());
    	debug.log("Error opening port in update.setUp: "+e);
    }
        System.out.println("Port is opened.");
    }
    public static void force_update_parseIn(String me){
    	parseIn(me);
    }
    /**
     * Called from event listener, parses input from port
     * @param me Data from port
     */
    private static void parseIn(String me){
    	if(basic.logger_lvl > 10) debug.log("String recieved from serial @: "+System.currentTimeMillis()+" : "+me);
       if(!me.equals(input)){
    	   init = 2;
    	   //System.out.println("i got something: "+me);
           input = me;
           if(input.length() >= 10 && me.startsWith("Running self test")){//checks for self test
               return;
           }else{
               if(me.split(",").length == 5){//parses pitch, roll, direction, depth, and water level
                   IMU_pitch = Integer.parseInt(me.split(",")[0].trim());
                   IMU_roll = Integer.parseInt(me.split(",")[1].trim());
                   direction = Integer.parseInt(me.split(",")[2].trim());
                   depth = Integer.parseInt(me.split(",")[3].trim());
                   waterLvl = Integer.parseInt(me.split(",")[4].trim());        
               }else{
                   System.out.println("Bad input from ard: "+me);
                   debug.log("Bad input from ard: "+me);
               }
           }   
       }
    }
    /**
     * Sets up output data for setting motor values
     * @param x Values for each motor
     */
    public static void set_motors(double[] x){
        ready = false;//waits for all values to be set
        String newString;
        newString = "[n";//indicates that this will be setting motor values
        for(int i = 0; i<6; i++){//converts all double motor values to int ascii, seporated by commas
        	newString += (int)(x[i]);
        	newString += ",";
        }
        if(!core.RUN){//dont let motors turn on unless we are running
        	newString = "[v";
        }
        set(newString);//set new string and indicates output is ready
        ready = true;
    }
    
    /**
     * Runs a check to verify connection
     * @return
     */
    public static boolean self_test(){
    	if(core.no_fill() || !IS_PI || useReal){//if no_fill or not an actual PI,
    		return true;//there is no point in actually running the test
    	}
    	//return true;
        ready = false;//prepairs output
        String newString = "[t";//indicates that this is a self test
        int test_num = (int) (Math.random()*121);//11^2
        newString += test_num;
        newString += ",";//sets the rest of the values
        set(newString);
        ready = true;
        String good_string = "Running self test: " + test_num;
        try{
        	while(init != 2){//waits for init for be 2
                Thread.sleep(5);
        	}
        }catch(Exception e){
            System.out.println("This is kinda bad; thread interupt in update: "+e.getMessage());
        }
        if(!input.trim().equalsIgnoreCase(good_string.trim())){//checks validity of self test
            System.out.println("Bad input from ard on st " + input);
            debug.log_err("Bad input from ard on st " + input);
            return false;//no coms = bad
        }else{
        	
            return true;
        }
    }
    public static void force_out(String i){
        set(i);
    }
    public static String force_in(){
        return input;
    }
    public static String ard_force(String meinig) throws InterruptedException{
    	force_out(meinig);
    	Thread.sleep(100);
    	return force_in();
    }
    /**
     * Stops port, shutting down connection from PI to Arduino
     * Stops motors first
     */
    public static void stop(){
    	//motorControle.set_motors(motor_stop);
    	try{
    		//TODO change to base value of motors 
        	set("[n1500,1500,1500,1500,1500,1500");//turns off all motors
    		Thread.sleep(120);
    		set("[s1,");//send shut down
    		//TODO send shut command
    		Thread.sleep(120);//wait for signal to go through
    		serial.close();//close port
    	}catch(Exception e){
    		debug.error(e.getMessage());
    	}
    	if(run){
        	run = false;
        	System.out.println("Connection terminated");
    	}
    }
    public static void force_roll_value(int x){
    	IMU_roll = x;
    }
    public static void force_pitch_value(int x){
    	IMU_pitch = x;
    }
    private static void set(String newOutputString){
    	if(basic.logger_lvl > 10) debug.log("Setting update.output @ : "+System.currentTimeMillis()+" : "+newOutputString);
    	output = newOutputString;
    }
    public static void log(String me) {
		packetNum++;
		if(me.equals(last)){
			return;
		}
		last = me;
		String logFile = "output/SerialOutFile.txt";
		StringBuilder temp = new StringBuilder();
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(logFile),true))) {
			if(packetNum!=0) temp.append("Packet "+packetNum+": ");
			temp.append(me+"\n");
			logOut.write(temp.toString());
		} catch (IOException e) {
			System.out.print("Problem writing to: " + e);
		}finally{/*Finally*/}
	}
    
    
    public void start() {
        if (t == null) {
            t = new Thread(this, "update");
            t.start();
        }else{
       	 debug.logWithStack("Second instance being made: update");
        }
    }
}

//set_debug_lvl 8 init 1 -t 1000 start
//https://github.com/OlivierLD/raspberry-pi4j-samples/blob/master/Arduino.RaspberryPI/src/arduino/raspberrypi/SerialReaderWriter.java
//http://www.lediouris.net/RaspberryPI/Arduino/RPi.read.Arduino/readme.html
//http://bildr.org/2012/03/stable-orientation-digital-imu-6dof-arduino/
