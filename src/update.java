package robosub;

import java.io.IOException;

//import org.apache.log4j.Logger;//TODO

/*import com.pi4j.io.serial.Serial;//TODO
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
*/

/**
 * Directly interfaces with Arduino
 * Most of it is commented out since pi4j is only available on pi
 * Be careful when changing anything in this class, espeshally setUp, parseIn, self_test and run
 * @author Dakota
 *
 */
@SuppressWarnings("unused")
class update implements Runnable{//interface with sensors
    static Thread t;
    static int mod = 0;
    static int init = 0;
    static String ard;
    static String port;
    static int br;
    /*static Serial serial;*/
    static String output = "[v";
    static String input = "def";
    static boolean ready = false;
    static int IMU_pitch = 0, IMU_roll = 0, depth = 0, waterLvl = 0, direction = 0;
    static boolean run = false;
    static double[] motor_stop = {0,0,0,0,0,0};
    //private static Logger logger = Logger.getLogger(update.class.getCanonicalName());
    public static double get_depth(){
        return depth;
    }
    public static double getWaterSensor() {
        // TODO Auto-generated method stub
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
    
    @Override
    public void run(){//TODO
    	while(run){
    		mod++;
    		if(mod%1==0 && basic.debug_lvl > 5) System.out.println("Sending packet: "+mod+" as: "+output);
    		try{
        		Thread.sleep(100);
        	}catch(Exception e){
                //debug.error(e.getMessage());
                System.out.println(e);
            }
    	}
    	/*long start = System.currentTimeMillis();
        while(run){
            if(serial.isOpen()){
                try{
                    if(ready && (System.currentTimeMillis() >= start+100)){
                    	start = System.currentTimeMillis();
                        //System.out.println("writing");
                        mod++;
    					if(mod%20==0 && basic.debug_lvl > 5) System.out.println("Sending packet: "+mod+" as: "+output);
                        serial.write(output);
                        Thread.sleep(80);
                    }
                }catch(Exception e){
                    //debug.error(e.getMessage());
                    System.out.println(e);
                }
            } 
        }
        synchronized(t){
            t.notify();
        }*/
    	try{
    		Thread.sleep(100);
    	}catch(Exception e){
            //debug.error(e.getMessage());
            System.out.println(e);
        }
    }
    public static void setUp(){
        ready = true;
        run = true;
        System.out.println("Setting up serial coms...");
        ard = "/dev/ttyACM0";
        port = System.getProperty("serial.port", ard);
        br = Integer.parseInt(System.getProperty("baud.rate", "1200"));
        /*serial = SerialFactory.createInstance();//TODO
        serial.addListener(event -> {
            String payload;
            try {
                payload = event.getAsciiString();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            parseIn(payload);
        });
        System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
        try {
            serial.open(port, br);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }*/
        System.out.println("Port is opened.");
    }
    public static void parseIn(String me){
       if(!me.equals(input)){
    	   init = 2;
    	   //System.out.println("i got something: "+me);
           input = me;
           if(input.length() >= 10 && me.startsWith("Running self test")){
               return;
           }else{
               if(me.split(",").length == 4){
                   IMU_pitch = Integer.parseInt(me.split(",")[0].trim());
                   IMU_roll = Integer.parseInt(me.split(",")[1].trim());
                   depth = Integer.parseInt(me.split(",")[2].trim());
                   waterLvl = Integer.parseInt(me.split(",")[3].trim());
                   direction = Integer.parseInt(me.split(",")[4].trim());
               }else{
                   System.out.println("Bad input from ard: "+me);
                   debug.log("Bad input from ard: "+me);
               }
           }   
       }
    }
    public static void set_motors(double[] x){
        ready = false;
        output = "[n";
        for(int i = 0; i<6; i++){
            output += (int)(x[i]);
            output += ",";
        }
        if(!core.RUN){//dont let motors turn on unless we are running
        	output = "[v";
        }
        ready = true;
    }
    public static boolean self_test(){
    	if(core.no_fill()){
    		return true;
    	}
    	return true;
        /*ready = false;//TODO
        output = "[t";
        int test_num = (int) (Math.random()*122);
        output += test_num;
        output += ",";
        ready = true;
        String good_string = "Running self test: " + test_num;
        //System.out.println("Test string is: "+good_string);
        try{
        	while(init != 2){
                Thread.sleep(5);
        	}
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        if(!input.trim().equalsIgnoreCase(good_string.trim())){
            System.out.println("Bad input from ard on st " + input);
            debug.log_err("Bad input from ard on st " + input);
            return false;
        }else{
            
            return true;
        }*/
    }
    public static void force_out(String i){
        output = i;
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
        	output = "[n0,0,0,0,0,0";
    		Thread.sleep(120);
    		output = "[s1,";
    		//TODO send shut command
    		Thread.sleep(120);
    		//serial.close();//TODO
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
