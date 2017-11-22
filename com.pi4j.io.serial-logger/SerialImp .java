package com.pi4j.io.serial;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SerialImp implements Serial {
	static String last = "dumb";
	static int packetNum = -1;
	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(SerialDataEventListener... listener) {
		// TODO Auto-generated method stub
		
	}

	public static void log(String me) {
		packetNum++;
		if(me.equals(last)){
			return;
		}
		last = me;
		String logFile = "output/SerialOutFile.txt";
		StringBuilder temp = new StringBuilder();
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(logFile),true))) {
			if(packetNum!=0) temp.append("Packet "+packetNum+": ");
			temp.append(me+"\n");
			logOut.write(temp.toString());
		} catch (IOException e) {
			System.out.print("Problem writing to: " + e);
		}finally{/*Finally*/}
	}

	public static void del() {
		String logFile = "output/SerialOutFile.txt";
		StringBuilder temp = new StringBuilder();
		try (Writer logOut = new BufferedWriter(new FileWriter(new File(logFile)))) {
			temp.append("\n");
			logOut.write(temp.toString());
		} catch (IOException e) {
			System.out.print("Problem writing to: " + e);
		}finally{/*Finally*/}
	}
		
}
