package robosub;

import com.pi4j.system.SystemInfo;

public class coreTwo implements Runnable {
	static Thread t;//Thread for this class
	static boolean RUN = false;//is running
	public static boolean INIT = false;
	private static boolean PI = false;
	private static short error = 0;
	private static boolean error_allow = true;
	public static int mode = 0;
	
	private void master() {
		while(RUN){
			
		}
		
	}
	
	
	
	
	
	
	
	
	public static boolean status(){//this checks to see if there is a problem
		if(!error_allow){
			return true;
		}
		if(!status2()){//different from status2() as there needs 3 consecutive error for there to be an abort
			error++;//this means one false reading wont abort the whole system
		}else if(error>0){
			error--;
		}
		if(error > 2){//three status fails needed to cuase total fail
			return false;
		}else{
			return true;
		}
	}
	
	private static boolean status2() {// check to make sure everything is OK
		//error_allow = false;
		if (update.getWaterSensor() > 600) {//600 is a fair amount of water, <10 is no water
			/*if (basic.logger_lvl > 0)
				debug.log("Takeing on water; level at: " + update.getWaterSensor());*/
			debug.print("Takeing on water; level at: " + update.getWaterSensor());
			return false;// we are taking on water, abort!
		}
		if (Math.abs(update.IMU_pitch()) > 20 || Math.abs(update.IMU_roll()) > 20) {
			System.out.println("Pitch or roll too great, shutting down for safty;");
			System.out.println("pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			if (basic.logger_lvl > 0)
				debug.log("pithc/roll too great;");
			if (basic.logger_lvl > 0)
				debug.log("pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			debug.print("Bad stable: pitch: " + update.IMU_pitch() + " roll: " + update.IMU_roll());
			return false;
		}
		//TODO check anything else, tmp, battery level etc.
		try{
			int temp = (int) SystemInfo.getCpuTemperature();
			if(temp > 90 && temp < 98){
				System.out.println("Things are getting hot!");
				debug.log("CPU temp hot: "+SystemInfo.getCpuTemperature());
			}else if(temp >= 98){
				System.out.println("Way too hot");
				debug.log("CPU temp too hott: "+SystemInfo.getCpuTemperature());
				parser.parse("exit");
				return false; //this can be removed if you don't care about this little CPU that tryed
			}
		}catch(Exception e){
			System.out.println("Couldnt get temp: "+e.getMessage());
		}
		return true;
	}
	
	public static String info() {
		float temp = -100;
		try{
			temp = SystemInfo.getCpuTemperature();
		}catch(Exception e){
			System.out.println("Couldn't get temp: " + e.getLocalizedMessage());
		}
		String name = null;
		try{name = basic.mode_names[mode];}catch(Exception e){name = "No name";}
		String all_info = "System temp: "+temp;
		all_info += "\nSystem Status: "+status();
		all_info += "\nIs PI: "+PI;
		all_info += "\nInit, Run & Connected: "+INIT+", "+RUN + ", "+update.self_test();
		all_info += "\nMode: " + mode + ", "+name+"\n";
		all_info += "Stabalized: "+movable.isStabilize()+", Movable mode: "+movable.mode;
		all_info += "\nMotor Values: "+movable.print_motor_values();
		all_info += "\nTelemitry; Current, Target \n\tYaw(direction): "+update.IMU_yaw()+"; "+movable.getTarget_direction();
		all_info += "\n\tPitch: "+update.IMU_pitch()+", "+movable.getcal_pitch();
		all_info += "\n\tRoll: "+update.IMU_roll()+", "+movable.getcal_roll();
		all_info += "\n\tDepth: "+update.get_depth()+"; "+movable.getTarget_depth();
		
		return all_info;
	}
	public void shutdown(){
		RUN = false;
	}
	
	 public void start() {
	        if (t == null) {
	            t = new Thread(this, "coreTwo");
	            t.start();
	        }else{
	       	 debug.logWithStack("Second instance being made in coreTwo: " + t.getName());
	        }
	    }


	@Override
	public void run() {
		try{
			master();
		}catch(Exception e){
			System.out.println("Exception in coreTwo: "+e);
		}
		
	}

}
