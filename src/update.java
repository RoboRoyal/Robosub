package robosub;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

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
 * With that said, it is robust enough and never crashes. Also, its pretty darn accurate, even with no error correction.
 * @author Dakota
 *
 */
@SuppressWarnings("unused")
class update implements Runnable{//interface with sensors
	private static int delayTime = 100;//ms
	static boolean IS_PI;
    private static Thread t;
    private static int mod = 0;
    static int init = 0;
    private static final String ard = "/dev/ttyACM0";
    //private static int ard_num = 0;
    //private static final String port = System.getProperty("serial.port", ard);
    private static final String port = ard;
    private static final int br = 9600;//Integer.parseInt(System.getProperty("baud.rate", "9600"));
    private static Serial serial;
    private static String output = "[v";
    private static String input = "def";
    static boolean ready = false;
    static int IMU_pitch = 0, IMU_roll = 0, IMU_YAW = 0, depth = 0, waterLvl = 0;
    static boolean run = false;
    public static boolean logTraffic = true;//logs the packets sent to Arduino
    public static boolean useReal = true;//if true, actually sends packets. Otherwise, its just a test
    private static int packetNum = 0;//, packetNumIn = 0;//keeps track of packets
    private static String last = null;
	private static boolean logSerialIn = true;
	private static String last2;
    public static int get_depth(){
        return depth;
    }
    public static int getWaterSensor() {
        //checks if we are taking on water
        return waterLvl;
    }
    public static int currentDepth(){
        return depth;
    }
    /*public static double sonar_dist(double freq){
        return -1;
    }
    public static double sonar_dir(double freq){
        return -1;
    }
    public static double sonar_depth(double freq, double current_depth){
        return -1;
    }*/
    public static int IMU_roll(){
        return IMU_roll;
    }
    public static int IMU_pitch(){
        return IMU_pitch;
    }
    public static int IMU_yaw(){
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
            if(serial.isOpen() || !useReal){
                try{
                    if(ready && (System.currentTimeMillis() >= start+delayTime)){
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
    					if(!IS_PI){//reads fake input data from input/SerialInFile.txt
    						fakeReadIn();
    					}
                        Thread.sleep((long) (delayTime*.8));
                    }
                }catch(Exception e){
                    debug.logWithStack("Problem with serial in updater: "+e.getMessage());
                    System.out.print(e); 
                }
            } 
        }
        try{if(serial.isOpen()) serial.close();}catch(Exception e){System.out.println("Error closing serial: "+e);}
        //synchronized(t){t.notify();}
    	try{
    		Thread.sleep(delayTime);
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
        //ard = "/dev/ttyACM0";//defualt location for arduino
        //port = System.getProperty("serial.port", ard);//sets port object
        //br = Integer.parseInt(System.getProperty("baud.rate", "9600"));//gets baud rate
        serial = SerialFactory.createInstance();//creates serial instance 
        if(IS_PI && useReal){//only adds even listener if a true pi
        	serial.addListener(event -> {//add event listener to get data from port
                String payload = "";
                try {
                	//Thread.sleep(5);
                    payload = event.getAsciiString();
                } catch (IOException ioe) {
                    System.out.println("Failed to connect to arduino "+ioe.getMessage());
                    debug.log_err("Failed to connect to arduino "+ioe.getMessage());
                    throw new RuntimeException(ioe);
                } //catch (InterruptedException e) {System.out.println("Error waiting for in update.setUp(): "+e);}
                parseIn(payload);//parser input from port(Arduino)
            });
        }
        System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
        if(logTraffic){
        	del();//deletes last serial log file
        	log("Opening port [" + port + ":" + Integer.toString(br) + "]");
        }
        try {
            if(useReal) serial.open(port, br);//actually opens port
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
    	if(logTraffic){//TODO have seperate var for log in/log out?
    		logIn(me);
    	}
       if(!me.equals(input)){
    	   try{
    		   Thread.sleep(5);
    	   }catch(Exception e){}
    	   init = 2;
    	   //System.out.println("i got something: "+me);
           input = me;
           if(input.length() >= 10 && me.startsWith("Running self test")){//checks for self test
               return;
           }else{
        	   String[] meSplit = me.split(",");
               if(meSplit.length == 5){//parses pitch, roll, direction, depth, and water level
                   IMU_pitch = Integer.parseInt(meSplit[0].trim());
                   IMU_roll = Integer.parseInt(meSplit[1].trim());
                   IMU_YAW = Integer.parseInt(meSplit[2].trim());
                   depth = Integer.parseInt(meSplit[3].trim());
                   waterLvl = Integer.parseInt(meSplit[4].trim());        
               }else{
                   debug.print("Bad input from ard: "+me);
                   debug.print("When sent: "+output);
               }
           }   
       }
    }
    /**
     * Sets up output data for setting motor values
     * @param motor_vals Values for each motor
     */
    public static void set_motors(int[] motor_vals){
    	boolean tmp = ready;
        ready = false;//waits for all values to be set
        String newString;
        newString = "[n";//indicates that this will be setting motor values
        for(int i = 0; i<6; i++){//converts all motor values to int ascii, separated by commas
        	newString += (motor_vals[i]);
        	newString += ",";
        }
        if(!core.RUN){//dont let motors turn on unless we are running
        	newString = "[v";
        }
        set(newString);//set new string and indicates output is ready
        ready = tmp;
    }
    
    /**
     * Runs a check to verify connection
     * @return
     */
    public static boolean self_test(){
    	if(core.no_fill() || !IS_PI || !useReal){//if no_fill or not an actual PI,
    		return true;//there is no point in actually running the test
    	}
    	//return true;
        ready = false;//prepairs output
        String newString = "[t ";//indicates that this is a self test
        int test_num = (int) (Math.random()*121);//11^2
        newString += test_num;
        newString += ",";//sets the rest of the values
        try {
			Thread.sleep(delayTime - 20);
		} catch (InterruptedException e1) {System.out.println("Interupt in delay of update.selftest(): "+e1);}
        set(newString);
        ready = true;
        String good_string = "Running self test: " + test_num;
        init = 1;
        try{
        	while(init != 2){//waits for init for be 2
                Thread.sleep(5);
        	}
        }catch(Exception e){
            System.out.println("This is kinda bad; thread interupt in update.selfTest: "+e.getMessage());
        }
        if(!input.trim().equalsIgnoreCase(good_string.trim())){//checks validity of self test
            System.out.println("Bad input from ard on st: " + input);
            debug.log("Bad input from ard on self test: " + input);
            return false;//no coms = bad
        }else{
        	//Good test
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
    	if(!serial.isOpen()){
    		System.out.println("Port not open, cant close");
    	}else{
    		try{
    			set("[n"+movable.base_speed+","+movable.base_speed+","+movable.base_speed+","+movable.base_speed+","+movable.base_speed+","+movable.base_speed+",");
    			//set("[n1500,1500,1500,1500,1500,1500,");//turns off all motors
        		Thread.sleep(120);
        		set("[s1,");//send shut down
        		//TODO send shut command
        		Thread.sleep(120);//wait for signal to go through
        		if(useReal) serial.close();//close port
        	}catch(Exception e){
        		System.out.println("Error in update.stop(): "+e);
        		debug.error(e.getMessage());
        	}
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
	public static int getDelayTime() {
		return delayTime;
	}
	public static void setDelayTime(int delayTime) {
		update.delayTime = delayTime;
	}
    private static void set(String newOutputString){
    	if(basic.logger_lvl > 10) debug.log("Setting update.output @ : "+System.currentTimeMillis()+" : "+newOutputString);
    	output = newOutputString;
    }
    public static void log(String me) {//TODO make one writer and close it at the end
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
			System.out.print("Problem writing Serial file in update.log(): " + e);
		}finally{/*Finally*/}
		//Now read in file from serial in file
	}
    
    public static void logIn(String me) {//TODO make one writer and close it at the end
		if(me.equals(last2)){//Dont write duplicates
			return;
		}
		last2 = me;
		String logFile = "output/SerialLogIn.txt";
		StringBuilder temp = new StringBuilder();
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(logFile),true))) {
			temp.append("Packet "+packetNum+": ");
			temp.append(me+"\n");
			logOut.write(temp.toString());
		} catch (IOException e) {
			System.out.print("Problem writing Serial file in update.logIn(): " + e);
		}finally{/*Finally*/}
		
	}
    
