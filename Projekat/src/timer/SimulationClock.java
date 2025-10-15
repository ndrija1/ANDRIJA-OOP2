package timer;

import java.awt.EventQueue;
import java.awt.Label;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SimulationClock extends Thread {
	private Label displayLabel;
	private volatile boolean isRunning=false;
	private int simMinutes=0;
	private int simHours=0;
	
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
	
	public SimulationClock(Label label) {
		this.displayLabel=label;
		setDaemon(true);
	}
	
	@Override
	public void run() {
		try {
			while (!isInterrupted()) {
                // This synchronized block ensures the thread will wait if not running
                synchronized (this) {
                    while (!isRunning) {
                        wait(); // Wait for a 'go()' signal
                    }
                }

				Thread.sleep(1000);
				simMinutes+=10;
				if(simMinutes>=60) {
					simHours+=simMinutes/60;
					simMinutes%=60;
				}
				 EventQueue.invokeLater(() -> {
	                    if (displayLabel != null) { // Defensive check
	                        displayLabel.setText(toString());
	                    }
	                });			}
			
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
	}
    public synchronized void go() {
        if (!isRunning) {
            isRunning = true;
            notify(); // Wake up the waiting thread
        }
    }

    public synchronized void pause() {
        isRunning = false;
    }

    public synchronized void reset() {
        isRunning = false;
        simHours = 0;
        simMinutes = 0;
        EventQueue.invokeLater(() -> {
            if (displayLabel != null) {
                displayLabel.setText(toString());
            }
        });
    }
    public int getTotalSimMinutes() {
        return (simHours * 60) + simMinutes;
    }
    @Override
    public String toString() {
        return String.format("%02d:%02d", simHours, simMinutes);
    }
}
