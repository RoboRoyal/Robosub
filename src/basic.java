package robosub;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

/**
 * Holds the most basic functions but mostly helps initiate and shutdown
 * Hands off control to CORE after init and start
 * @author Dakota
 *
 */
@SuppressWarnings("unused")
public class basic {
	public static String[] mode_names = {"0","0","sonar nav","debug","test move","test move 2","dummy multi","0","dummy","0","dummy 2","0","compitition sonar nav",};
	private static final int SONAR_NAV = 2;// normal sonar navigation
	private static final int DEBUG = 3;// tests systems
	private static final int TEST_MOVE = 4;// perform set motions
	private static final int TEST_MOVE_2 = 5;
	private static final int DUMMY_MULTI = 6;
	private static final int DUMMY = 8;// do basic setup and end
	private static final int DUMMY2 = 10;
	private static final int CMP_SONAR_NAV = 12;// Competition sonar nav
	private static boolean run = true;
	private static core me;
	public static final String[] MOTOR_LAYOUT = {"FL","FR","BL","BR","L","R"};
	public static final String VERSION_NUMBER = "1.0.4";
	public static int debug_lvl = 0;
	private static Logger logger = Logger.getLogger(basic.class.getCanonicalName());

	public static void main(String[] args) {
		BasicConfigurator.configure();
		if (args.length > 1) {
			System.out.print("I should add that in");
			parser.parse(args);
		}
		parser me = new parser();
		try{
			me.start();
		}catch(Exception e){
			logger.info("How did you manage to mess THAT up?"+e);
			e.printStackTrace();
		}
	}

	public static void shutdown() throws InterruptedException {
		core.shutdown();
		run = false;
		try {
			me.t.stop();
		} catch (NullPointerException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Successful shutdown");
		parser.log("Shutdown");
	}

	public static void start_prog() {
		start();
	}

	private static void start() {
		System.out.println("Started by: "+System.getProperty("user.name"));
		try {
			if (!core.INIT) {
				logger.info("Cant start without first initiating");
			}else{
				debug.blink();
				me = new core();
				me.start();
				Thread.sleep(20);
			}
		} catch (Exception e) {
			logger.error(e);
			debug.error(e.getMessage());
		}
	}

	private static void start_with_init() {
		// System.getProperties().list(System.out);
		debug.blink();
		try {
			if (!core.INIT) {
				core.init();
				Thread.sleep(800);
			}
		} catch (Exception e) {
			logger.error(e);
			debug.error(e.getMessage());
		}
		start();
	}
}
//Dakota Abernathy 2017
//CMPE Robosub capstone team
