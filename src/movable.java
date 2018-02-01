package robosub;

//import org.apache.log4j.Logger;

/**
 * Used to interface simple commands like turn and move to complex motor control
 * with motorControl
 * 
 * @author Dakota
 *
 */
class movable implements Runnable {
	Thread t;
	static boolean run = false;// Initialize to false, must be set to true
								// before running
	static boolean init = false;//keeps track of if movable has been initialized 
	static boolean stabilize = true;// Whether or not to actively stabilize the
									// sub
	//private static Logger logger = Logger.getLogger(movable.class.getCanonicalName());
	private static double target_depth = 5;// in [no unit]
	private static double target_direction = 0;// in degrees
	private static double sideMove = 0;//for movements laterally:side to side
	private static int speed = 100;// not max speed, just normal seed
	private static final int base_speed = 1500;
	public static int mode = 0;// mode 0=don't move, 1=turn, 2=move and turn, 3
								// = move forward with no turning, 5= no update
								//6=side movement, no stabilization, experimental 
	private static double[] motors = { base_speed, base_speed, base_speed, base_speed, base_speed, base_speed };// see basic for motor
														// names
	private static double cal_pitch;//calabrated level pitch
	private static double cal_roll;//calebrated stable roll
	private static double target_pitch = 0.0;//target pitch
	private static double target_roll = 0.0;//target roll angle
	private static boolean use_IMU_cal = true;
	public static int getbase_speed(){return base_speed;}
	public static boolean isStabilize() {
		return stabilize;
	}
	public static double getTarget_depth() {
		return target_depth;
	}
	public static double getTarget_direction() {
		return target_direction;
	}
	public static double getcal_pitch(){
		return cal_pitch;
	}
	public static double getcal_roll(){
		return cal_roll;
	}
	 
	public static void setuse_IMU_cal(){
		use_IMU_cal = false;
		cal_pitch = 0;
		cal_roll = 0;
	}
	/**
	 * Stops movment of sub
	 */
	public static void stop() {
		motors[4] = base_speed;
		motors[5] = base_speed;
		mode = 0;
	}
	public static void quickSurface(){
		motors[0] = base_speed - 100;
		motors[1] = base_speed - 100;
		motors[2] = base_speed - 100;
		motors[3] = base_speed - 100;
		target_depth = -50;
	}

	/**
	 * Brings sub to surface
	 */
	public static void surface() {
		motors[0] = base_speed;
		motors[1] = base_speed;
		motors[2] = base_speed;
		motors[3] = base_speed;
		target_depth = 0;
	}
	
	/**
	 * Used to move sub right and left laterally. 
	 * Changes mode to 6.
	 * Positive = right, negative = left.
	 * This will not stop movement until mov is set to 0 or mode is changed.
	 * @param mov How fast to move laterally. Positive moves sub right, negative numbers move it left.
	 */
	public static void side(int mov){
		mode = 6;
		sideMove = mov;
	}
	
	/**
	 * Internal method to adjust stable motor position to move sub laterally left and right. 
	 * @param tmp Stable motor position
	 * @return New motor position
	 */
	private double[] side_move(double[] tmp) {
		//0,2=left;1,3=right
		
		//brute force way:
		tmp[0] = tmp[0] + sideMove;
		tmp[2] = tmp[2] + sideMove;
		tmp[1] = tmp[1] - sideMove;
		tmp[3] = tmp[3] - sideMove;
		
		//better way to do it is to adjust target tilt angle 
		return tmp;
	}

	/**
	 * Turns sub in direction of dir This direction is
	 * absolute(update.direction), not relative (Updates mode to 1)
	 * 
	 * @param dir
	 */
	public static void face(int dir) {
		target_direction = dir;
		if (!isFacing(dir)) {
			mode = 1;
		}
	}

	/**
	 * Turns sub in direction of dir. This direction is relative, not absolute
	 * (Updates mode to 1)
	 * 
	 * @param dir
	 */
	public static void face_R(int dir) {
		dir = (dir + update.direction) % 360;
		target_direction = dir;
		if (!isFacing(dir)) {
			mode = 1;
		}
	}

	/**
	 * Moves sub in direction dir. Move and turns at same time This direction is
	 * absolute(update.direction), not relative (Updates mode to 2)
	 * 
	 * @param dir
	 */
	public static void moveInDir(int dir) {
		target_direction = dir;
		mode = 2;
	}

	/**
	 * Moves sub in direction dir. Move and turns at same time This direction is
	 * relative, not absolute (Updates mode to 2)
	 * 
	 * @param dir
	 */
	public static void moveInDir_R(int dir) {
		dir = (dir + update.direction) % 360;
		target_direction = dir;
		mode = 2;
	}

