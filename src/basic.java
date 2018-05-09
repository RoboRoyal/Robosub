package robosub;

//import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//import Sonar.util.*;

//import org.apache.log4j.BasicConfigurator;

/**
 * Holds the most basic functions but mostly helps initiate and shutdown
 * Hands off control to CORE after init and start
 * @author Dakota
 *
 */
@SuppressWarnings("unused")
public class basic {
	public static final String[] mode_names = {"0","End Fast","sonar nav","debug","test move","test move 2","dummy multi","0","dummy","0","dummy 2","0","compitition sonar nav",};
	private static final int SONAR_NAV = 2;// normal sonar navigation
	private static final int DEBUG = 3;// tests systems
	private static final int TEST_MOVE = 4;// perform set motions
	private static final int TEST_MOVE_2 = 5;
	private static final int DUMMY_MULTI = 6;
	private static final int DUMMY = 8;// do basic setup and end
	private static final int DUMMY2 = 10;//debug
	private static final int CMP_SONAR_NAV = 12;// Competition sonar nav
	private static boolean run = true;
	private static core me;//this is to keep track of the thread so we can stop it for shutdown
	public static final String[] MOTOR_LAYOUT = {"FL","FR","BL","BR","L","R"};
	public static final String VERSION_NUMBER = "1.5.6";//now with working sonar nav!
	public static int debug_lvl = 0; 
	public static int logger_lvl = 5;
	private static boolean exitBefore = false;
	//private static Logger logger = Logger.getLogger(basic.class.getCanonicalName());

	/*TODO
	 * add in option for run without parser thread
	 * better start and shut with NEW core
	 * better variable handling
	 * startup/config NEW debug(GPIO)
	 * 
	 */
	
	public static void main(String[] args) {
		try{
			master(args);
			//SonarInterface.updateDir();
		}catch(Throwable e){//trust me, catch throwable jic
			e.printStackTrace();
			System.out.println(e);
			debug.error(e.getMessage());
		}finally{
			System.out.println("End of Main");
		}

	}

	private static void master(String[] args) { 
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		debug.log("\n*----------------------------*");
		debug.log("System started at: "+dateFormat.format(date));
		debug.log("System started at: "+System.currentTimeMillis());//This wont print anything if log is deleted in config
		if (args.length > 1) {
			System.out.print("I should add that in, okay, hold on, ok, there, I did it");
			parser.parse(args);
		}
		parser me2 = new parser();
		me2.config();
		try{
			me2.start();
			if(core.running || core.RUN || core.INIT){
				System.out.print("Parser ended while system still running");
				//You can comment this out if you need to, but generally it means you messed up your config file
				throw new Exception("Parser ended while system still running");
			}
		}catch(Exception e){
			System.out.print("How did you manage to mess THAT up?: "+e);
			e.printStackTrace();
			debug.error("They managed to mess up start: "+e.getMessage());
			//Should probably exit, but... whatever. youre prob, not mine
		}
		
	}

	public static void shutdown(String why) throws InterruptedException {
		if(exitBefore){//If this happens, this isnt the first time the sub tried to shut down
			System.out.println("Program in cyclic shutdown cycle. Force ending. Error: "+why);
			//movable.stop();//try one last time to save the sub
			//movable.surface();
			//try{Thread.sleep(101);}catch(Throwable e){}//there is no point in trying to catch an error here
			debug.logWithStack("Program in cyclic shutdown cycle. Force ending. Error: "+why);
			System.exit(1);//This is bad and there is no saving this
		}
		exitBefore = true;
		core.shutdown(why);
		run = false;
		try {
			Thread.sleep(150);//allow time for thread to end correctly
			if(core.t!=null) core.t.stop();//may be not needed-but ensure it ends
			core.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Successful shutdown");
		debug.log("Successful shutdown: with stack");
		//System.exit(0);//IDK why but sometimes this is needed? Should fix, didn;t use to need this
	}

	public static void start_prog() {
		start();
	}

	private static void start() {
		debug.print("Started by: "+System.getProperty("user.name"));
		try {
			if (!core.INIT) {
				System.out.print("Cant start without first initiating");
				throw new Exception("basic.start() called without first inititilizing");
			}else{
				if(core.running)
					throw new Exception("Core already running??: running");
				debug.blink();
				if (me == null){
					me = new core();
					me.start();
				}else{
					me.run();
				}
				Thread.sleep(20);
			}
		} catch (Exception e) {
			System.out.print(e);
			debug.log_err(e.getMessage());
		}
	}

	private static void start_with_init() {
		debug.blink();
		try {
			if (!core.INIT) {
				core.init();
				Thread.sleep(800);
			}
		} catch (Exception e) {
			System.out.print(e);
			debug.error(e.getMessage());
		}
		start();
	}
}
//Dakota Abernathy 2017
//CMPE Robosub capstone team