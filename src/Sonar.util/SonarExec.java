package SonarUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import robosub.debug;
import robosub.movable;
import robosub.update;
import SonarUtil.SPI_int;

public class SonarExec implements Runnable {
	public static boolean RUN = false;
	public static boolean running = false;
	private Thread t;
	public static boolean saveFiles = false;
	static String left = "left.txt";
	static String right = "right.txt";

	@SuppressWarnings("unused")
	public static int correction(int dir, int size) {

		if (false)// change if direction needs to be reversed
			dir = -dir;

		if (size < 80 * 1000 && dir != 0) {
			while (size < 80 * 1000) {
				dir *= 1.05;
				size += 10000;
			}
		}

		return dir;
	}

	public static void save(String file, int[] data) {
		file = "sonarFiles/" + file;// adds dir
		StringBuilder temp = new StringBuilder();
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(file), true))) {
			for (int t : data)
				temp.append(t + "\n");
			logOut.write(temp.toString());
		} catch (IOException e) {
			System.out.print("Problem writing to file from SonarExec.save(): " + e);
		} finally {
			/* Finally */}
	}

	public static int[] convert(ArrayList<Integer> dataIn, int size) {
		final short con = 16;
		final short offset = 512 * 16;
		int[] out = new int[size];
		for (int i = 0; i < size; i++)
			out[i] = con * dataIn.get(i) - offset;
		return out;
	}

	public static int mono() {
		running = true;
		System.out.println("------------Next gen-----------");
		int dir = 0;
		int bucket = 0;
		SPI_int hydro = new SPI_int(0);
		try {
			hydro.mono();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		System.out.println("Got data origonal size: " + hydro.data.size() + ", " + hydro.data2.size());

		int min = -1;
		if (hydro.data.size() == hydro.data2.size())
			min = hydro.data.size();
		if (min < 0) {
			System.out.println("Error in sonarExec lighterer() : min is -1");
			return 0;
		}
		System.out.println("Min: " + min);
		// System.out.println("Difference:
		// "+(hydro.data.size()-hydro.data2.size()));
		if ((hydro.data.size() - hydro.data2.size()) > 225)
			debug.print("Poor size diff: " + (hydro.data.size() - hydro.data2.size()));

		if (min > 20 * 1000) {// need at least 20k samples to even do anything
			Search.left = convert(hydro.data, min);
			Search.right = convert(hydro.data2, min);
			System.out.println("Got data; " + Search.left.length + ", " + Search.right.length);
			if (saveFiles) {
				long tm = System.currentTimeMillis();
				save("left_" + tm + ".txt", Search.left);
				save("right_" + tm + ".txt", Search.right);
			}
			try {
				bucket = Search.findBucket();
				System.out.println("Bucket is: " + bucket);
			} catch (Exception e) {
				System.out.println("Fail1: " + e);
				e.printStackTrace();
			}
			try {
				dir = Search.findDir(bucket);
				// System.out.println("Dir is: " + dir);
			} catch (Exception e) {
				System.out.println("Fail2: " + e);
				e.printStackTrace();
			}
		} else {
			System.out.println("Too few samples to work: " + min);
		}
		debug.print("\n-----Sonar Info-----\nMin: " + min + "\nBucket: " + bucket + "\nDir: " + dir);
		running = false;
		return correction(dir, min);
	}

	public static int lighterer() {
		running = true;
		System.out.println("------------Next gen-----------");
		int dir = 0;
		int bucket = 0;
		SPI_int leftHydrophone = new SPI_int(0);
		SPI_int rightHydrophone = new SPI_int(1);
		// SPI_int right = new SPI_int(0);
		movable.puase(true);
		update.puase(true);
		leftHydrophone.start();
		rightHydrophone.start();
		try {
			leftHydrophone.t.join();
			rightHydrophone.t.join();
			leftHydrophone.shut();
			rightHydrophone.shut();
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		movable.puase(false);
		update.puase(false);
		System.out
				.println("Got data origonal size: " + leftHydrophone.data.size() + ", " + rightHydrophone.data.size());
		int min = leftHydrophone.data.size();
		if (min > rightHydrophone.data.size())
			min = rightHydrophone.data.size();
		min = min - 1;
		if (min < 0) {
			System.out.println("Error in sonarExec lighterer() : min is -1");
			return 0;
		}
		System.out.println("Min: " + min);
		System.out.println("Difference: " + (leftHydrophone.data.size() - rightHydrophone.data.size()));
		if ((leftHydrophone.data.size() - rightHydrophone.data.size()) > 225)
			debug.print("Poor size diff: " + (leftHydrophone.data.size() - rightHydrophone.data.size()));

		if (min > 20 * 1000) {
			Search.left = convert(leftHydrophone.data, min);
			Search.right = convert(rightHydrophone.data, min);
			System.out.println("Got data; " + Search.left.length + ", " + Search.right.length);
			if (saveFiles) {
				long tm = System.currentTimeMillis();
				save("left_" + tm + ".txt", Search.left);
				save("right_" + tm + ".txt", Search.right);
			}
			try {
				bucket = Search.findBucket();
				System.out.println("Bucket is: " + bucket);
			} catch (Exception e) {
				System.out.println("Fail1: " + e);
				e.printStackTrace();
			}
			try {
				dir = Search.findDir(bucket);
				// System.out.println("Dir is: " + dir);
			} catch (Exception e) {
				System.out.println("Fail2: " + e);
				e.printStackTrace();
			}
		} else {
			System.out.println("Too few samples to work: " + min);
		}
		debug.print("\n-----Sonar Info-----\nMin: " + min + "\nBucket: " + bucket + "\nDir: " + dir);
		running = false;
		return dir;
	}

	public static int lighter() {
		System.out.println("------------Next gen-----------");
		int dir = 0;
		int bucket = 0;
		try {
			Process p = Runtime.getRuntime().exec("python3 lighterPy.py ");
			// Process p2 = Runtime.getRuntime().exec("python3 right.py ");
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}

		int min = Sonar_Test.getSize(left);
		if (min > Sonar_Test.getSize(right))
			min = Sonar_Test.getSize(right);
		min = min - 1;
		System.out.println("Min: " + min);
		if (min < 13000) {
			Search.left = Sonar_Test.readInSmall(left, min);
			Search.right = Sonar_Test.readInSmall(right, min);
			System.out.println("Got data; " + Search.left.length + ", " + Search.right.length);
			try {
				bucket = Search.findBucket();
				System.out.println("Bucket is: " + bucket);
			} catch (Exception e) {
				System.out.println("Fail1: " + e);
				e.printStackTrace();
			}
			try {
				dir = Search.findDir(bucket);
				// System.out.println("Dir is: " + dir);
			} catch (Exception e) {
				System.out.println("Fail2: " + e);
				e.printStackTrace();
			}
		} else {
			System.out.print("Too few samples to work");
		}
		return dir;
	}

	public static int light() {
		int dir = 0;
		int bucket = 0;
		socketMe2 me = new socketMe2();
		me.start();
		System.out.println(me.send("start"));
		me.waitEnd();
		// System.out.println(me.send("close"));
		// me.close();
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}

		int min = Sonar_Test.getSize(left);
		if (Sonar_Test.size(left) > Sonar_Test.getSize(right))
			min = Sonar_Test.getSize(right);
		min = min - 1;
		System.out.println("Min: " + min);
		Search.left = Sonar_Test.readInSmall(left, min);
		Search.right = Sonar_Test.readInSmall(right, min);
		System.out.println("Got data; " + Search.left.length + ", " + Search.right.length);
		try {
			bucket = Search.findBucket();
			System.out.println("Bucket is: " + bucket);
		} catch (Exception e) {
			System.out.println("Fail1: " + e);
			e.printStackTrace();
		}
		try {
			dir = Search.findDir(bucket);
			System.out.println("Dir is: " + dir);
		} catch (Exception e) {
			System.out.println("Fail2: " + e);
			e.printStackTrace();
		}
		return dir;
	}

	public void norm() {
		// first time
		//

		// LOOP:----------------
		// connection
		// wait for left
		// start left reader
		/*
		 * -when left done done, search for bucket -when right ready, start
		 * reading right. Verify correct version
		 */
		// when both are ready and bucket is found, start search
		// ...
		// when search is done, update pinger connection
		// END LOOP:-----------

		// close everything with end()
	}

	public static void shutdown() {

	}

	public void setup() {
		// left = new pingerReader("dosent.exist", "LEFT");
		// right = new pingerReader("dosent.exist2", "RIGHT");
		// start py prog?
		// set up socket connection
		// test connect
	}

	public void run() {
		try {
			norm();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if (!RUN) {
			RUN = true;
			if (t == null) {
				t = new Thread(this, "SonarExec");
				setup();
				t.start();
			} else {
				System.out.println("Problem making SonarExec:" + t);
			}
		} else {
			System.out.println("Trying to make second instance of SonarExec");
		}
	}

}
