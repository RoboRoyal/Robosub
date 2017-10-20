package robosub;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class parser implements Runnable {
	Thread t;
	private static Logger logger = Logger.getLogger(basic.class.getCanonicalName());
	static boolean RUN = true;

	private void star() {
		Scanner in = new Scanner(System.in);
		while (RUN) {
			try{
				System.out.print("> ");
				parser.parse(in.nextLine());
			}catch(Exception e){}
		}
		in.close();
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
		}	
	}

	private static void set(String[] arg) throws InterruptedException {
		int speed = 100;
		for (int x = 0; x < arg.length; x += 2) {
			switch (arg[x]) {
			case "max_time":
			case "-t":
				core.setMaxTime(Integer.valueOf(arg[x + 1]));
				break;
			case "mode":
			case "-m":
				core.mode = Integer.valueOf(arg[x + 1]);
				break;
			case "shut":
				try {
					basic.shutdown();
					//RUN = false;
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;
			case "check":
				core.check(valueOf(arg[x + 1]));
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
			case "-ws":
			case "wait":
				core.wait_start(Integer.valueOf(arg[x + 1]));
				break;
			case "i_wrote_this":
				if(arg[x + 1].equals("RoboRoyal")){
					System.out.println("yes");
				}else{
					System.out.println("nope");
				}
				break;
			case "nope":
				System.out.println("true");
				break;
			case "-d":
				movable.set_depth(Integer.valueOf(arg[x + 1]));
				break;
			case "forward": 
				movable.move(speed);
				break;
			case "stop":
				movable.stop();
				break;
			case "max_speed":
			case "-ms":
				motorControle.max_speed = valueOf(arg[x + 1]);
				break;
			case "speed":
				speed = valueOf(arg[x + 1]);
				break;
			case "send":
				System.out.println(update.ard_force(arg[x+1]));
				break;
			case "log":
				log(arg[x+1]);
				break;
			case "exit":
				RUN = false;
				break;
			case "set_debug_lvl":
				basic.debug_lvl = valueOf(arg[x + 1]);
				break;
			case "mot":
				double[] motor_vals = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
				try{
					motor_vals[valueOf(arg[x + 1])] = speed;
					System.out.println("Setting motor of " + basic.MOTOR_LAYOUT[valueOf(arg[x + 1])] + " to " + speed);
				}catch(Exception e){
					System.out.println("Turning off mototrs");
				}
				motorControle.set_motors(motor_vals);
				break;
			case "echo":
				System.out.println(valueOf(arg[x + 1]));
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
		case "fl":
			return 0;
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
	public static void log(String me) {
		String logFile = "output/logFile.txt";
		StringBuilder temp = new StringBuilder();
		try (Scanner in = new Scanner(new File(logFile))) {
			while (in.hasNextLine()) {
				temp.append(in.nextLine() + "\n");
			}
		} catch (IOException e) {
			logger.error("Problem reading from: " + e);
		}
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(logFile)))) {
			temp.append(me);
			logOut.write(temp.toString());
		} catch (IOException e) {
			logger.error("Problem reading from: " + e);
		}
	}
	public static String a(){
		return "Duckbot by CMPE Robosub Team: "+basic.VERSION_NUMBER;
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
