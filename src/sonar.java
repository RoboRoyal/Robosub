package robosub;

import org.apache.log4j.Logger;

/**
 * All things sonar, one I figure that out
 * 
 * @author Dakota
 *
 */
class sonar implements Runnable {
	private static Logger logger = Logger.getLogger(sonar.class.getCanonicalName());
	Thread t;
	static int target_freq = 35000;
	static int pinger_depth = 10;
	static int pinger_dir = 0;
	static int pinger_dist = 50;
	private static boolean init = false;
	private static boolean run = false;

	public static String print() {
		String re = "Sonar: ";
		re += " Pinger diection: " + pinger_dir;
		re += " Pinger distance: " + pinger_dist;
		re += " Pinger depth " + pinger_depth;
		re += " Target freq " + target_freq;
		return re;
	}

	public String toString() {// self print
		return print();
	}
	
	public static void stop() {
		run = false;
	}

	public static void set_target_freq(int freq) {
		target_freq = freq;
	}

	public static int get_pinger_depth() {
		if (!init) {
			debug.error("Accessing sonar without first initiating 1 ");
		}
		return pinger_depth;
	}

	public static int get_pinger_dir() {
		if (!init) {
			debug.error("Accessing sonar without first initiating 2 ");
		}
		return pinger_dir;
	}

	public static int get_pinger_dist() {
		if (!init) {
			debug.error("Accessing sonar without first initiating 3 ");
		}
		return pinger_dist;
	}

	public static void sonar_calc_simple(int freq) {
		// (left_in, right_in, top_in)
		// first signal is at t=0, counting difference in time in seconds(or
		// nano)
		// int freq = 35000;//target freq
		Long left_time = (long) 0, right_time = (long) 0, top_time = (long) 0;

		/*
		 * while(left_in == 0 || right_in == 0 || top_in == 0){ Update
		 * (right_in, left_in,top_in);//gets time when freq is detected }
		 */

		int C = 1484; // speed of sound in water (m/s)
		double dist2 = 1.0; // distance between microphones (meters)

		double dir = Math.asin((left_time - right_time) * (C / dist2));
		int dpth = (int) (((dir / 2) - top_time) * (C / dist2));

		pinger_dir = (int) dir;
		pinger_depth = dpth;
		pinger_dist = (int) ((left_time + right_time + top_time) * (1 / 3) * (C / dist2));

	}

	private void sonar_run() {
		while (run) {
			// take pinger_freq
			// update all pinger values
			// repeat
			sonar_calc_simple(target_freq);
			try {
				Thread.sleep(90);
			} catch (Exception e) {
				debug.logWithStack("Thread interupt");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		logger.info("Initilizing sonar");
		// run set up to initiate sonar
		// self test
		if (sonar_init() && sonar_self_test()) {
			run = true;
			init = true;
			logger.info("Sonar initilizing compleate");
			sonar_run();
			logger.info("Shutting down sonar");
		} else {
			logger.error("Problem initiating sonar");
		}

	}

	private boolean sonar_self_test() {
		try {
			double p = update.sonar_dir(0);
			Thread.sleep(200);
			if (p != update.sonar_dir(0)) {
				return false;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean sonar_init() {
		// TODO Auto-generated method stub
		return true;
	}

	public void start() {
		if (t == null) {
			t = new Thread(this, "sonar");
			t.start();
		}else{
       	 debug.logWithStack("Second instance being made: update");
        }
	}
}