	/**
	 * This turns on and off stabilization of the sub using IMU data Only turn
	 * off (false) if you don't trust stabilization or IMU data. Default is ON
	 * (true) and best to leave it that way
	 * 
	 * @param boo
	 */
	public static void stabilize(boolean boo) {
		stabilize = boo;
	}

	/**
	 * Sets speed of sub. This speed is not in any units and there is an upper
	 * limit. Don't know the limit as of writing this. Default is 100, change as
	 * you wish. Please no negatives
	 * 
	 * @param sp
	 */
	public static void setSpeed(int new_speed) {
		if(new_speed < 0 || new_speed >= 200){
			debug.print("invalid speed set: "+new_speed);
		}
		speed = new_speed;
	}

	/**
	 * Prints out all info on sub, used for debug.
	 * 
	 * @return
	 */
	public static String print() {
		String re = "Movable; Motors: ";
		for (double i : motors) {
			re += i + ", ";
		}
		re += " Target depth: " + target_depth;
		re += " Target direction: " + target_direction;
		re += " Mode: " + mode;
		re += " Stabilize? " + stabilize;
		re += " Dpth & Dir: " + update.depth + " " + update.direction;
		return re;
	}

	public static String print_motor_values() {
		String re = "Movable; Motors: ";
		for (double i : motors) {
			re += i + ", ";
		}
		return re;
	}
	
	public String toString() {// self print
		return print();
	}

	/**
	 * Forces value of specific motor pos to speed speed. Motor positions are:
	 * "FL","FR","BL","BR","L","R" Use with mode 0. Only use for debug or if you
	 * know what you are doing
	 * As a further note, using motorControle.set_motors might be better
	 * @param speed
	 * @param pos
	 */
	public static void set_raw_input(int speed, int pos) {
		motors[pos] = speed;
	}

	/**
	 * Sets mode to 3. This means the sub will move forward at set speed and
	 * wont turn until told to turn. Used for debug.
	 */
	public static void move() {
		mode = 3;
	}

	/**
	 * Turns on every motor one at a time for 2 seconds each.
	 * 
	 * @throws InterruptedException
	 */
	public static void motorTest() throws InterruptedException {
		if (!core.INIT) {
			System.out.print("Can't run test without init first");
			return;
		}
		abort();// turn off all motors
		boolean tmp = stabilize;
		int tmp2 = mode;
		stabilize = false;
		for (int i = 0; i < 6; i++) {
			mode = 5;
			set_raw_input(base_speed + speed, i);
			System.out.println("Turning on motor: " + basic.MOTOR_LAYOUT[i] + " @: "+(base_speed + speed));
			Thread.sleep(2000);
			abort();
		}
		abort();
		mode = 5;
		Thread.sleep(110);
		System.out.println("Now reversing both motors");
		set_raw_input((base_speed - speed), 4);
		set_raw_input((base_speed - speed), 5);
		Thread.sleep(2000);
		abort();
		stabilize = tmp;
		mode = tmp2;
		System.out.println("All motors should no be at rest.");
	}

	/**
	 * Stops and surfaces sub.
	 */
	public static void abort() {
		stop();
		surface();
	}

	/**
	 * Used to calculate value of left and right motor
	 * Uses values from update.directio (current facing direction) and target_direction and mode
	 */
	private static void internal_move() {
		double LM = base_speed, RM = base_speed;
		if (mode == 2) {// if move and turn set inital value to move forward
			LM = base_speed + speed;
			RM = base_speed + speed;
		}
		// if mode is 1(turn and dont move) or if we are far from correct
		// direction, stop
		if (mode == 1 || Math.abs(update.direction - target_direction) > 30) {
			LM = base_speed;
			RM = base_speed;
		}
		// if we aren't in correct direction:
		if (!isCorrect() && mode > 0) {
			if ((target_direction + update.direction) % 360 < 180) {// turn
																	// right
				LM = LM + 25 + 2*(180 - Math.abs(Math.abs(target_direction - update.direction) - 180));
				RM = RM - 25 - 2*(180 - Math.abs(Math.abs(target_direction - update.direction) - 180));
			} else {// turn left
				LM = LM - 25 - 2*(180 - Math.abs(Math.abs(target_direction - update.direction) - 180));
				RM = RM + 25 + 2*(180 - Math.abs(Math.abs(target_direction - update.direction) - 180));
			}
		}
		if (mode == 3) {// if we are only suppose to move forward(mode 3) ignore
						// everything and go forward
			LM = base_speed + speed;
			RM = base_speed + speed;
		}
		/*if (LM < 0)// check to make sure there are not negatives
			LM = base_speed;
		if (RM < 0)// this may be removed when negatives are implemented on ard
			RM = base_speed;*/
		if (mode != 5) {
			motors[4] = LM;
			motors[5] = RM;
		}
	}

