package robosub;

import java.util.Scanner;
import com.pi4j.system.SystemInfo;

//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Logger;

/**
 * Core class handles heart of program and many helper functions. Main function
 * is run_mode that branches off control to subroutine
 * 
 * @author Dakota A.
 *
 */
@SuppressWarnings("unused")
public class core implements Runnable {
	//private static Logger logger = Logger.getLogger(core.class.getCanonicalName());
	static Thread t;
	static boolean RUN = false;//allowed
	public static boolean running = false;//actually running
	static boolean shutOnFinish = true;
	private static int OVER = 4;
	public static int mode = 10;
	public static boolean INIT = false;
	private static boolean PI = false;
	private static long MAX_TIME = 20000;// mili

	static void wait_start(Integer integer) {
		System.out.println();
		System.out.println("Waiting...");
		try {
			Thread.sleep(integer);
			debug.log("Waited " + integer + " now starting");
		} catch (Exception e) {

		}
		System.out.println("Wait over, starting");
		basic.start_prog();
	}
	
	static void shutdown(String why) throws InterruptedException {

	//static void shutdown() throws InterruptedException {
		if (basic.debug_lvl > 0)
			debug.logWithStack("System receaved shutdown command becuase: "+why);
		System.out.println("Ending...");
		RUN = false;
		running = false;
		INIT = false;
		Thread.sleep(100);//chiil for a sec
		movable.stop();//stop and surface sub
		movable.surface();
		Thread.sleep(400);//give time for command to go through
		movable.stop_thread();//stop movable
		Thread.sleep(400);
		sonar.stop();//stop sonar
		// /motorControle.stop();
		Thread.sleep(200);
		update.stop();//give update time to give message then stop it
		Thread.sleep(400);
		//t = null; //may want to uncomment; helps reset thread when it works
	}

	/**
	 * Core module that runs everything. Methods break off from this one to
	 * control the sub then return to this one to check up and make sure
	 * everything is OK
	 * 
	 * @param mode
	 * @throws InterruptedException
	 */
	static void runMode(int mode) throws InterruptedException {
		System.out.println("Starting mode: " + mode);
		final long end_time = System.currentTimeMillis() + MAX_TIME;
		boolean goal = false;
		boolean status = status();
		int step = 0;
		int state = 0;
		while (!goal && ((mode >= 10) || (status && System.currentTimeMillis() < end_time))) {
			step++;
			if (mode < 0)
				debug.error("Invalid mode");
			switch (mode) {
			case (0):
			case (10):
				// do nothing for debug
				break;
			case (1):
				if(step == 20)
					goal = true;
			break;
			case (3):
				System.out.println("Running");
				break;
			case (4):
				goal = test_move(step);
				break;
			case (50):
				goal = test2(step);
				break;
			case (6):
			case (16):
				state = schedule(state);
				break;
			case (8):
				Thread.sleep(1000 * step);
				break;
			case (5):
				movable.move();
				if (step == 20) {
					movable.moveInDir((update.direction + 180) % 360);
				}
				if (step == 40) {
					goal = true;
				}
			case (2):
			case (12):
				goal = sonar_nav();
				break;
			default:
				// def
			}
			Thread.sleep(90);
			if (basic.debug_lvl > 5 && step % 10 == 0) {
				debug.log("Step: " + step);
			}
			status = status();
			if(!status && step %10 == 0 && mode < 10){//if we arent checking for code, still nice to know if it fails
				debug.log("Status failed");
			}
		}
		running = false;//need
		if (!status) {
			System.out.println("Status failed");
			debug.log("Status failed");
			abort();
		} else if (System.currentTimeMillis() >= end_time) {
			System.out.println("Time expired");
			debug.log("Time expired");
			abort();
		} else if (goal) {
			System.out.println("Succsess! Ending running");
			debug.log("Succsess! Ending running [goal reached]");
			if(shutOnFinish){
				System.out.println("Shutting down; shut on finish");
				debug.log("Shutting down; shut on finish");
				movable.stop();
				movable.surface();
				basic.shutdown("Sub reached goal");
			}else{
				running = false;//redudency lol
			}
			
		} else {
			System.out.println("IDK what happened but program stopped");
			debug.log_err("IDK what happened but program stopped");
		}
		//we are no logging running even if we have permission to
		//we have to wait to be restarted
		running = false;
		//Stop for now
		movable.stop();
	}

	private static boolean test2(int step) {
		boolean goal = false;
		if (step == 10) {
			movable.set_raw_input(2, 2);
			System.out.println(movable.print());
		}
		if (step == 20) {
			movable.moveInDir(50);
			System.out.println(movable.print());
		}
		if (step == 30) {
			movable.face(60);
			System.out.println(movable.print());
		}
		if (step == 40) {
			System.out.println(movable.print());
		}
		if (step == 40) {
			goal = true;
		}
		return goal;
	}

