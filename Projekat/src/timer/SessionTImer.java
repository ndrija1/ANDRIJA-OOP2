package timer;


import java.awt.Frame;
import java.util.Timer;
import java.util.TimerTask;

import Dialogs.WarningDialog;

public class SessionTImer {
	private final Frame owner;
	private final int TIMEOUT_SECONDS=60;
	private final int WARNING=5;
	
	private Timer timer;
	private int secondsRemaining;
	
    public SessionTImer(Frame owner) {
        this.owner = owner;
        this.secondsRemaining = TIMEOUT_SECONDS;
    }
    
    
    public synchronized void start() {
    	if (timer != null) {
			timer.cancel();
		}
    	
    	timer=new Timer();
    	secondsRemaining=TIMEOUT_SECONDS;
    	
    	timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				secondsRemaining--;
				
				if(secondsRemaining==WARNING) {
					timer.cancel();
					WarningDialog dialog=new WarningDialog(owner, ()->resetAndStart(), () -> logout());
					dialog.setVisible(true);
				}
				if(secondsRemaining<=0) {
					logout();
				}
			}
		},1000,1000);
    }
    public synchronized void reset() {
        System.out.println("User activity detected. Resetting session timer.");
        secondsRemaining = TIMEOUT_SECONDS;
    }
    public synchronized void stop() {
    	if(timer!=null) {
    		timer.cancel();
    	}
    }
    
    private void logout() {
    	System.out.println("Timed out");
    	stop();
    	owner.dispose();
    }
    private synchronized void resetAndStart() {
        reset();
        start();
    }
 


    
}
