package robosub;

import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Core class handles heart of program and many helper functions. Main function
 * is run_mode that branches off control to subroutine
 * 
 * @author Dakota
 *
 */
@SuppressWarnings("unused")
public class core implements Runnable {
	private static Logger logger = Logger.getLogger(core.class.getCanonicalName());
	static Thread t;
	static boolean RUN = false;//allowed
	public static boolean running = false;//actually running
	static boolean shutOnFinish = true;
	private static int OVER = 4;
	public static int mode = 10;
	public static boolean INIT = false;
	private static long MAX_TIME = 20000;// mili

	static void wait_start(Integer integer) {
		System.out.println();
		System.out.println("Waiting...");
		try {
			Thread.sleep(integer);
			debug.log("Waited " + integer + " now starting");
		} catch (Exception e) {

		}
		logger.info("Wait over, starting");
		basic.start_prog();
	}

	static void shutdown() throws InterruptedException {
		if (basic.debug_lvl > 0)
			debug.logWithStack("System receaved shutdown command, shutting down");
		logger.info("Ending...");
		RUN = false;
		running = false;
		INIT = false;
		Thread.sleep(1000);
		movable.stop();
		movable.surface();
		movable.stop_thread();
		Thread.sleep(400);
		sonar.stop();
		// /motorControle.stop();
		Thread.sleep(200);
		update.stop();
		Thread.sleep(400);
		//t = null;
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
		logger.info("Starting mode: " + mode);
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
			case (10):
				// do nothing for debug
				break;
			case (0):
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
		}
		running = false;//need
		if (!status) {
			logger.error("Status failed");
			debug.log("Status failed");
			abort();
		} else if (System.currentTimeMillis() >= end_time) {
			logger.error("Time expired");
			debug.log_err("Time expired");
			abort();
		} else if (goal) {
			logger.info("Succsess! Ending running");
			debug.log("Succsess! Ending running [goal reached]");
			if(shutOnFinish){
				logger.info("Shutting down; shut on finish");
				debug.log("Shutting down; shut on finish");
				movable.stop();
				movable.surface();
				basic.shutdown();
			}else{
				running = false;//redudency lol
			}
			
		} else {
			logger.info("IDK what happened but program stopped");
			debug.log_err("IDK what happened but program stopped");
		}
		//we are no logging running even if we have permission to
		//we have to wait to be restarted
		running = false;
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
		logger.error("Aborting!");
		debug.log("Aborting!");
		movable.abort();
		try {
			movable.abort();
			basic.shutdown();
		} catch (InterruptedException e) {
			logger.info("Error 76");
			e.printStackTrace();
		}
		try {
			Exception e = new Exception("Print me");
		} catch (Exception e) {
			logger.error("Stack:");
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
			logger.info("Turning");
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
		motorControle.set_motors(motor_vals);
		if (state > 200) {
			return true;
		}
		return false;
	}

