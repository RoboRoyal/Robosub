package robosub;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.pi4j.system.SystemInfo;

import SonarUtil.SPI_int;
import SonarUtil.Search;
import SonarUtil.SonarExec;
import SonarUtil.Sonar_Test;

/**
 * Provides shell for user and parses config file and passed in arguments
 * 
 * @author Dakota
 *
 */
public class parser implements Runnable {
	Thread t;
	private static boolean log_parser = false;
	static boolean RUN = false;

	/**
	 * Main method of this class. Provides the interface for the shell and gets
	 * user input
	 */
	private void star() {
		Scanner in = new Scanner(System.in);
		displayWelcome();
		while (RUN) {
			try {
				System.out.print("> ");
				String line = in.nextLine();
				if (log_parser || basic.logger_lvl >= 4)
					debug.log("Parser line input: " + line);
				parser.parse(line);
			} catch (Exception e) {
				debug.print("Error in parser.star():" + e);
			}
		}
		in.close();
	}

	/**
	 * Displays the name and version
	 */
	private void displayWelcome() {
		displayName();
		System.out.println("Version: " + basic.VERSION_NUMBER);

	}

	/**
	 * Splits string x and parses each parameter
	 * 
	 * @param x
	 *            String to parse
	 */
	public static void parse(String x) {
		parse(x.split(" "));
	}

	/**
	 * Handles problems form set()
	 * 
	 * @param args
	 *            Arguments to parse
	 * @return
	 */
	public static boolean parse(String[] args) {
		try {
			set(args);
		} catch (IllegalArgumentException e) {
			if (!"End".equals(e.getMessage())) {
				System.out.println("Problem parsing command line arguments: " + e);
			}
			System.out.println("Usage: ");
			help(0);
			return false;
		} catch (InterruptedException e) {
			System.out.println("Problem in parser.parse(String[]): " + e);
		}
		return true;
	}

	/**
	 * Prints out help. Most help is in input/help.txt Best help from calling
	 * help in parse. help(2)
	 * 
	 * @param i
	 *            How much help you want
	 */
	private static void help(int i) {
		if (i == 0) {
			System.out.println("Please type help for more options");
			System.out.println("-t for max time(int)");
			System.out.println("-m for mode(int)");
			System.out.println("-ms [int] max speed");
			System.out.println("wait [int] delayed start in miliseconds");
			System.out.println("init [?]");
			System.out.println("wait [int] wait [#] miliseconds before starting");
			System.out.println("start to start prog");
			System.out.println("shut for shutdown");
			System.out.println("help for more info");
		} else if (i == 1) {
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
		} else {
			try (Scanner in = new Scanner(new File("input/help.txt"))) {
				String line = " ";
				while (in.hasNextLine() && !line.equals(("$"))) {
					System.out.println(line);
					line = in.nextLine();
				}
			} catch (Exception e) {
				System.out.println("Error parsing help file: " + e);
			}
		}
	}

