package robosub;

/**
* Class to interface between direct motor control and update
* Checks motor values are within limits, handles enable, and sets max limit of motors
* @author Dakota
*
*/
class motorControle {
	public static int form = 0;
	private static int TopSpeed = 1700; //software defined top speed
	public static final int max_speed = 1750;//this is a very conservative max and min for the motors
	public static final int min_speed = 1250;//too high or too low can danmage them. Hardware limit
	//however, you can adjust if you need more power
	//Actual max and min values of current motor servo controlers are 1100-1900
	static int[] motor_vals = {1500,1500,1500,1500,1500,1500};//FLM,FRM,BLM,BRM,LM,RM
	static boolean[] motor_enable = {true, true, true, true, true, true};
	private static boolean invertVerticalMotors = false;
	//private static final int baseSpeed = 1500;

	/**
	 * Sets motor values, formats and passes values to update
	 * @param motors Array of motor values, 2, 4, or 6 values 
	 * @throws Exception Incorrect number of motor values
	 */
	public static void set_motors(int[] motors) throws Exception{
		for(int i = 0;i<motors.length;i++){
			if(motors[i] > TopSpeed){
				motors[i] = TopSpeed;
			}else if(motors[i] < 3000 - TopSpeed){
				motors[i] = 3000 - TopSpeed;
			}
			/*if(motors[i] > max_speed){
				debug.print("Warning; invalid motor value, value too high: "+motors[i] + " @ motor: "+i);
				motors[i] = max_speed;
			}else if(motors[i] < min_speed){
				debug.print("Warning; invalid motor value, value too low: "+motors[i]+ " @ motor: "+i);
				motors[i] = min_speed;
			}*/
		}
		form++;
		if(form%10==0 && basic.debug_lvl >= 9){System.out.println("Motors: "); for(int i = 0;i<motors.length;i++) System.out.print("Seting motor @: "+motors[i]);if(form>12300) form = 0;}
		if(motors.length == 6){
			//all motors
			motor_vals = motors;
		}else if(motors.length == 4){
			//depth motors
			for(int i=0;i<4;i++){
				if(invertVerticalMotors){
					motors[i] = 3000-motors[i];
				}
				motor_vals[i]=motors[i];
			}
		}else if(motors.length == 2){
			//moving motors
			for(int i=4;i<6;i++){
				motor_vals[i]=motors[i-4];
			}
		}else{
			if(core.no_fill()){//force setting these motors anyways
				for(int i=0;i<6;i++){
					if(invertVerticalMotors && i <=4){
						motors[i] = 3000-motors[i];
					}
					motor_vals[i]=motors[i];
				}
			}else{
				debug.error(motors.length+" motor values given, only 2,4 and 6 are valid");
				throw new Exception("Invalid motor commands givent to motorControle.set_motors: "+motors);
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
	/**
	 * Enable or disables motors
	 * @param mot The motor to change state of
	 * @param enable true = on/enables, false = off/disabled
	 */
	public static void motor_enable(int mot, boolean enable){
		try{motor_enable[mot] = enable;}catch(ArrayIndexOutOfBoundsException e){System.out.println("Out of bounds: "+e); return;}
		debug.print("Motor "+mot+" set to "+enable);
	}
	
	/**
	 * Sets the maximum speed of the motors. Speed is offset from 1500
	 * Range: ~150 - 250
	 * @param newSpeed New top speed 
	 */
	public static void setTopSpeed(int newSpeed){
		if(newSpeed + 1500 > max_speed){
			System.out.println("Warning; invalid motor value, value too high.");
			newSpeed = max_speed;
		}
		if(1500 - newSpeed < min_speed){
			System.out.println("Warning; invalid motor value, value too low.");
			newSpeed = min_speed;
		}
		TopSpeed = newSpeed;
	}
}