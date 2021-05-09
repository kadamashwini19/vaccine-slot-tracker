/**
 * 
 */
package com.cowin.finder;

import java.util.Timer;

/**
 * @author admin
 *
 */
public class SlotNotifier {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Vaccing Tracker Started....");
		Timer time = new Timer();               // Instantiate Timer Object
		Scheduler scheduler = new Scheduler();  // Instantiate Scheduler class
        scheduler.getConfigurationFileDetails(); // read config file
		time.schedule(scheduler, 0, 1000*60);  // Repeat tracker after one minute         
	}
	
}