	/**
	 * What does most of the actual parsing
	 * 
	 * @param arg
	 *            Arguments to parse
	 * @throws InterruptedException
	 */
	private static void set(String[] arg) throws InterruptedException {
		for (int x = 0; x < arg.length; x++) {
			if (basic.logger_lvl > 8)
				debug.log("Parsing input command: " + arg[x]);
			if (arg[x].contains("#")) {
				x = arg.length + 1;
				break;
			}
			switch (arg[x]) {
			case "test_sonar":
				long start = System.currentTimeMillis();
				Sonar_Test.testSonar();
				System.out.println("Time taken: " + (System.currentTimeMillis() - start));
				break;
			case "test_snav":
				long start2 = System.currentTimeMillis();
				System.out.println(SonarExec.lighterer());
				System.out.println("Time taken: " + (System.currentTimeMillis() - start2));
				break;
			case "showleft":
				Search.showLeft();
				break;
			case "":// to stop it from crashing for extra spaces
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
				try {
					System.out.println("Mode changed to: " + basic.mode_names[core.mode]);
				} catch (Exception e) {
					// guess you have invalid mode i don't know about. Enjoy
				}
				break;
			case "END":
				// End of config file
				debug.log("Successful cfg parsing\n*----------------------*\n");
				break;
			case "update_time":
				x++;
				update.setDelayTime(Integer.valueOf(arg[x]).shortValue());
				break;
			case "face":
				x++;
				movable.face(Integer.valueOf(arg[x]));
				break;
			case "surface":
				movable.surface();
				break;
			case "set_direction":// Better to use face() or MoveInDir()
			case "set_dir":
				x++;
				movable.set_dir(Integer.valueOf(arg[x]));
				break;
			case "log_time":
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				debug.print("*----------------------------*");
				debug.print("System time is: " + dateFormat.format(date));
				debug.log("System time is being logged now: " + System.currentTimeMillis());
				debug.log("Started by: " + System.getProperty("user.name"));
				debug.print("*----------------------------*\n");
				break;
			case "log_parser":
				x++;
				log_parser = isTrue(arg[x]);// arg[x].equalsIgnoreCase("true")
											// || arg[x].equalsIgnoreCase("t");
				break;
			case "set_logger_lvl":
				x++;
				basic.logger_lvl = valueOf(arg[x]);
				break;
			case "mark":
				DateFormat dateFormat3 = new SimpleDateFormat("HH:mm:ss");
				Date date3 = new Date();
				debug.print("Mark: " + dateFormat3.format(date3));
				break;
			case "pause":
				x++;
				pause(Integer.valueOf(arg[x]));
				break;
			case "shut":
				try {
					if (core.RUN) {
						basic.shutdown("Command line parser: 'shut'");
					} else if (core.INIT) {
						basic.shutdown("Command line parser: 'shut'");
					} else {
						System.out.println("Nothing to shut");
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				break;
			case "force":
				debug.logWithStack("Forced exit from console");
				System.exit(1);
				break;
			case "check":
				x++;
				System.out.println(core.check(valueOf(arg[x])));
				break;
			case "init":
				try {
					try {
						if (arg[x + 1].equals("-i")) {
							System.out.println("quick");
							core.init(true);
							x++;
						} else {
							core.init();
						}
					} catch (IndexOutOfBoundsException e) {
						core.init();
					}
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
				try {
					temp = SystemInfo.getCpuTemperature();
				} catch (Exception e) {
					System.out.println("Couldn't get temp: " + e.getLocalizedMessage());
				}
				System.out.println(temp);
				break;
			case "test":
				core.selfTest();
				break;
			case "allow_error":
				core.Seterror_allow(isTrue(arg[++x]));
				break;
			case "log_info":
				debug.log(core.info());
				break;
			case "log_start":
				DateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date2 = new Date();
				debug.log("*----------------------------*");
				debug.log("System started at: " + dateFormat2.format(date2));
				debug.log("System started at: " + System.currentTimeMillis());
				debug.log("Started by: " + System.getProperty("user.name"));
				debug.log("*----------------------------*\n");
				break;
			case "logTraffic":
				x++;
				update.logTraffic = isTrue(arg[x]);
				break;
			case "isreal":
				x++;
				update.useReal = isTrue(arg[x]);
				break;
			case "isreal?":
				System.out.println(update.useReal);
				break;
			case "shutOnFinish":
				x++;
				core.shutOnFinish = isTrue(arg[x]);// (arg[x].equalsIgnoreCase("true")
													// ||
													// arg[x].equalsIgnoreCase("t"));
				break;
			case "getSize":
				x++;
				System.out.println(Sonar_Test.getSize(arg[x]));
			case "waitForFinish":
				while (core.running) {
					Thread.sleep(100);
				}
				break;
			case "coms":
				System.out.println(update.self_test());
				break;
			case "wait":
				x++;
				try {
					Thread.sleep(Integer.valueOf(arg[x]));
				} catch (Exception e) {
				}
				break;
			case "-ws":
			case "waitStart":
				x++;
				core.wait_start(Integer.valueOf(arg[x]));
				break;
			case "i_wrote_this":
				x++;
				if (arg[x].equals("RoboRoyal")) {
					System.out.println("yes");
				} else {
					System.out.println("nope");
				}
				break;
			case "force_update_parseIn":
				x++;
				update.force_update_parseIn(arg[x]);
				break;
			/*
			 * case "enter": update.force_update_parseIn(arg[++x]); break;
			 */
			case "testSonar":
				// SonarInterface.updateDir();
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
				motorControle.motor_enable(valueOf(arg[x]), isTrue(arg[x + 1]));
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
				motorControle.setTopSpeed(valueOf(arg[x]));
				break;
			case "stabilize":
				x++;
				movable.stabilize(isTrue(arg[x]));
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
				debug.del__log__(true);
				break;
			case "exit":
				if (core.INIT) {
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
			case "save_sonar":
				SonarExec.saveFiles = isTrue(arg[++x]);
				break;
			case "mono_sonar":
				core.MONO = isTrue(arg[++x]);
				break;
			case "set_debug_lvl":
				x++;
				basic.debug_lvl = valueOf(arg[x]);
				break;
			case "puase_update":
				x++;
				update.puase(isTrue(arg[x]));
				break;
			case "puase_movable":
				x++;
				movable.puase(isTrue(arg[x]));
				break;
			case "mot":
				int[] motor_vals = { 1500, 1500, 1500, 1500, 1500, 1500 };
				try {
					x++;
					motor_vals[valueOf(arg[x])] = valueOf(arg[++x]);
					System.out.println("Setting motor of " + basic.MOTOR_LAYOUT[valueOf(arg[--x])] + " to " + arg[++x]);
				} catch (Exception e) {
					System.out.println("Turning off mototrs");
				}
				try {
					motorControle.set_motors(motor_vals);
				} catch (Exception e) {
					System.out.println("invalid: " + e.getMessage());
				}
				break;
			case "about":
				System.out.println("Duckbot v" + basic.VERSION_NUMBER + " by CMPE Robosub Team 2017-2018");
				break;
			case "turn_r":
				x++;
				movable.face_R(Integer.valueOf(x));
				break;
			case "echo":
				x++;
				System.out.println(arg[x]);
				break;
			case "parsefile":
				parseFile(arg[++x]);
				break;
			case "start":
				if (core.INIT) {
					basic.start_prog();
				} else {
					System.out.println("Can't start run mode, you have not initilized");
				}
				break;
			case "giveupdateinfo":
				for (int mine = 0; mine < 100; mine++) {
					System.out.println(update.ToString());
					Thread.sleep(500);
				}
				break;
			case "whatson":
				if (core.RUN) {
					System.out.println("Core");
				}
				if (update.RUN) {
					System.out.println("Update: Puased? " + update.puase);
				}
				if (movable.RUN) {
					System.out.println("Movable: Puased? " + movable.puase);
				}
				if (parser.RUN) {
					System.out.println("Parser");
				}
				if (SPI_int.RUN) {
					System.out.println("SPI_int");
				}
				if (SonarExec.RUN) {
					System.out.println("SonarExec");
				}
				if (SonarExec.running) {
					System.out.println("SonarExec sub func");
				}
				break;
			case "help":
			case "?":
				help(2);
				break;
			case "light":
				System.out.println(SonarExec.light());
			default:
				System.out.println("Invalid: " + arg[x]);
				throw new IllegalArgumentException("Invalid statment");
			}
		}
	}

	/**
	 * Transforms string to int
	 * 
	 * @param string
	 *            to convert to int
	 * @return int
	 */
	private static int valueOf(String string) {
		switch (string.toLowerCase()) {
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
		case "bottom_left":
			return 2;
		case "br":
		case "bottom_right":
			return 3;
		case "left":
		case "l":
			return 4;
		case "r":
		case "right":
			return 5;
		default:
			try {
				return Integer.valueOf(string);
			} catch (Exception e) {
				System.out.println("Expected an integer as argument, given '" + string + "'; setting to 0");
			}
		}
		return 0;
	}

	/**
	 * Checks for many different interpretations of true
	 * 
	 * @param x
	 *            is true?
	 * @return
	 */
	public static boolean isTrue(String x) {
		return (x.equalsIgnoreCase("true") || x.equalsIgnoreCase("t") || x.equalsIgnoreCase("tru")
				|| x.equalsIgnoreCase("treu"));
	}

	public static String a() {
		return "Duckbot v" + basic.VERSION_NUMBER + " by CMPE Robosub Team 2017-2018";
		// Dakota
	}

	public static void displayName() {
		String blackFile = "input/text_name.txt";
		try (Scanner in = new Scanner(new File(blackFile))) {
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				System.out.println(line);
			}
		} catch (Exception e) {
		}
	}

	public static void pause(int x) {
		try {
			Thread.sleep(x);
		} catch (Exception e) {
			System.out.println("Problem in parse.pause(): " + e);
		}
	}

	public static void parseFile(File f) {
		try (Scanner in = new Scanner(f)) {
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				parse(line);
			}
		} catch (Exception e) {
			System.out.println("Problem parsing file: " + f + ". Problem: " + e);
		}
	}

	public static void parseFile(String fileName) {
		try (Scanner in = new Scanner(new File(fileName))) {
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				parse(line);
			}
		} catch (Exception e) {
			System.out.println("Problem parsing file in parseFile(String): " + fileName + ". Problem: " + e);
		}
	}

	public void config() {
		String configFile = "input/config.cfg";
		parseFile(configFile);
	}

	public void run() {
		try {
			star();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		RUN = true;
		if (t == null) {
			t = new Thread(this, "parser");
			t.start();
		}
	}
}