package Sonar.util;

import java.io.File;
import java.util.Scanner;

public class Sonar_Test {
	
	public static boolean testSonar(){
		int bucket = 0;
		long st = System.currentTimeMillis();
		Search.left=better("test_data/left_data_full_ID1.txt");
		Search.right=better("test_data/right_data_full_ID1.txt");
		//Search.left=readIn("test_data/left_data_full_ID1.txt",200000);
		//Search.right=readIn("test_data/right_data_full_ID1.txt",200000);
		System.out.println("Got data; "+Search.left.length+", "+Search.right.length);
		System.out.println("time = "+(System.currentTimeMillis()-st));
		//return true;/*
		try{
			bucket = Search.findBucket();
			System.out.println("Bucket is: "+bucket);
		}catch(Exception e){
			System.out.println("Fail1: "+e);
			e.printStackTrace();
		}
		try{
			int dir = Search.findDir(bucket);
			System.out.println("Dir is: "+dir);
		}catch(Exception e){
			System.out.println("Fail2: "+e);
			e.printStackTrace();
		}
		return true;
	}
	
	private static int[] better(String file){
		int size = 200000;
		int[] numbers = new int[size];
		try (Scanner in = new Scanner(new File(file))) {
			for(int index = 0;index < size;index++){
				numbers[index] = (int) (16384*in.nextDouble());
			}
		}catch(Exception e){
			System.out.println("Error; problem parsing data file: "+e);
		}
		return numbers;	
	}
	static int[] readIn(String file){
		return readIn(file, getSize(file));
	}
	public static int[] readIn(String file, int size){
		int[] numbers = new int[size];
		try (Scanner in = new Scanner(new File(file))) {
			for(int index = 0;index < size;index++){
				numbers[index] = (int) (16384*in.nextDouble());
			}
		}catch(Exception e){
			System.out.println("Error; problem parsing data file: "+e);
		}
		return numbers;	
	}
	
	public static int size(String file){
		int size = 0;
		try{
			System.out.println(new File(file).length());
		}catch(Exception e){
			System.out.println("Error; problem parsing data file: "+e);
		}
		return size;	
	}
	private static int getSize(String file){
		int x = 0;
		try (Scanner in = new Scanner(new File(file))) {
			double line;
			while (in.hasNextDouble()) {
				line = in.nextDouble();
				x++;
			}
		}catch(Exception e){
			System.out.println("Error; problem parsing test_data file: "+e);
		}	
		return x;
	}
	
	/*private static int[] parse(String data) {
		int size = 32767;//data.length()]
		String[] tmp = data.split("	");
		int[] numbers = new int[size];
		for(int index = 0;index < size;index++){
			numbers[index] = (int) (16384*Double.parseDouble(tmp[index]));
		}
		return numbers;
	}


	private static String getData(String file){
		try (Scanner in = new Scanner(new File(file))) {
			String line;
			while (in.hasNextLine()) {
				line = in.nextLine();
				return line;
				//parse(line);
			}
		}catch(Exception e){
			System.out.println("Error; problem parsing test_data file: "+e);
		}
		return null;	
	}*/

}
