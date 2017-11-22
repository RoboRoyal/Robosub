/*
 * movable.cpp
 *
 *  Created on: Oct 31, 2017
 *      Author: Dakota
 */

#include "movable.h"
#include "logger.cpp"
#include "update.h"

movable::movable() {
	// TODO Auto-generated constructor stub

}

/**
 * Used to interface simple commands like turn and move to complex motor control
 * with motorControl
 *
 * @author Dakota
 *
 */

/**
 * REturns if sub is facing within 4 degrees of target direction
 *
 * @return
 */
static int isCorrect() {
  if (abs(update::get_direction() - target_direction) < 4) {
    return 1;
  }
  return 0;
}


	/**
	 * Stops movement of sub
	 */
	static void stop() {
		motors[4] = 0;
		motors[5] = 0;
		mode = 0;
	}

	/**
	 * Brings sub to surface
	 */
	static void surface() {
		motors[0] = 0;
		motors[1] = 0;
		motors[2] = 0;
		motors[3] = 0;
		target_depth = -10;
	}


	/**
	 * Moves sub in direction dir. Move and turns at same time This direction is
	 * absolute(update.get_direction()), not relative (Updates mode to 2)
	 *
	 * @param dir
	 */
	static void moveInDir(int dir) {
		target_direction = dir;
		mode = 2;
	}

	/**
	 * Moves sub in direction dir. Move and turns at same time This direction is
	 * relative, not absolute (Updates mode to 2)
	 *
	 * @param dir
	 */
	 static void moveInDir_R(int dir) {
	   dir = (dir + update::get_direction()) % 360;
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
	 static void set_stabilize(int boo) {
		stabilize = boo;
	}

	/**
	 * Sets speed of sub. This speed is not in any units and there is an upper
	 * limit. Don't know the limit as of writing this. Default is 100, change as
	 * you wish. Please no negatives
	 *
	 * @param sp
	 */
	 static void setSpeed(int sp) {
		speed = sp;
	}

	/**
	 * Prints out all info on sub, used for debug.
	 *
	 * @return
	 */
	 static string print() {
		char re[] = "Movable; Motors: ";
		for (int x = 0;x<6;x++) {
		  double i = motors[x];
		  //sprintf(re,i,", ");
		}
		sprintf(re, " Target depth: ");
		//sprintf(re,target_depth);
		sprintf(re,"Not implimented!");
		strcpy(re," Target direction: ");
		//strcpy(re,target_direction);
		//sprintf(re," Mode: " , mode);
		//sprintf(re," Stabilize? " , stabilize);
		//sprintf(re," Dpth & Dir: ",update::get_depth()," ",update::get_direction());
		return re;
	}

	 string toString() {// self print
		return print();
	}

	/**
	 * Forces value of specific motor pos to speed speed. Motor positions are:
	 * "FL","FR","BL","BR","L","R" Use with mode 0. Only use for debug or if you
	 * know what you are doing
	 *
	 * @param speed
	 * @param pos
	 */
	 static void set_raw_input(int speed, int pos) {
		motors[pos] = speed;
	}

	/**
	 * Sets mode to 3. This means the sub will move forward at set speed and
	 * wont turn until told to turn. Used for debug.
	 */
	 static void move() {
		mode = 3;
	}

	/**
	 * Turns on every motor one at a time for 2 seconds each.
	 *
	 * @throws InterruptedException
	 */
	 static void motorTest(){
		/*if (!core.INIT) {
			logger.error("Can't run test without init first");
			return;
		}
		abort();//turn off all motors
		boolean tmp = stabilize;
		stabilize = false;
		for (int i = 0; i < 6; i++) {
			mode = 5;
			set_raw_input(speed, i);
			logger.info("Turning on motor: " + basic.MOTOR_LAYOUT[i]);
			Thread.sleep(2000);
			abort();
		}
		abort();
		stabilize = tmp;*/
		// TODO add reverse test for R and L motors
	}

	/**
	 * Stops and surfaces sub.
	 */
	 static void abort() {
		stop();
		surface();
	}

	/**
	 * Used to calculate value of left and right motor
	 */
	 static void internal_move() {
		double LM = 0, RM = 0;
		if (mode == 2) {// if move and turn set inital value to move forward
			LM = speed;
			RM = speed;
		}
		// if mode is 1(turn and dont move) or if we are far from correct
		// direction, stop
		if (mode == 1 || abs(update::get_direction() - target_direction) > 30) {
			LM = 0;
			RM = 0;
		}
		// if we aren't in correct direction:
		if (!isCorrect() && mode > 0) {
		  if ((int)(target_direction + update::get_direction()) % 360 > 180) {// turn
																	// right
			  LM = LM + 5 + (int)(target_direction + update::get_direction()) % 360;
			  RM = RM - 5 - (int)(target_direction + update::get_direction()) % 360;
			} else {// turn left
			  LM = LM - 5 - (int)(target_direction + update::get_direction()) % 360;
			  RM = RM + 5 + (int)(target_direction + update::get_direction()) % 360;
			}
		}
		if (mode == 3) {// if we are only suppose to move forward(mode 3) ignore
						// everything and go forward
			LM = speed;
			RM = speed;
		}
		if (LM < 0)// check to make sure there are not negatives
			LM = 0;
		if (RM < 0)// this may be removed when negatives are implemented on ard
			RM = 0;
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
	 static void forceMode(int m) {
		mode = m;
	}

	/**
	 * Pauses the thread until sub is facing the right way.
	 */
	 static void waitTillTurned() {
		int itt = 0;
		mode = 1;
		while (!isCorrect()) {
			itt++;
			try {
				if (itt > 100) {
					//throw ("Taking too long to wait for turn");
					throw 101;
				}
				//Thread.sleep(100);
			} catch (int e) {
				//e.printStackTrace();
			  //logger::error(e);
			  logger::error("There was an error in waitTillTurned");
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
	 static void simpleTurn(int num) {
		int tmp = mode;
		mode = 0;
		int LM = 0, RM = 0;
		int num2 = num;
		if(num2 < 0){
		  num2 = num2 * -1;
		}
		for (int i = 0; i < num2; i++) {
			if (num > 0) {
				LM = speed;
			} else {
				RM = speed;
			}
			motors[4] = LM;
			motors[5] = RM;
			try {
			  //Thread.sleep(100);
			} catch (int e) {
			  //e.printStackTrace();
			  cout<<"error:"<<e<<"\n";
			}
			target_direction = update::get_direction();
		}
		mode = tmp;
	}

	/**
	 * Stops execution of this thread within 100ms.
	 */
	 static void stop_thread() {
		run = 0;
	}

	/**
	 * Sets target depth of sub. This depth may or may not correspond to any
	 * actual unit of measurement.
	 *
	 * @param new_depth
	 */
	 static void set_depth(double new_depth) {
		target_depth = new_depth;
	}


//what am i doing at 5am? why am i working on this
	/**
	 * Used to calculate values of outer four motors without IMU data. Used when
	 * stabilize = false
	 *
	 * @param current_depth
	 * @param target_depth
	 * @return
	 */
	static double * stable(double current_depth, double target_depth) {
		int C1 = 1;
		double d_m = ((target_depth - current_depth) * C1);
		double motors[] = { d_m, d_m, d_m, d_m };
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
	static double * stable(double current_depth, double target_depth, double roll, double pitch) {
		int K = 1; // constant multiplier for calibration
		double FLM = 1.0, FRM = 1.0, BLM = 1.0, BRM = 1.0; // front left motor,
															// etc. final value
															// is motor speed
		int base = 0; // base downward force to cancel out positive buoyancy
		int C1 = 1, C2 = 1, C3 = 1;// C1, C2, C3 used for calibration

		int depth_multiplier = (int) ((target_depth - current_depth) * C1);
		int pitch_multiplier = (int) ((0 - pitch) * C2); // can replace 0 with
															// number given by
															// IMU when level
		int roll_multiplier = (int) ((0 - roll) * C3);

		FLM *= (double) (K * (base + depth_multiplier + pitch_multiplier + roll_multiplier));
		FRM *= (double) K * (base + depth_multiplier + pitch_multiplier - roll_multiplier);
		BLM *= (double) K * (base + depth_multiplier - pitch_multiplier + roll_multiplier);
		BRM *= (double) K * (base + depth_multiplier - pitch_multiplier - roll_multiplier);

		double motors[] = { FLM, FRM, BLM, BRM };
		// for(int i = 0; i < 4; i++) System.out.println("mot" + motors[i]);
		return motors;
	}
	/**
	 * Used to calculate values of outer four motors.
	 *
	 * @return
	 */
	static double * stable() {
		if (mode == 5)
			return motors;
		if (stabilize) {
		  return stable(update::get_depth(), target_depth, update::IMU_roll(), update::IMU_pitch());
		} else {
		  return stable(update::get_depth(), target_depth);
		}
	}

	/**
	 * Returns if sub is facing within 4 degrees of direction dir. This
	 * direction is absolute(update.get_direction()), not relative
	 *
	 * @param dir
	 * @return
	 */
	 static int isFacing(int dir) {
	   if ((abs((double)(update::get_direction() - dir)) < 4)){
			return 1;
		}
		return 0;
	}

	/**
	 * Returns if sub is facing within 4 degrees of direction dir. This
	 * direction is relative, not absolute
	 *
	 * @param dir
	 * @return
	 */
	 static int isFacing_R(double dir) {
	   dir = (int)(dir + update::get_direction()) % 360;
	   if (abs(update::get_direction() - dir) < 4) {
			return 1;
		}
		return 0;
	}
		/**
		 * Turns sub in direction of dir. This direction is
		 * absolute(update.get_direction()), not relative (Updates mode to 1)
		 *
		 * @param dir
		 */
		static void face(int dir) {
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
		static void face_R(int dir) {
		  dir = (dir + update::get_direction()) % 360;
			target_direction = dir;
			if (!isFacing(dir)) {
				mode = 1;
			}
		}


	/**
	 * Sets target direction of sub without changing mode. This direction is
	 * absolute(update.get_direction()), not relative. Better to use face() or
	 * moveInDir().
	 *
	 * @param dir
	 */
	 static void set_dir(double dir) {
		target_direction = dir;
	}

	/**
	 * Sets target direction of sub without changing mode. This direction is
	 * relative, not absolute. Better to use face() or moveInDir().
	 *
	 * @param dir
	 */
	 static void set_dir_R(double dir) {
		target_direction = dir;
	}

	// @SuppressWarnings("unused")
	 static double * move(double dir, double dist) {
		// dir: 0= forward, negative is left, positive is right, from -180 to
		// 180
	   double LM = 1;
	   double  RM = 1;
		// double dist_mult = 5;
		double dir_mult = 2;
		double k = 2;
		if (dist > 50 && abs(dir) < 30) {
			LM = LM * k * (dist * dir_mult + dir_mult * dir);
			RM = RM * k * (dist * dir_mult - dir_mult * dir);
		} else if (abs(dir) >= 30) {
			LM = LM * k * (k + dir_mult * dir);
			RM = RM * k * (k - dir_mult * dir);
		} else if (dist < 50) {
			if (dist < 10) {
				LM = 0;
				RM = 0;
				logger::info("Reached destination, turning off motors");
			} else {
				LM = LM * k * (dist + dir_mult * dir);
				RM = RM * k * (dist - dir_mult * dir);
			}
		}
		double motors[] = { LM, RM };
		return motors;
	}

	static void stable_cal(){
		double roll_cal_total = 0;
		double pitch_cal_total = 0;
		for (int x = 0; x < 50; x++) {
		  pitch_cal_total += update::IMU_pitch();
		  roll_cal_total += update::IMU_roll();
		  //this_thread.sleep_for(5);
		  //sleep
		}
		// double roll_cal = roll_cal_total/100;//calibration for IMU
		// double pitch_cal = pitch_cal_total/100;
	}

	/**
	 * Main method of movable; calls stable() and internal_move(); Then sets
	 * motors to correct value using motorControle
	 */
	void norm() {
		int div = 0;
		/*while (run) {
			if (div == 10000)
				div = -1;
			div++;
			double tmp[] = stable();
			for (int i = 0; i < 4; i++) {
				motors[i] = tmp[i];
			}
			internal_move();
			if (basic.debug_lvl > 10) {
				logger.info(this.toString());
			}
			if ((basic.logger_lvl > 6 && div % 10 == 0) || (basic.logger_lvl > 9))
				debug.log(this.toString());
			motorControle.set_motors(motors);// TODO
			try {
				Thread.sleep(90);
			} catch (int e) {
				logger.error("Error in movable: "+e)
			}
		}
		*/
	}

	 void Run() {
	   logger::info("Initializing stabilization");
		stable_cal();
		// self test
		run = 1;
		init = 1;
		logger::info("Stabilization and motor function initialized");
		norm();
		logger::info("Shutting down sub stabilization");
	}



movable::~movable() {
	// TODO Auto-generated destructor stub
}