	public static void abort() {
		System.out.println("Aborting!");
		debug.log_err("Aborting!");
		movable.abort();
		try {
			movable.abort();
			String why ="Sub aborted";
			if(!status()){
				System.out.println("Status failed");
				 why += ": statsu failed";
			}
			basic.shutdown(why);
		} catch (InterruptedException e) {
			System.out.println("Error 76");
			e.printStackTrace();
		}
		try {
			Exception e = new Exception("Print me");
		} catch (Exception e) {
			System.out.println("Stack:");
			e.printStackTrace();
		}
	}

	/**
	 * The schedule for competition or practice. Example: follow fine, do thing,
	 * follow pingers, do thing n
	 * 
	 * @param state
	 *            current
	 * @return next state to go to
	 */
	private static int schedule(int state) {// TODO
		switch (state) {
		case (0):
			// follow line
			// if(got to next obstical)
			// return state+1
			break;
		case (1):
			// do objective
			// return state+1
			break;
		case (2):
			// follow line
			// if(got to next obstical)
			// return state+1
			break;
		case (3):
			// go to pinger
			// if(got to next obstical)
			// return state+1
			break;
		case (4):
			// do objective
			// return state+1
			break;
		case (5):
			// go to pinger
			// if(got to next obstical)
			// return state+1
			break;
		case (6):
			// do objective
			// return state+1
			break;
		}
		return 0;
	}

	private static boolean test_move(int state) throws InterruptedException {
		movable.set_depth(5.0);
		Thread.sleep(100);
		if (state < 90) {
			movable.move();
		}
		if (state == 90) {
			movable.face_R(90);
			System.out.println("Turning");
		}
		if (state > 110 && state < 200) {
			movable.moveInDir_R(90);
		}
		if (state == 200) {
			return true;
		}
		return false;
	}

