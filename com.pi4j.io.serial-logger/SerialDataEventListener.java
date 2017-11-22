package com.pi4j.io.serial;

public interface SerialDataEventListener extends java.util.EventListener{
	 void dataReceived(SerialDataEvent event);
}
