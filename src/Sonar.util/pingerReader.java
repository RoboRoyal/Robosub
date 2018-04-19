package Sonar.util;

import java.io.File;
import java.util.Scanner;

public class pingerReader{

	private String fileIn;
	private String name = "Unnamed";
	private short state = 0;//0 = uninitiated
	
	public pingerReader(String me, String nom){
		fileIn = me;
		name = nom;
		state = 1;
	}
	
	public void NewFile(String fileNext){
		fileIn = fileNext;
	}
	
	private int[] better(String file){
		state = 2;
		int size = 200000;
		int[] numbers = new int[size];
		try (Scanner in = new Scanner(new File(file))) {
			for(int index = 0;index < size;index++){
				numbers[index] = (int) (16384*in.nextDouble());
			}
		}catch(Exception e){
			state = -1;//error state
			System.out.println("Error; problem parsing data file from pingerReader: "+e);
		}
		state = 3;
		return numbers;	
	}
}