	private static boolean status() {// check to make sure everything is OK
		if (update.getWaterSensor() > .5) {
			logger.error("Takeing on water; level at: " + update.getWaterSensor());
			/*if (basic.logger_lvl > 0)
				debug.log("Takeing on water; level at: " + update.getWaterSensor());*/
			debug.log_err("Takeing on water; level at: " + update.getWaterSensor());
			return false;// we are taking on water, abort!
		}
		if (Math.abs(update.IMU_pitch()) > 20 || Math.abs(update.IMU_roll()) > 20) {
			logger.error("Pitch or roll too great, shutting down for safty;");
			logger.error("pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			if (basic.logger_lvl > 0)
				debug.log("pithc/roll too great;");
			if (basic.logger_lvl > 0)
				debug.log("pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			debug.log_err("Bad stable: pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			return false;
		}
		// check anything else, tmp, battery level etc.
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

	static void init() throws InterruptedException {
		if (!check(4)) {
			return;
		}
		if (basic.logger_lvl > 0)
			debug.log("----Initiating system----");
		logger.info("----Initiating system----");
		Thread.sleep(300);// wait
		update m4 = new update();
		update.setUp();
		m4.start();
		if (update.self_test()) {
			logger.info("Successful connection!");
		} else {
			logger.error("Unable to establish connection");
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
		Thread.sleep(600);
		INIT = true;
		logger.info("----System sucsessfully initiated----");
		if (basic.logger_lvl > 0)
			debug.log("Init sucsess");

	}

	public static boolean check(int over) throws InterruptedException {
		if (INIT && over < 8) {
			System.out.println("Already init");
			logger.error("marker, try setting no_fill_ilv");
			Thread.sleep(1200);
			return false;
		}
		if (System.getProperty("sun.arch.data.model").toLowerCase().contains("64")) {
			// System.out.println("64bit CPU detected");
		} else {
			System.out.println("32 bit CPU detected");
		}
		boolean good = false;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			System.out.println("Your OS, WINDOWS, is not full supported! Motor controle disabled");
			// System.out.println("Windows");
			good = false;
		} else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			System.out.println("Your OS, MAC, is not full supported! Motor controle disabled");
			// System.out.println("Mac");
			good = false;
		} else if (System.getProperty("os.name").toLowerCase().contains("ras")) {
			good = true;
		} else {
			System.out.println("Not sure if OS is supported, but ill try it");
			good = true;
		}
		if (System.console() == null) {
			System.out.println("No console");
		}
		System.gc();
		if (good && over > 0) {
			return true;
		}
		logger.error("Failed to init");
		if (over > 2) {
			logger.info("Over ridding fail");
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void set_no_fill(boolean trust) {
		if (trust) {
			logger.info("I hope you feel good and confident about this....");
			logger.info("For real. Don't mess with this unless you know what it does");
			logger.info("please, i worked really hard on this. A lot of people did too. Dont break this");
			logger.info("ill give you a few (10) seconds to reconsider....");
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				return;
			}
			System.out.println("Im doing it");
			OVER = 9;
			System.runFinalizersOnExit(true);
			System.setSecurityManager(System.getSecurityManager());
			if (System.console() == null) {
				System.out.println("No con; GC");
				System.gc();
			}
			try {
				System.setProperty("no_fill", "true");
			} catch (Exception e) {
				logger.error("I told you this was bad");
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
			logger.info("Ya have to INIT and be running before self test ya dingis");
			return false;
		}
		if (!RUN) {
			logger.error("***You must start excecution before test.***");
			logger.info("***Try setting mode to 51 with -m 51. Then start, then test***");
			return false;
		}
		boolean allGood = true;
		if (update.self_test()) {
			logger.info("***Good connection***");
		} else {
			logger.error("***Bad connection***");
			allGood = false;
		}
		logger.info("*** Testing motors. Each motor should turn on, one at a time, for two seconds**");
		logger.info("***If a motor does not turn on, there is a problem***");
		movable.motorTest();
		logger.info("***All motors should have turned on. All motors should now be off***");
		logger.info("***Place sub on level ground. Test will resume in 5 secons***");
		Thread.sleep(5000);
		logger.info("***Starting next test now***");
		if (update.IMU_pitch > 2 || update.IMU_roll > 2) {
			logger.error(
					"***Sub is not reading level; Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
			logger.info("***IMU data is wrong or sub is not level. Please check***");
			allGood = false;
		} else {
			logger.info("***Data looks good!***");
			logger.info("***Exact results are: Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
		}
		logger.info("***Now starting tilt test***");
		logger.info("***Tilt sub aprox 30 degrees. Test will resume in 5 secons***");
		Thread.sleep(5000);
		if (Math.abs(update.IMU_pitch) < 20) {
			logger.error("***Sub is not reading tilt; Pitch: " + update.IMU_pitch + "***");
			logger.info("***IMU data is wrong or sub is not tilted. Please check***");
			allGood = false;
		} else {
			logger.info("***Data looks good!***");
			logger.info("***Exact results are: Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
		}
		logger.info("***Now starting roll test***");
		logger.info("***Roll sub aprox 30 degrees. Test will resume in 5 secons***");
		Thread.sleep(5000);
		if (Math.abs(update.IMU_roll) < 20) {
			logger.error("***Sub is not reading tilt; Pitch: " + update.IMU_roll + "***");
			logger.info("***IMU data is wrong or sub is not tilted. Please check***");
			allGood = false;
		} else {
			logger.info("***Data looks good!***");
			logger.info("***Exact results are: Roll: " + update.IMU_roll + " Pitch: " + update.IMU_pitch + "***");
		}
		logger.info("***Now running extended connection test***");
		int t;
		int suc = 0;
		for (t = 0; t <= 25; t++) {
			if (update.self_test())
				suc++;
		}
		if (suc != t) {
			allGood = false;
			logger.error("***Sub did not pass extended connection test. Check connection.***");
			logger.info("Exact ratio is: " + suc + " sucsesses out of " + t + " trials");
		} else {
			logger.info("***Sub passed extended connection test. Check connection.***");
			logger.info("***Exact ratio is: " + suc + " sucsesses out of " + t + " trials***");
		}
		logger.info("Running status test");
		Thread.sleep(500);
		if (status()) {
			logger.info("Passed status test");
		} else {
			logger.info("Failed status check");
		}
		// TODO
		// add in tests for sonar etc
		if (allGood) {
			logger.info("***Sub passed all tests!***");
		} else {
			logger.info("***Sub didn't pass all tests. Please check it***");
		}
		return allGood;
	}

	public static boolean run_motors() {
		return (System.getProperty("os.name").contains("ras"));
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
				logger.error("big problems here:"+t);
			}
		} else {
			logger.error("Trying to make second instance of core");
		}
	}

	public static void reset() {
		t = null;
		System.gc();
		if (t == null) {
			System.out.println("Didnt work");
		}
	}
}
