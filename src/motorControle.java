package robosub;

//import org.apache.log4j.Logger;

/**
* Class to interface between direct motor control and update
* @author Dakota
*
*/
class motorControle {//implements Runnable{
	public static int form = 0;
	//private static Logger logger = Logger.getLogger(motorControle.class.getCanonicalName());
	public static int max_speed = 1750;//this is a very conservative max and min for the motors
	public static final int min_speed = 1250;//too high or too low can danmage them. 
	//however, you can adjust if you need more power
	//Actual max and min values of current motor servo controlers are 1100-1900
	//TODO Changes might also need to be made in Arduino code 
	
	//static boolean init = false;//old
	static double[] motor_vals = {0.0,0.0,0.0,0.0,0.0,0.0};//FLM,FRM,BLM,BRM,LM,RM
	static boolean[] motor_enable = {true, true, true, true, true, true};

	public static void set_motors(double[] x) throws Exception{
		for(int i = 0;i<x.length;i++){
			if(x[i]>max_speed){
				debug.log("Warning; invalid motor value: "+x[i] + " @ motor: "+i);
				x[i] = max_speed;
			}
			if(x[i] < min_speed){
				debug.log("Warning; invalid motor value: "+x[i]+ " @ motor: "+i);
				x[i] = min_speed;
			}
		}
		form++;
		if(form%10==0 && basic.debug_lvl >=9){System.out.println("Motors: "); for(int i = 0;i<x.length;i++) System.out.print("Seting motor @: "+x[i]);if(form>12300) form =0;}
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
				throw new Exception("Invalid motor commands givent to motorControle.set_motors: "+x);
			}	
		}
		//possibly fix up this code to make it work better
		for(int p = 0; p<6;p++){
			if(!motor_enable[p]){
				motor_vals[p] = 1500;//sets motor to off if its disable
			}
		}
		update.set_motors(motor_vals);
	}
	public static void motor_enable(int mot, boolean en){
		debug.log("Motor "+mot+" set to "+en);
		motor_enable[mot] = en;
	}
	
}
