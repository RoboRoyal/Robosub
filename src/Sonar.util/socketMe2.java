package SonarUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class socketMe2 implements Runnable {
	
	static Thread t;
	static Socket socket;
	static int x = 0;
	static boolean send = false;
	static boolean RUN = true;
	static String msg = "";
	String ret = "none";

    public void waitEnd(){
    	System.out.println("Waiting for end");
	while(!ret.equalsIgnoreCase("finished")){
		try{Thread.sleep(10);}catch(Exception e){}
	    }
    }
	public void close(){
	    RUN = false;
	    try{socket.close();}catch(Exception e){}
	}
    
	public String send(String message){
		msg = message;
		send = true;
		try{Thread.sleep(100);}catch(Exception e){}
		return ret;
	}
	
	public void core() {
		try {
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			while (RUN) {
				while (!send){
					ret = stdIn.readLine();
					Thread.sleep(10);
				}
				send = false;
				out.print(msg);
				out.flush();
				// System.out.println("Trying to read...");
				ret = stdIn.readLine();
				//System.out.println(stdIn.readLine());
				//out.print("Hello son!" + x + "\r\n");
				//x++;
				

			}

		} catch (IOException e) {
			System.out.println("That failed: " + e);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("That failed2: " + e);
		}

	}
	
	
	
	
	public socketMe2(){
		try {
			socket = new Socket("localhost", 9901);
		} catch (Exception e) {
			System.out.println("That failed hard: " + e);
		}
	}
	
	public void run() {
		try {
			core();
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