	/**
	 * Forces mode to m. Used for debugging, only use if you know what you are
	 * doing.
	 * 
	 * @param m
	 */
	public static void forceMode(int m) {
		mode = m;
	}

	/**
	 * Pauses the thread until sub is facing the right way.
	 */
	public static void waitTillTurned() {
		int itt = 0;
		mode = 1;
		while (!isCorrect()) {
			itt++;
			try {
				if (itt > 200) {//20 seconds
					throw new Exception("Taking too long to wait for turn");
				}
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * Used to turn the sub without IMU data. Negative values turn it left,
	 * positive values turn it right. Amount turned corresponds to value of num.
	 * Better to use face(int) or moveInDir(int).
	 * 
	 * @param num
	 */
	public static void simpleTurn(int num) {
		int tmp = mode;
		mode = 0;
		int LM = 0, RM = 0;
		for (int i = 0; i < Math.abs(num); i++) {
			if (num > 0) {
				LM = base_speed + speed;
			} else {
				RM = base_speed + speed;
			}
			motors[4] = LM;
			motors[5] = RM;
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			target_direction = update.direction;
		}
		mode = tmp;
	}

	/**
	 * Stops execution of this thread within 100ms.
	 */
	public static void stop_thread() {
		run = false;
	}

	/**
	 * Sets target depth of sub. This depth may or may not correspond to any
	 * actual unit of measurement.
	 * 
	 * @param new_depth
	 */
	public static void set_depth(double new_depth) {
		target_depth = new_depth;
	}

	/**
	 * Used to calculate values of outer four motors.
	 * If mode is 5, motors will not update.
	 * @return
	 */
	private static double[] stable() {
		if(target_depth == 0)
			return motors;
		if (mode == 5)
			return motors;
		if (stabilize) {
			return stable(update.get_depth(), target_depth, update.IMU_roll(), update.IMU_pitch());
		} else {
			return stable(update.get_depth(), target_depth);
		}
	}

	// what am i doing at 5am? why am i working on this
	/**
	 * Used to calculate values of outer four motors without IMU data. Used when
	 * stabilize = false
	 * 
	 * @param current_depth
	 * @param target_depth
	 * @return
	 */
	private static double[] stable(double current_depth, double target_depth) {
		int C1 = 10;
		int d_m = base_speed + (int) ((target_depth - current_depth) * C1);
		double[] motors = { d_m, d_m, d_m, d_m };
		return motors;
	}

	/**
	 * Used to calculate values of outer four motors with IMU data. stabilize =
	 * true
	 * 
	 * @param current_depth
	 * @param target_depth
	 * @param roll
	 * @param pitch
	 * @return
	 */
	private static double[] stable(double current_depth, double target_depth, double roll, double pitch) {
		int K = 1; // constant multiplier for calibration
		int FLM = base_speed, FRM = base_speed, BLM = base_speed, BRM = base_speed; // front left motor,
															// etc. final value
															// is motor speed
		int base = 0; // base downward force to cancel out positive buoyancy
		int C1 = 1, C2 = 1, C3 = 1;// C1, C2, C3 used for calibration

		int depth_multiplier = 10 *(int) ((target_depth - current_depth) * C1);
		int pitch_multiplier = 2*(int) (((cal_pitch - target_pitch) - pitch) * C2); // can replace 0 with
															// number given by
															// IMU when level---DONE
		int roll_multiplier = 2*(int) (((cal_roll - target_roll) - roll) * C3);

		FLM += (double) (K * (base + depth_multiplier + pitch_multiplier + roll_multiplier));
		FRM += (double) K * (base + depth_multiplier + pitch_multiplier - roll_multiplier);
		BLM += (double) K * (base + depth_multiplier - pitch_multiplier + roll_multiplier);
		BRM += (double) K * (base + depth_multiplier - pitch_multiplier - roll_multiplier);

		double[] motors = { FLM, FRM, BLM, BRM };
		// for(int i = 0; i < 4; i++) System.out.println("mot" + motors[i]);
		return motors;
	}
	
	/**
	 * Starts moving the sub backwards at speed [speed].
	 * Also changes mode to 5;
	 */
	public static void reverse(){
		mode = 5;
		int R = base_speed, L = base_speed;
		R -= speed;
		L -= speed;
		motors[4] = L;
		motors[5] = R;
	}

	/**
	 * Returns if sub is facing within 4 degrees of direction dir. This
	 * direction is absolute(update.direction), not relative
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean isFacing(double dir) {
		if (Math.abs(update.direction - dir) < 4) {
			return true;
		}
		return false;
	}

	/**
	 * Returns if sub is facing within 4 degrees of direction dir. This
	 * direction is relative, not absolute
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean isFacing_R(double dir) {
		dir = (dir + update.direction) % 360;
		if (Math.abs(update.direction - dir) < 4) {
			return true;
		}
		return false;
	}

	/**
	 * REturns if sub is facing within 4 degrees of target direction
	 * 
	 * @return
	 */
	public static boolean isCorrect() {
		if (Math.abs(update.direction - target_direction) < 4) {
			return true;
		}
		return false;
	}

	/**
	 * Sets target direction of sub without changing mode. This direction is
	 * absolute(update.direction), not relative. Better to use face() or
	 * moveInDir().
	 * 
	 * @param dir
	 */
	public static void set_dir(double dir) {
		target_direction = dir;
	}

	/**
	 * Sets target direction of sub without changing mode. This direction is
	 * relative, not absolute. Better to use face() or moveInDir().
	 * 
	 * @param dir
	 */
	public static void set_dir_R(double dir) {
		target_direction = dir;
	}

	// @SuppressWarnings("unused")
	@Deprecated
	public static double[] move(double dir, double dist) {
		// dir: 0= forward, negative is left, positive is right, from -180 to
		// 180
		double LM = 1, RM = 1;
		// double dist_mult = 5;
		double dir_mult = 2;
		double k = 2;
		if (dist > 50 && Math.abs(dir) < 30) {
			LM = LM * k * (dist * dir_mult + dir_mult * dir);
			RM = RM * k * (dist * dir_mult - dir_mult * dir);
		} else if (Math.abs(dir) >= 30) {
			LM = LM * k * (k + dir_mult * dir);
			RM = RM * k * (k - dir_mult * dir);
		} else if (dist < 50) {
			if (dist < 10) {
				LM = 0;
				RM = 0;
				System.out.println("Reached destination, turning off motors");
			} else {
				LM = LM * k * (dist + dir_mult * dir);
				RM = RM * k * (dist - dir_mult * dir);
			}
		}
		double[] motors = { LM, RM };
		return motors;
	}

	//@SuppressWarnings("unused")-gives warning that 'unused' is unused. Ironic.
	private static void stable_cal() throws Exception {
		double roll_cal_total = 0;
		double pitch_cal_total = 0;
		for (int x = 0; x < 50; x++) {
			pitch_cal_total += update.IMU_pitch();
			roll_cal_total += update.IMU_roll();
			Thread.sleep(101);
		}
		cal_roll = roll_cal_total/50;
		cal_pitch = pitch_cal_total/50;
		debug.log("Calabrated roll: "+cal_roll+" pitch: "+cal_pitch);
		if(cal_roll > 10 || cal_pitch > 10){
			System.out.print("Invalid roll/pitch values");
			System.out.print("Calabrated roll: "+cal_roll+" pitch: "+cal_pitch);
			throw new Exception("Invalid calabration position. Reset sub position and reinitiate");
		}
		// double roll_cal = roll_cal_total/100;//calibration for IMU
		// double pitch_cal = pitch_cal_total/100;
	}

	/**
	 * Main method of movable; calls stable() and internal_move(); Then sets
	 * motors to correct value using motorControle
	 * @throws Exception 
	 */
	private void norm() throws Exception {
		int div = 0;
		while (run) {
			if (div == 10000)
				div = -1;
			div++;
			double[] tmp = stable();
			if(mode == 6){
				tmp = side_move(tmp);
			}else{
				if(use_IMU_cal){//cal_pitch and roll should already be set
					//do nothing
					//target_roll = cal_roll;
					//target_pitch = cal_pitch;

				}else{
					//target_roll = 0;
					//target_pitch = 0;
					cal_roll = 0;
					cal_pitch = 0;
				}
			}
			for (int i = 0; i < 4; i++) {
				motors[i] = tmp[i];
			}
			internal_move();
			if (basic.debug_lvl > 10) {
				System.out.print(this.toString());
			}
			if ((basic.logger_lvl > 6 && div % 10 == 0) || (basic.logger_lvl >= 9))
				debug.log(this.toString());
			try {
				motorControle.set_motors(motors);
				Thread.sleep(90);
			} catch (InterruptedException e) {
				e.printStackTrace();
				debug.log_err(e.getLocalizedMessage());
			}
		}
	}

//debug.log_err(e.getLocalizedMessage());
	@Override
	public void run(){
		System.out.println("Initilizing stabilization");
		try {
			stable_cal();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// self test
		run = true;
		init = true;
		System.out.println("Stabilization and motor function initilized");
		try {
			norm();
		} catch (Exception e) {
			e.printStackTrace();
			debug.log_err(e.getLocalizedMessage());
		}
		System.out.println("Shutting down sub stabilization");
	}
	public static boolean initiated(){return(run && init);}
	public void start() {
		if (t == null) {
			t = new Thread(this, "movable");
			t.start();
		} else {
			debug.logWithStack("Second instance being made: movable");
		}
	}

}
