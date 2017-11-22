package com.pi4j.io.serial;

import java.io.IOException;


public interface Serial extends AutoCloseable{
	public boolean isOpen();

	public default void write(String output) {
		SerialImp.log(output);
		
	}

	//public void addListener(SerialDataEventListener... listener);

	public default void open(String port, int br) throws IOException{
		SerialImp.del();
		SerialImp.log("Opened port: "+port+" at budrate: "+br);
	}

	public void close();

	

	public void addListener(SerialDataEventListener... listener);
}
