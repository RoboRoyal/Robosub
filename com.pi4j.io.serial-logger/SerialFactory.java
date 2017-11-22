package com.pi4j.io.serial;

public class SerialFactory {

	public static Serial createInstance() {
		return new SerialImp();
	}

}
