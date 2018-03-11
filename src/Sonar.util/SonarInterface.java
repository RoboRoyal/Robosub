package Sonar.util;

import java.io.File;
import java.util.Scanner;

public class SonarInterface {

	static int itteration = 1;
	static int dir = 0;
	static int bucket = -1;
	static String fileBase_left = "HydrophoneData/left_data_ID";
	static String fileBase_right = "HydrophoneData/right_data_ID";
	
	
	public static int updateDir(){
		String left_file_name = fileBase_left+itteration+".txt";
		String right_file_name = fileBase_right+itteration+".txt";
		if(!(new File(left_file_name).exists() && new File(right_file_name).exists())){
			System.out.println("Missing files");
			return 0;
		}else{
			return internalUpdate(left_file_name, right_file_name);
		}
	}
	
	
	private static int internalUpdate(String left_file_name, String right_file_name){
		
		//int bucket = 0;
		int left_size = getSize(left_file_name), right_size = getSize(right_file_name);
		if(left_size < right_size){
			Search.right = readIn(right_file_name, right_size);
			Search.left = readIn(left_file_name, right_size);
			System.out.println("Miss match sonar data sizes");
		}else if(left_size > right_size){
			Search.right = readIn(right_file_name, left_size);
			Search.left = readIn(left_file_name, left_size);
			System.out.println("Miss match sonar data sizes");
		}else{
			Search.right = readIn(right_file_name, right_size);
			Search.left = readIn(left_file_name, left_size);
		}
		
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
		return 0;
	}
	/*private static int[] readIn(String file){
		return readIn(file, getSize(file));
	}*/
	private static int[] readIn(String file, int size){
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
	private static int getSize(String file){
		int x = 0;
		try (Scanner in = new Scanner(new File(file))) {
			@SuppressWarnings("unused")
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
	
	
	public static int[][] update(int len_mili){
		int[] left = {0,1,0,1};
		int[] right = {1,4,5,3};
		int[] top = {1,2,3,2};
		int[][] out = {left,right,top};
		return (out);
		}
}

/*

*/