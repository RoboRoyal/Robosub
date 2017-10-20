package robosub;

import org.apache.log4j.Logger;

class movable implements Runnable{
	Thread t;
	static boolean run = false;
	static boolean init = false;
	private static Logger logger = Logger.getLogger(movable.class.getCanonicalName());
	private static double target_depth = 0;
	private static double cur_direction = 0;
	private static double target_direction = 0;
	public static void stop_thread(){run = false;}
	public static void set_depth(double new_depth){
		target_depth = new_depth;
	}
	public static void set_cur(double i){
		cur_direction = i;
	}
	public static void turn(int i) throws InterruptedException {
		motorControle.set_motors(move(i,60));
		Thread.sleep(5000);
		movable.stop();
	}
	public static double[] stable(){
		return stable(update.get_depth(),target_depth,update.IMU_roll(),update.IMU_pitch());
	}
	
	public static double[] stable(double current_depth, double target_depth, double roll, double pitch){
		int K=10; //constant multiplier for calibration
		Double FLM=1.0, FRM=1.0, BLM=1.0, BRM=1.0; //front left motor, etc. final value is motor speed
		int base = 100; //base downward force to cancel out positive buoyancy 
		int C1=1,C2=1,C3=1;
		
		int depth_multiplier = (int) ((target_depth - current_depth)*C1); //C1, C2, C3 used for calibration 
		int pitch_multiplier = (int) ((0 - pitch)*C2); //can replace 0 with number given by IMU when level 
		int roll_multiplier = (int) ((0 - roll)*C3);

		FLM *= (double) (K*(base + depth_multiplier + pitch_multiplier + roll_multiplier));
		FRM *= (double) K*(base + depth_multiplier + pitch_multiplier - roll_multiplier);
		BLM *= (double) K*(base + depth_multiplier - pitch_multiplier + roll_multiplier);
		BRM *= (double) K*(base +depth_multiplier - pitch_multiplier - roll_multiplier);

		double[] motors = {FLM,FRM,BLM,BRM};
		return motors;
	}
	public static boolean turn_correct(double dir){
		if(Math.abs(target_direction-dir) < 4){
			return true;
		}
		return false;
	}
	public static void set_dir(double dir){
		target_direction = dir;
	}
	public static boolean active_turn(double dir){
		if(Math.abs(target_direction-dir) < 4){
			motorControle.stop();
			return true;
		}
		double[] mot = {0,0};
		if((Math.abs(target_direction-cur_direction))>180){
			mot[0]= 100;
		}else{
			mot[1]= 100;
		}
		motorControle.set_motors(mot);
		return false;
	}
	public static void turn2(){
		double[] mot = {0,0};
		if(!turn_correct(target_direction)){
			if((Math.abs(target_direction-cur_direction))>180){
				mot[0]= (Math.abs(target_direction-cur_direction)/2);
			}else{
				mot[1]= (Math.abs(target_direction-cur_direction-180)/2);
			}
		}
		motorControle.set_motors(mot);
	}
	
	//@SuppressWarnings("unused")
	public static double[] move(double dir, double dist){
		//dir: 0= forward, negative is left, positive is right, from -180 to 180
		double LM = 1, RM = 1;
		//double dist_mult = 5;
		double dir_mult = 2;
		double k = 2;
		if(dist > 50 && Math.abs(dir) < 30){
			LM = LM*k*(dist*dir_mult + dir_mult*dir);
			RM = RM*k*(dist*dir_mult - dir_mult*dir);
		}else if(Math.abs(dir) >= 30){
			LM = LM*k*(k + dir_mult*dir);
			RM = RM*k*(k - dir_mult*dir);
		}else if(dist < 50){
			if(dist < 10){
				LM = 0;
				RM = 0;
				logger.info("Reached destination, turning off motors");
			}else{
				LM = LM*k*(dist + dir_mult*dir);
				RM = LM*k*(dist - dir_mult*dir);
			}
		}
		double[] motors = {LM,RM};
		return motors;
	}
	public static void abort(){
		logger.error("Aborting!");
		double[] stop = {-1.0,-1.0,-1.0,-1.0,0.0,0.0};//change sinking motors positive to rise quickly
		motorControle.set_motors(stop);
		try {
			basic.shutdown();
		} catch (InterruptedException e) {
			logger.info("Error 76");
			e.printStackTrace();
		}
	}
	public static void stop(){
		double[] stop = {0.0,0.0};
		motorControle.set_motors(stop);
	}
	public static void surface(){
		double[] stop = {0.0,0.0,0.0,0.0};
		motorControle.set_motors(stop);
	}
	@Override
	public void run() {
		logger.info("Initilizing stabilization");
		try {
			stable_cal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//self test
		run = true;
		init = true;
		logger.info("Stabilization and motor function initilized");
		stable_run();
		logger.info("Shutting down sub stabilization");
	}
	
	@SuppressWarnings("unused")
	private static void stable_cal() throws InterruptedException{
		double roll_cal_total = 0;
		double pitch_cal_total = 0;
		for(int x = 0; x<50;x++){
			pitch_cal_total+=update.IMU_pitch();
			roll_cal_total+=update.IMU_roll();
			Thread.sleep(5);
		}
		//double roll_cal = roll_cal_total/100;//calibration for IMU
		//double pitch_cal = pitch_cal_total/100;
	}
	public static void move(int speed){
		double[] move = {speed,speed};
		motorControle.set_motors(move);
	}
	private void stable_run() {
		while(run){
			motorControle.set_motors(stable());
			try {
				Thread.sleep(90);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void start() {
		if (t == null) {
			t = new Thread(this, "movable");
			t.start();
		}
	}
	
}
