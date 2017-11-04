package robosub;

import java.io.File;
import java.util.Scanner;

import org.apache.log4j.Logger;
/**
 * Provides shell for user and parses config file and passed in arguments
 * @author Dakota
 *
 */
public class parser implements Runnable {
	Thread t;
	private static Logger logger = Logger.getLogger(basic.class.getCanonicalName());
	static boolean RUN = true;

	private void star() {
		Scanner in = new Scanner(System.in);
		displayWelcome();
		while (RUN) {
			try{
				System.out.print("> ");
				String line = in.nextLine();
				if(basic.logger_lvl > 0) debug.log("Parser line input: "+line);
				parser.parse(line);
			}catch(Exception e){}
		}
		in.close();
	}

	private void displayWelcome() {
		displayName();
		System.out.println("Version: "+basic.VERSION_NUMBER);
		
	}

	static void parse(String x) {
		parse(x.split(" "));
	}

	public static boolean parse(String[] args) {
		try {
			set(args);
		} catch (IllegalArgumentException e) {
			if (!"End".equals(e.getMessage())) {
				logger.error("Problem parsing command line arguments");
				logger.trace(e);
			}
			logger.info("Usage: ");
			help(0);
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	private static void help(int i) {
		if(i ==0) logger.info("Please type help for more options");
		logger.info("-t for max time(int)");
		logger.info("-m for mode(int)");
		logger.info("-ms [int] max speed");
		logger.info("wait [int] delayed start in miliseconds");
		logger.info("init [?]");
		logger.info("wait [int] wait [#] miliseconds before starting");
		logger.info("start to start prog");
		logger.info("shut for shutdown");
		if(i >= 1){
			logger.info("stop stop lateral movement");
			logger.info("send [string] to arduino, return answer");
			logger.info("log [string] logs word");
			logger.info("mot [int] starts motor at [#]");
			logger.info("speed");
			logger.info("-d [int] depth");
			logger.info("off");
			logger.info("forward");
			logger.info("check [int]");
			logger.info("set_dir");
			logger.info("face");
			logger.info("set bebug.logger level, self test, etc");
		}
	}

	private static void set(String[] arg) throws InterruptedException {
		int speed = 100;
		for (int x = 0; x < arg.length; x ++) {
			switch (arg[x]) {
			case ""://to stop it from crashing for extra spaces
				break;
			case "max_time":
			case "-t":
				x++;
				core.setMaxTime(Integer.valueOf(arg[x]));
				break;
			case "mode":
			case "-m":
				x++;
				core.mode = valueOf(arg[x]);
				try{
					System.out.println("Mode changed to: "+basic.mode_names[core.mode]);
				}catch(Exception e){
					//guess you have invalid mode i don't know about. Enjoy
				}	
				break;
			case "face":
				x++;
				movable.face( Integer.valueOf(arg[x]));
				break;
			case "set_dir":
				x++;
				movable.set_dir((double) Integer.valueOf(arg[x]));
				break;
			case "pause":
				x++;
				pause(Integer.valueOf(arg[x]));
				break;
			case "shut":
				try {
					if(core.RUN){basic.shutdown();
					}else if(core.INIT){
						basic.shutdown();
					}else{
						logger.info("Nothing to shut");
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;
			case "check":
				x++;
				core.check(valueOf(arg[x]));
				break;
			case "init":
				try {
					core.init();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			case "prop":
				System.getProperties().list(System.out);
				break;
			case "err":
				x++;
				debug.error(arg[x]);
				break;
			case "test":
				core.selfTest();
				break;
			case "shutOnFinish":
				x++;
				core.shutOnFinish = (arg[x].equals("true") || arg[x].equals("t"));
			case "waitForFinish":
				while(core.running){
					Thread.sleep(100);
				}
				break;
			case "-ws":
			case "wait":
				x++;
				core.wait_start(Integer.valueOf(arg[x]));
				break;
			case "i_wrote_this":
				x++;
				if(arg[x].equals("RoboRoyal")){
					System.out.println("yes");
				}else{
					System.out.println("nope");
				}
				break;
			case "name":
				displayName();
				break;
			case "nope":
				System.out.println("true");
				break;
			case "-d":
				x++;
				movable.set_depth(valueOf(arg[x]));
				break;
			case "forward": 
				movable.move();
				break;
			case "stop":
				movable.stop();
				break;
			case "movable":
				System.out.println(movable.print());
				break;
			case "is_run":
				System.out.println(core.RUN);
			case "f_roll_v":
				x++;
				update.force_roll_value(valueOf(arg[x]));
				break;
			case "f_pitch_v":
				x++;
				update.force_pitch_value(valueOf(arg[x]));
				break;
			case "max_speed":
			case "-ms":
				x++;
				motorControle.max_speed = valueOf(arg[x]);
				break;
			case "speed":
				x++;
				speed = valueOf(arg[x]);
				break;
			case "send":
				x++;
				System.out.println(update.ard_force(arg[x]));
				break;
			case "log":
				x++;
				debug.log(arg[x]);
				break;
			case "log_err":
				x++;
				debug.log_err(arg[x]);
				break;
			case "de__log__":
				//x++;
				debug.del__log__(true);
				break;
			case "exit":
				if(core.INIT){
					basic.shutdown();
				}
				RUN = false;
				break;
			case "update_force_water":
					x++;
					update.waterLvl = Integer.valueOf(arg[x]);
					break;
			case "test_motors":
				movable.motorTest();
				break;
			case "set_debug_lvl":
				x++;
				basic.debug_lvl = valueOf(arg[x]);
				break;
			case "mot":
				double[] motor_vals = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
				try{
					x++;
					motor_vals[valueOf(arg[x])] = speed;
					System.out.println("Setting motor of " + basic.MOTOR_LAYOUT[valueOf(arg[x])] + " to " + speed);
				}catch(Exception e){
					System.out.println("Turning off mototrs");
				}
				motorControle.set_motors(motor_vals);
				break;
			case "turn_r":
				x++;
				movable.face_R(Integer.valueOf(x));
				break;
			case "echo":
				x++;
				System.out.println(arg[x]);
				break;
			case "start":
				if (core.INIT) {
					basic.start_prog();
				} else {
					logger.error("Can't start run mode, you have not initilized");
				}
				break;
			case "help":
			case "?":
				help(2);
				break;
			default:
				logger.error("Invalid: " + arg[x]);
				throw new IllegalArgumentException("Invalid statment");
			}
		}
	}

	private static int valueOf(String string) {
		switch(string.toLowerCase()){
		case "dog":
			return 101;
		case "sonar_nav":
			return 2;
		case "debug":
			return 3;
		case "test_move":
			return 4;
		case "dummy":
			return 8;
		case "cmp_sonar_nav":
			return 12;
		case "front_left":
		case "fl":
			return 0;
		case "front_right":
		case "fr":
			return 1;
		case "bl":
			return 2;
		case "br":
			return 3;
		case "left":
		case "l":
			return 4;
		case "r":
		case "right":
			return 5;
		default:
			try{
				return Integer.valueOf(string);
			}catch(Exception e){
				System.out.println("Expected an integer as argument, given '"+string+"'; setting to 0");
			}
		}
		return 0;
	}
	public static String a(){
		return "Duckbot v"+basic.VERSION_NUMBER+" by CMPE Robosub Team 2017-2018";
		//Dakota
	}
	public static void displayName(){
		String blackFile = "input/text_name.txt";
		try (Scanner in = new Scanner(new File(blackFile))) {
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				System.out.println(line);
			}
		}catch(Exception e){}
	}
	public static void pause(int x){
		try{Thread.sleep(x);}catch(Exception e){}
	}
	public void run() {
		try {
			star();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void start() {
		if (t == null) {
			t = new Thread(this, "parser");
			t.start();
		}
	}
}
