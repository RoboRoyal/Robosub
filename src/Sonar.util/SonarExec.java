package Sonar.util;

public class SonarExec implements Runnable {
	private boolean RUN = false;
	private Thread t;
	private pingerReader left, right;

	public void norm(){
		//first time
		//
		
		//LOOP:----------------
		//connection
		//wait for left
		//start left reader
		/*
		 * -when left done done, search for bucket
		 * -when right ready, start reading right. Verify correct version
		 */
		//when both are ready and bucket is found, start search
		//...
		//when search is done, update pinger connection
		//END LOOP:-----------
		
		//close everything with end()
	}
	public void setup(){
		left = new pingerReader("dosent.exist", "LEFT");
		right = new pingerReader("dosent.exist2", "RIGHT");
		//start py prog?
		//set up socket connection
		//test connect
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
				System.out.println("Problem making SonarExec:"+t);
			}
		} else {
			System.out.println("Trying to make second instance of SonarExec");
		}
	}

}