	public static boolean test_motors(int state) {
		double[] motor_vals = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		int mot = (int) (state / 33);
		motor_vals[mot] = 100;
		try {
			motorControle.set_motors(motor_vals);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if (state > 200) {
			return true;
		}
		return false;
	}

	private static boolean status() {// check to make sure everything is OK
		if (update.getWaterSensor() > .5) {
			System.out.println("Takeing on water; level at: " + update.getWaterSensor());
			/*if (basic.logger_lvl > 0)
				debug.log("Takeing on water; level at: " + update.getWaterSensor());*/
			debug.log_err("Takeing on water; level at: " + update.getWaterSensor());
			return false;// we are taking on water, abort!
		}
		if (Math.abs(update.IMU_pitch()) > 20 || Math.abs(update.IMU_roll()) > 20) {
			System.out.println("Pitch or roll too great, shutting down for safty;");
			System.out.println("pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			if (basic.logger_lvl > 0)
				debug.log("pithc/roll too great;");
			if (basic.logger_lvl > 0)
				debug.log("pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			debug.log_err("Bad stable: pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			return false;
		}
		//TODO check anything else, tmp, battery level etc.
		if(SystemInfo.getCpuTemperature() > 90){
			System.out.println("Things are getting hot!");
			debug.log("CPU temp hot: "+SystemInfo.getCpuTemperature());
		}
		if(SystemInfo.getCpuTemperature() > 98){
			System.out.println("Way too hot");
			debug.log("CPU temp too hott: "+SystemInfo.getCpuTemperature());
			parser.parse("exit");
			return false; //this can be removed if you don't care about this little CPU that tryed
		}
		return true;
	}

	private static boolean sonar_nav() throws InterruptedException {
		int[] pair1 = { 1, 2 };// freq of two beacons
		sonar.set_target_freq(pair1[0]);
		movable.move(sonar.get_pinger_dir(), sonar.get_pinger_dist());
		movable.set_depth(sonar.get_pinger_depth());
		Thread.sleep(100);
		// locate both beacons
		// aim between them
		// go for it
		return (sonar.get_pinger_dist() < 10);
	}

	//I knbow this is a bit ugly but it works well enough
	static void init() throws InterruptedException {
		try{
			if (!check(4)) {
				return; //invalid to run
			}
			if (basic.logger_lvl > 0)
				debug.log("----Initiating system----");
			System.out.println("----Initiating system----");
			Thread.sleep(300);// wait
			update m4 = new update();
			
			//TODO
			//CHANGE THIS LINE, PLEASE, CHANGE IT
			//FIX THIS LINE FOR THE LOVE OF GOD
			//IT WONT WORK LIKE THIS
			update.setUp(false); //THIS SHOULD BE PI, NOT FALSE
			//CHANGE THIS LINE TO: update.setUp(PI);
			
			m4.start();
			if (update.self_test()) {
				System.out.println("Successful connection!");
			} else {
				System.out.println("Unable to establish connection");
			}
			Thread.sleep(100);
			// set up IO
			sonar me = new sonar();
			me.start();
			Thread.sleep(300);
			// motorControle me2 = new motorControle();
			// me2.start();
			Thread.sleep(300);
			movable me3 = new movable();
			me3.start();
			while(!movable.initiated()){
				Thread.sleep(50);
			}
			Thread.sleep(200);
			INIT = true;
			if(true){
				System.out.println("----System sucsessfully initiated----");
				if (basic.logger_lvl > 0)
					debug.log("Init sucsess");
			}else{
				System.out.println("Unkown problem occured");
			}
		}catch(Exception elo){
			debug.print("Error occured: "+elo);
			debug.log_err(elo.getMessage());
		}
	}

	public static boolean check(int over) throws InterruptedException {
		if (INIT && over < 8) {
			System.out.println("Already init");
			System.out.println("marker, try setting no_fill_ilv");
			Thread.sleep(1200);
			return false;
		}
		if (System.getProperty("sun.arch.data.model").toLowerCase().contains("64")) {
			System.out.println("64bit CPU detected");
		} else {
			System.out.println("32 bit CPU detected");
		}
		boolean good = false;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			System.out.println("Your OS, WINDOWS, is not full supported! Motor controle disabled");
			//System.out.println("Windows");
			System.out.println(System.getProperty("os.arch"));
			good = false;
			PI = false;
		} else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			System.out.println("Your OS, MAC, is not full supported! Motor controle disabled");
			// System.out.println("Mac");
			good = false;
			PI = false;
		} else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
			System.out.println("Linux");
			if(System.getProperty("os.arch").toLowerCase().contains("arm")){
				good = true;
				PI = true;
			}
		} else {
			System.out.println("Not sure if OS is supported, but ill try it");
			good = true;
			PI = true;
		}
		if (System.console() == null) {
			System.out.println("No console");
		}
		System.gc();
		if (good && over > 0) {
			return true;
		}
		System.out.println("Failed to init");
		if (over > 2) {
			System.out.println("Over ridding fail");
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void set_no_fill(boolean trust) {
		if (trust) {
			System.out.println("I hope you feel good and confident about this....");
			System.out.println("For real. Don't mess with this unless you know what it does");
			System.out.println("please, i worked really hard on this. A lot of people did too. Dont break this");
			System.out.println("ill give you a few (10) seconds to reconsider....");
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				return;
			}
			System.out.println("Im doing it");
			OVER = 9;
			System.runFinalizersOnExit(true);
			if(System.getSecurityManager() == null){
				SecurityManager me = new SecurityManager();
				System.setSecurityManager(me);
				System.out.println("new secM @: "+me);
			}
			if (System.console() == null) {
				System.out.println("No con; GC");
				System.gc();
			}
			try {
				System.setProperty("no_fill", "true");//this is not a good thing to do
			} catch (Exception e) {
				System.out.println("I told you this was bad");
				abort();
			}
		}
	}

	/**
	 * Checks if no_fill is set
	 * 
	 * @return
	 */
	public static boolean no_fill() {
		return (OVER == 9);
	}

	/**
	 * Sets the maximum running time for the program. Doesn't apply to modes >=
	 * 10.
	 * 
	 * @param value
	 */
	public static void setMaxTime(Integer value) {
		MAX_TIME = value;
	}

	/**
	 * Runs a self test on the sub checking major functionality. This includes
	 * IMU data, motor control, and other tests. Parser command: test
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public static boolean selfTest() throws InterruptedException {
		if (!INIT) {
			System.out.println("Ya have to INIT and be running before self test ya dingis");
			return false;
		}
		if (!RUN) {
			System.out.println("***You must start excecution before test.***");
			System.out.println("***Try setting mode to 51 with -m 51. Then start, then test***");
			return false;
		}
		boolean allGood = true;
		if (update.self_test()) {
			System.out.println("***Good connection***");
		} else {
			System.out.println("***Bad connection***");
			allGood = false;
		}
		//TODO check battery status and temp
		System.out.println("*** Testing motors. Each motor should turn on, one at a time, for two seconds**");
		System.out.println("***If a motor does not turn on, there is a problem***");
		movable.motorTest();
		System.out.println("***All motors should have turned on. All motors should now be off***");
		System.out.println("***Depth meter is reading "+ update.get_depth()+" units. Does this seem right?");
		System.out.println("***Running core.status() test: "+status()+" ***");
		if(!status()){
			allGood = false;
		}
		System.out.println("***Place sub on level ground. Test will resume in 5 secons***");
		Thread.sleep(5000);
		System.out.println("***Starting next test now***");
		if (update.IMU_pitch > 2 || update.IMU_roll > 2) {
			System.out.println(
					"***Sub is not reading level; Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
			System.out.println("***IMU data is wrong or sub is not level. Please check***");
			allGood = false;
		} else {
			System.out.println("***Data looks good!***");
			System.out.println("***Exact results are: Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
		}
		System.out.println("***Now starting tilt test***");
		System.out.println("***Tilt sub aprox 30 degrees. Test will resume in 5 secons***");
		Thread.sleep(5000);
		if (Math.abs(update.IMU_pitch) < 20) {
			System.out.println("***Sub is not reading tilt; Pitch: " + update.IMU_pitch + "***");
			System.out.println("***IMU data is wrong or sub is not tilted. Please check***");
			allGood = false;
		} else {
			System.out.println("***Data looks good!***");
			System.out.println("***Exact results are: Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
		}
		System.out.println("***Now starting roll test***");
		System.out.println("***Roll sub aprox 30 degrees. Test will resume in 5 secons***");
		Thread.sleep(5000);
		if (Math.abs(update.IMU_roll) < 20) {
			System.out.println("***Sub is not reading tilt; Pitch: " + update.IMU_roll + "***");
			System.out.println("***IMU data is wrong or sub is not tilted. Please check***");
			allGood = false;
		} else {
			System.out.println("***Data looks good!***");
			System.out.println("***Exact results are: Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
		}
		System.out.println("***Now running extended connection test***");
		int t;
		int suc = 0;
		for (t = 0; t < 25; t++) {
			if (update.self_test())
				suc++;
		}
		if (suc != t) {
			allGood = false;
			System.out.println("***Sub DID NOT pass extended connection test. Check connection.***");
			System.out.println("Exact ratio is: " + suc + " sucsesses out of " + t + " trials");
		} else {
			System.out.println("***Sub passed extended connection test. Check connection.***");
			System.out.println("***Exact ratio is: " + suc + " sucsesses out of " + t + " trials***");
		}
		System.out.println("Running status test");
		Thread.sleep(500);
		if (status()) { //it runs this twice on purpose
			System.out.println("Passed status test");
		} else {
			System.out.println("Failed status check");
		}
		// TODO
		// add in tests for sonar etc
		if (allGood) {
			System.out.println("***Sub passed all tests!***");
		} else {
			System.out.println("***Sub didn't pass all tests. Please check it***");
			debug.log("Sub did not pass full self test");
		}
		return allGood;
	}

	public static boolean run_motors() {
		return (System.getProperty("os.name").contains("ras"));//this is out of date, i mean wrong
		//turns out raspian is identified as just Linux from JVM
		
	}

	@Override
	public void run() {
		try {
			running = true;
			RUN = true;
			runMode(mode);
		} catch (InterruptedException e) {
			debug.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public void start() {
		if (!RUN) {
			RUN = true;
			if (t == null) {
				t = new Thread(this, "core");
				t.start();
			} else {
				System.out.println("big problems here:"+t);
			}
		} else {
			System.out.println("Trying to make second instance of core");
		}
	}

	public static void reset() {
		t = null;
		System.gc();
		if (t != null) {
			System.out.println("Didnt work");
		}
	}
	public static String telemitry(){
		String all_info = "\nTelemitry; \n\tYaw(direction): "+update.IMU_yaw()+"\n\tPitch: ";
		all_info += update.IMU_pitch()+"\n\tRoll: "+update.IMU_roll();
		all_info += "\n\tDepth: "+update.get_depth();
		
		return all_info;
	}

	public static String info() {
		String all_info = "System temp: "+SystemInfo.getCpuTemperature();
		all_info += "\nSystem Status: "+status();
		all_info += "\nInit, Run & Connected: "+INIT+", "+RUN + ", "+update.self_test();
		all_info += "\nMode: " + mode + " Stabalized: "+movable.isStabilize();
		all_info += "\nMotor Values: "+movable.print_motor_values();
		all_info += "\nTelemitry; \n\tYaw(direction): "+update.IMU_yaw()+"; "+movable.getTarget_direction();
		all_info += "\n\tPitch: "+update.IMU_pitch();
		all_info += "\n\tRoll: "+update.IMU_roll();
		all_info += "\n\tDepth: "+update.get_depth()+"; "+movable.getTarget_depth();
		
		return all_info;
	}
}
