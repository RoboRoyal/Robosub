package robosub;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

@SuppressWarnings("unused")
public class core implements Runnable{
	private static Logger logger = Logger.getLogger(core.class.getCanonicalName());
	static Thread t;
	static boolean RUN = false;
	private static int OVER = 4;
	public static int mode = 10;
	public static boolean INIT = false;
	private static long MAX_TIME = 20000;//mili
	public static boolean running = true;	
	
	static void wait_start(Integer integer) {
		System.out.println();
		System.out.println("Waiting...");
		try{
			Thread.sleep(integer);;
		}catch(Exception e){
			
		}
		logger.info("Wait over, starting");
		basic.start_prog();
	}

	static void shutdown() throws InterruptedException {
		logger.info("Ending...");
		running = false;
		Thread.sleep(1000);
		movable.stop();
		movable.surface();
		Thread.sleep(400);
		sonar.stop();
		motorControle.stop();
		movable.stop_thread();
		Thread.sleep(1000);
		update.stop();
		INIT = false;
	}

	static void runMode(int mode) throws InterruptedException {
		logger.info("Starting mode: "+mode);
		final long end_time = System.currentTimeMillis() + MAX_TIME;
		boolean goal = false;
		int state = 0;
		while(!goal && ((mode > 10) || (status() && System.currentTimeMillis() < end_time))){
			state++;
			if(mode < 0) debug.error("Invalid mode");
			switch(mode){
			case(3):
				System.out.println("Running");
			break;
			case(4):
				goal = test_move(state);
			break;
			case(8):
				Thread.sleep(1000*state);
			break;
			case(2):
			case(12):
				goal = sonar_nav();
			break;
			default:
				//def
			}
			Thread.sleep(90);
			if(basic.debug_lvl>1 && state%10 == 0){
				parser.log("State: "+state);
			}
		}
		if(!status()){
			logger.error("Status failed");
			movable.abort();
		}else if(System.currentTimeMillis() >= end_time){
			logger.error("Time expired");
			movable.abort();
		}else if(goal){
			logger.info("Succsess! Ending prog");
			movable.stop();
			movable.surface();
		}else{
			
		}
		running = false;
	}
	
	private static boolean test_move(int state) throws InterruptedException {
		movable.set_depth(5.0);
		Thread.sleep(100);
		if(state < 100){
			movable.move(0,55);
		}
		if(state == 100){
			movable.turn(90);
			logger.info("Turning");
			Thread.sleep(5000);
		}
		if(state >100 && state<200){
			movable.move(0,55);
		}
		if(state == 200){
			return true;
		}
		return false;
	}
	
	public static boolean test_motors(int state){
		double[] motor_vals = {0.0,0.0,0.0,0.0,0.0,0.0};
		int mot = (int)(state/33);
		motor_vals[mot] = 100;
		motorControle.set_motors(motor_vals);
		if(state > 200){
			return true;
		}
		return false;
	}

	private static boolean status(){//check to make sure everything is OK
		if(update.getWaterSensor() > .5){
			logger.error("Takeing on water; level at: " + update.getWaterSensor());
			return false;//we are taking on water, abort!
		}
		if(Math.abs(update.IMU_pitch()) > 20 || Math.abs(update.IMU_roll()) > 20){
			logger.error("Pitch or roll to great, shutting down for safty;");
			logger.error("pitch: "+update.IMU_pitch()+" roll: "+update.IMU_roll());
			return false;
		}
		//check anything else, tmp, battery level etc.
		return true;
	}

	private static boolean sonar_nav() throws InterruptedException {
		int[] pair1 = {1,2};//freq of two beacons
		sonar.set_target_freq(pair1[0]);
		movable.move(sonar.get_pinger_dir(), sonar.get_pinger_dist());
		movable.set_depth(sonar.get_pinger_depth());
		Thread.sleep(100);
		//locate both beacons
		//aim between them
		//go for it
		return (sonar.get_pinger_dist()<10);
	}
		
	static void init() throws InterruptedException {
		if(!check(4)){
			return;
		}
		logger.info("----Initiating system----");
		Thread.sleep(500);//wait
		update m4 = new update();
		update.setUp();
		m4.start();
		if(update.self_test()){
			logger.info("Successful connection!");
		}else{
			logger.error("Unable to establish connection");
		}
		Thread.sleep(100);
		//set up IO
		sonar me = new sonar();
		me.start();
		Thread.sleep(600);
		motorControle me2 = new motorControle();
		me2.start();
		Thread.sleep(600);
		movable me3 = new movable();
		me3.start();
		Thread.sleep(600);
		INIT = true;
		logger.info("----System sucsessfully initiated----");
	}
	public static boolean check(int over) {
		if(INIT && over < 8){
			System.out.println("Already init");
			logger.error("marker, try setting no_fill_ilv");
			return false;
		}
		if(System.getProperty("sun.arch.data.model").toLowerCase().contains("64")){
			System.out.println("64");
		}else{
			System.out.println("32?");
		}
		boolean good = false;
		if(System.getProperty("os.name").toLowerCase().contains("win")){
			System.out.println("Your OS is not full supported! Motor controle disabled");
			System.out.println("Windows");
			good = false;
		}else if(System.getProperty("os.name").toLowerCase().contains("mac")){
			System.out.println("Your OS is not full supported! Motor controle disabled");
			System.out.println("Mac");
			good = false;
		}else if(System.getProperty("os.name").toLowerCase().contains("ras")){
			good = true;
		}else{
			System.out.println("Not sure if OS is supported, but ill try it");
			good = true;
		}
		if(good && over > 0){
			return true;
		}
		logger.error("Failed to init");
		if(over > 2){
			logger.info("Over ridding fail");
			return true;
		}
		return false;
	}
	public static void set_no_fill(boolean trust){
		if(trust){
			logger.info("I hope you feel good and confident about this....");
			logger.info("For real. Don't mess with this unless you know what it does");
			logger.info("please, i worked really hard on this. A lot of people did. Dont break this");
			logger.info("ill give you a few seconds to reconsider....");
			try{
				Thread.sleep(100000);
			}catch(Exception e){
				return;
			}
			System.out.println("Im doing it");
			OVER = 9;
		}
	}
	public static boolean no_fill(){
		return  (OVER ==9);
	}
	public static void setMaxTime(Integer value) {
		MAX_TIME = value;
	}
	@Override
	public void run() {
		try {
			runMode(mode);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	public void start() {
		if(!RUN){
			RUN = true;
			if (t == null) {
				t = new Thread(this, "core");
				t.start();
			}else{
				logger.error("big problems here");
			}
		}else{
			logger.error("Trying to make second instance of core");
		}
	}
}
