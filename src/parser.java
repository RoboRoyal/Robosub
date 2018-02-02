package robosub;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.pi4j.system.SystemInfo;

//import org.apache.log4j.Logger;
/**
 * Provides shell for user and parses config file and passed in arguments
 * @author Dakota
 *
 */
public class parser implements Runnable {
	Thread t;
	private static boolean log_parser = false;
	//private static Logger logger = Logger.getLogger(basic.class.getCanonicalName());
	static boolean RUN = true;

	private void star() {
		Scanner in = new Scanner(System.in);
		displayWelcome();
		while (RUN) {
			try{
				System.out.print("> ");
				String line = in.nextLine();
				if(log_parser || basic.logger_lvl > 3) debug.log("Parser line input: "+line);
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
				System.out.println("Problem parsing command line arguments");
				//logger.trace(e);
			}
			System.out.println("Usage: ");
			help(0);
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	private static void help(int i) {
		if(i ==0) System.out.println("Please type help for more options");
		System.out.println("-t for max time(int)");
		System.out.println("-m for mode(int)");
		System.out.println("-ms [int] max speed");
		System.out.println("wait [int] delayed start in miliseconds");
		System.out.println("init [?]");
		System.out.println("wait [int] wait [#] miliseconds before starting");
		System.out.println("start to start prog");
		System.out.println("shut for shutdown");
		if(i >= 1){
			System.out.println("stop stop lateral movement");
			System.out.println("send [string] to arduino, return answer");
			System.out.println("log [string] logs word");
			System.out.println("mot [int] starts motor at [#]");
			System.out.println("speed");
			System.out.println("-d [int] depth");
			System.out.println("off");
			System.out.println("forward");
			System.out.println("check [int]");
			System.out.println("set_dir");
			System.out.println("face");
			System.out.println("set bebug.logger level, self test, etc");
		}
	}

	private static void set(String[] arg) throws InterruptedException {
		//int speed = 100;
		for (int x = 0; x < arg.length; x ++) {
			if(basic.logger_lvl > 8) debug.log("Parsing input command: "+ arg[x]);
			if(arg[x].contains("#")){x=arg.length+1; break;}
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
			case "END":
				//End of config file
				debug.log("Successful cfg parsing");
				break;
			case "face":
				x++;
				movable.face( Integer.valueOf(arg[x]));
				break;
			case "set_dir":
				x++;
				movable.set_dir((double) Integer.valueOf(arg[x]));
				break;
			case "log_start":
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				debug.print("*----------------------------*");
				debug.print("System started at: "+dateFormat.format(date));
				debug.log("System started at: "+System.currentTimeMillis());
				debug.log("System started by: "+System.getProperty("user.name"));
				debug.print("*----------------------------*\n");
				break;
			case "log_parser":
				x++;
				log_parser = arg[x].equals("true");
				break;
			case "set_logger_lvl":
				x++;
				basic.logger_lvl = valueOf(arg[x]);
				break;
			case "pause":
				x++;
				pause(Integer.valueOf(arg[x]));
				break;
			case "shut":
				try {
					if(core.RUN){basic.shutdown("Command line parser: 'shut'");
					}else if(core.INIT){
						basic.shutdown("Command line parser: 'shut'");
					}else{
						System.out.println("Nothing to shut");
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
			case "print_tmp":
				float temp = -100;
				try{
					temp = SystemInfo.getCpuTemperature();
				}catch(Exception e){
					System.out.println("Couldn't get temp: " + e.getLocalizedMessage());
				}
				System.out.println(temp);
				break;
			case "test":
				core.selfTest();
				break;
			case "logTraffic":
				x++;
				update.logTraffic = (arg[x].equals("true") || arg[x].equals("t"));
				break;
			case "isReal":
				x++;
				update.useReal = (arg[x].equals("true") || arg[x].equals("t"));
				break;
			case "shutOnFinish":
				x++;
				core.shutOnFinish = (arg[x].equals("true") || arg[x].equals("t"));
				break;
			case "waitForFinish":
				while(core.running){
					Thread.sleep(100);
				}
				break;
			case "wait":
				x++;
				try{Thread.sleep(Integer.valueOf(arg[x]));}catch(Exception e){}
				break;
			case "-ws":
			case "waitStart":
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
			case "force_update_parseIn":
				x++;
				update.force_update_parseIn(arg[x]);
				break;
			case "name":
				displayName();
				break;
			case "nope":
				System.out.println("true");
				break;
			case "no_fill_ilv":
				x++;
				core.set_no_fill(arg[x].equals("true"));
				break;
			case "movableForceMode":
				x++;
				movable.forceMode(Integer.valueOf(arg[x]));
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
			case "motor_enable":
				x++;
				motorControle.motor_enable(valueOf(arg[x]),arg[x+1].equals("true)"));
				x++;
				break;
			case "is_run":
				System.out.println(core.RUN);
				break;
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
			case "stabilize":
				x++;
				movable.stabilize(arg[x].equals("true"));
				break;
			case "info":
				System.out.println(core.info());
				break;
			case "speed":
				x++;
				movable.setSpeed(valueOf(arg[x]));
				break;
			case "reverse":
			case "backup":
				movable.reverse();
				break;
			case "sidemove":
				x++;
				movable.side(valueOf(arg[x]));
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
			case "del_log_":
				//x++;
				debug.del__log__(true);
				break;
			case "exit":
				if(core.INIT){
					basic.shutdown("Command line parser: 'exit'");
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
				double[] motor_vals = { 1500, 1500, 1500, 1500, 1500, 1500 };
				try{
					x++;
					motor_vals[valueOf(arg[x])] = valueOf(arg[++x]);
					System.out.println("Setting motor of " + basic.MOTOR_LAYOUT[valueOf(arg[--x])] + " to " + arg[++x]);
				}catch(Exception e){
					System.out.println("Turning off mototrs");
				}
				try {
					motorControle.set_motors(motor_vals);
				} catch (Exception e) {
					System.out.println("invalid: "+e.getMessage());
				}
				break;
			case "about":
				System.out.println("Duckbot v"+basic.VERSION_NUMBER+" by CMPE Robosub Team 2017-2018");
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
					System.out.println("Can't start run mode, you have not initilized");
				}
				break;
			case "help":
			case "?":
				help(2);
				break;
			default:
				System.out.println("Invalid: " + arg[x]);
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
		try{Thread.sleep(x);}catch(Exception e){e.printStackTrace();}
	}
	public static void parseFile(File f){
		try (Scanner in = new Scanner(f)) {
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				parse(line);
			}
		}catch(Exception e){}
	}
	public static void parseFile(String fileName){
		try (Scanner in = new Scanner(new File(fileName))){
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				parse(line);
			}
		}catch(Exception e){}
	}
	
	public void config() {
		String blackFile = "input/config.cfg";
		try (Scanner in = new Scanner(new File(blackFile))) {
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				parse(line);
			}
		}catch(Exception e){
			System.out.println("Error; problem parsing cfg file: "+e);
		}	
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
