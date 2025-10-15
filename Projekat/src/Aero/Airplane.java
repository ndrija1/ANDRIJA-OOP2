package Aero;

public class Airplane {
	 public final Flight flight;
	    public double currX, currY;

	    private final double startX, startY;
	    private final double endX, endY;
	    private final int departureSimTime; // Real-world duration in milliseconds

	    public Airplane(Flight flight, double startX, double startY, double endX, double endY, int departureSimTime) {
	        this.flight = flight;
	        this.startX = startX;
	        this.startY = startY;
	        this.endX = endX;
	        this.endY = endY;
	        this.departureSimTime = departureSimTime;

	        this.currX = startX;
	        this.currY = startY;
	    }

	    public boolean updatePosition(int currentSimTime) {
	        int elapsedTime = currentSimTime - this.departureSimTime;

	        if (elapsedTime >= this.flight.length) {
	            this.currX = this.endX;
	            this.currY = this.endY;
	            return false; // Flight has arrived
	        }

	        if (this.flight.length == 0) { // Avoid division by zero for instant flights
	            this.currX = this.endX;
	            this.currY = this.endY;
	            return false;
	        }

	        // Calculate progress as a value between 0.0 and 1.0
	        double progress = (double) elapsedTime / this.flight.length;
	        this.currX = this.startX + (this.endX - this.startX) * progress;
	        this.currY = this.startY + (this.endY - this.startY) * progress;
	        return true; // Flight is in progress
	    }
	}
