package robosub;

import java.io.IOException;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;


/**
 * Directly interfaces with Arduino
 * A dummy library is avalable for Win, Mac, and Linx when not running on the PI. This
 * will simply print the output packets to a text file. 
 * DO NOT put port these dummy libraries to the PI.
 * Be careful when changing anything in this class, espeshally setUp, parseIn, self_test and run
 * @author Dakota
 *
 */
@SuppressWarnings("unused")
class update implements Runnable{//interface with sensors
	static boolean IS_PI;
    static Thread t;
    static int mod = 0;
    static int init = 0;
    static String ard;
    static String port;
    static int br;
    static Serial serial;
    private static String output = "[v";
    static String input = "def";
    static boolean ready = false;
    static int IMU_pitch = 0, IMU_roll = 0, IMU_YAW = 0, depth = 0, waterLvl = 0, direction = 0;
    static boolean run = false;
    static double[] motor_stop = {0,0,0,0,0,0};
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
                        serial.write(output);
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
    public static void setUp(boolean PI){
    	IS_PI = PI;
        ready = true;
        run = true;
        System.out.println("Setting up serial coms...");
        ard = "/dev/ttyACM0";
        port = System.getProperty("serial.port", ard);
        br = Integer.parseInt(System.getProperty("baud.rate", "4800"));
        serial = SerialFactory.createInstance();
        if(PI){
        	serial.addListener(event -> {
                String payload;
                try {
                    payload = event.getAsciiString();
                } catch (IOException ioe) {
                    System.out.println("Failed to connect to arduino "+ioe.getMessage());
                    debug.log_err("Failed to connect to arduino "+ioe.getMessage());
                    throw new RuntimeException(ioe);
                }
                parseIn(payload);
            });
        }
        System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
        try {
            serial.open(port, br);
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
    private static void parseIn(String me){
    	if(basic.logger_lvl > 10) debug.log("String recieved from serial @: "+System.currentTimeMillis()+" : "+me);
       if(!me.equals(input)){
    	   init = 2;
    	   //System.out.println("i got something: "+me);
           input = me;
           if(input.length() >= 10 && me.startsWith("Running self test")){
               return;
           }else{
               if(me.split(",").length == 5){
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
    public static void set_motors(double[] x){
        ready = false;
        String newString;
        newString = "[n";
        for(int i = 0; i<6; i++){
        	newString += (int)(x[i]);
        	newString += ",";
        }
        if(!core.RUN){//dont let motors turn on unless we are running
        	newString = "[v";
        }
        set(newString);
        ready = true;
    }
    public static boolean self_test(){
    	if(core.no_fill() || !IS_PI){//if no fill or not an actual PI,
    		return true;//there is no point in actually running the test
    	}
    	//return true;
        ready = false;
        String newString = "[t";
        int test_num = (int) (Math.random()*121);//11^2
        newString += test_num;
        newString += ",";
        set(newString);
        ready = true;
        String good_string = "Running self test: " + test_num;
        //System.out.println("Test string is: "+good_string);
        try{
        	while(init != 2){
                Thread.sleep(5);
        	}
        }catch(Exception e){
            System.out.println("This is kinda bad; thread interupt in update: "+e.getMessage());
        }
        if(!input.trim().equalsIgnoreCase(good_string.trim())){
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
    public static void stop(){
    	//motorControle.set_motors(motor_stop);
    	try{
        	set("[n1500,1500,1500,1500,1500,1500");
    		Thread.sleep(120);
    		set("[s1,");
    		//TODO send shut command
    		Thread.sleep(120);
    		serial.close();
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