    public static void fakeReadIn(){
    	String fileIn = "input/SerialInFile.txt";
    	try (Scanner in = new Scanner(new File(fileIn))){
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				if(!line.startsWith("#") && line.length() >  8)
					force_update_parseIn(line);
			}
		}catch(Exception e){
			System.out.println("Error reading in SerialInFile file: "+e);
		} 
    }
    public static void del() {
		String logFile = "output/SerialOutFile.txt";
		try (Writer logOut = new FileWriter(new File(logFile))) {
			logOut.write(" ");
		} catch (IOException e) {
			System.out.print("Problem deleting file in update.del(), Serial out file: " + e);
		}finally{/*Finally*/}
		
		String logFile_in = "output/SerialLogIn.txt";
		try (Writer logOut2 = new FileWriter(new File(logFile_in))) {
			logOut2.write(' ');
		} catch (IOException e) {
			System.out.print("Problem deleting file in update.del(), Serial in file: " + e);
		}finally{/*Finally*/}
		
	}
    public static String ToString(){
    	String me = "Pitch: " + IMU_pitch;
    	me += "; Yaw: " + IMU_YAW;
    	me += "; Roll: " + IMU_roll;
    	me += "; Water: " + waterLvl;
    	me += "; Depth: " + depth;
		return me;
    }
    public static String portInfo(){
    	return ard+"; "+br;
    }
    public static void resetPort(){
    	try{if(serial.isOpen()) serial.close();}catch(Exception e){System.out.println("Error in update.resetPort(): "+e);}
    }
    
    public void start() {
        if (t == null) {
            t = new Thread(this, "update");
            t.start();
        }else{
       	 debug.logWithStack("Second instance being made in update: " + t.getName());
        }
    }
}

//set_debug_lvl 8 init 1 -t 1000 start
//https://github.com/OlivierLD/raspberry-pi4j-samples/blob/master/Arduino.RaspberryPI/src/arduino/raspberrypi/SerialReaderWriter.java
//http://www.lediouris.net/RaspberryPI/Arduino/RPi.read.Arduino/readme.html
//http://bildr.org/2012/03/stable-orientation-digital-imu-6dof-arduino/