package robosub;

import org.apache.log4j.Logger;

class motorControle implements Runnable{
	private static Logger logger = Logger.getLogger(motorControle.class.getCanonicalName());
	public static int max_speed = 100;
	Thread t;
	private static boolean run = false;
	static boolean init = false;
	static double[] motor_vals = {0.0,0.0,0.0,0.0,0.0,0.0};//FLM,FRM,BLM,BRM,LM,RM
	public static void stop(){run = false;}
	public void run() {
		logger.info("Initilizing motor controle");
		//run set up to initiate connection to ardino
		//self test
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		run = true;
		init = true;
		logger.info("Motor controle init complete");
		try {
			motor_run();
		} catch (InterruptedException e) {
			debug.error(e.getLocalizedMessage());
		}
		logger.info("Shutting down motor controler");
	}
	private void motor_run() throws InterruptedException {
		while(run){
			//send to ardino motor_vals
			Thread.sleep(20);
		}
	}
	public void start() {
		if (t == null) {
			t = new Thread(this, "motorControle");
			t.start();
		}
	}
	public static void set_motors(double[] x){
		for(int i = 0;i<x.length;i++){
			if(x[i]>max_speed){
				x[i] = max_speed;
			}
			if(x[i] < 0){
				x[i] = 0;
			}
		}
		if(x.length == 6){
			//all motors
			motor_vals = x;
		}else if(x.length == 4){
			//depth motors
			for(int i=0;i<4;i++){
				motor_vals[i]=x[i];
			}
		}else if(x.length == 2){
			//moving motors
			for(int i=4;i<6;i++){
				motor_vals[i]=x[i-4];
			}
		}else{
			if(core.no_fill()){
				for(int i=0;i<6;i++){
					motor_vals[i]=x[i];
				}
			}else{
				debug.error(x.length+" motor values given, only 2,4 and 6 are valid");
			}	
		}
		update.set_motors(motor_vals);
	}
}