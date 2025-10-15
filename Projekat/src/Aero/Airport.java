package Aero;

public class Airport {
	
	private String Naziv;
	private String Kod;
	private double x,y;
	
    public boolean isFlickering = false; 
    
    public boolean isShown = true;

	
	public String getNaziv() {
		return Naziv;
	}
	public void setNaziv(String naziv) {
		Naziv = naziv;
	}
	public String getKod() {
		return Kod;
	}
	public void setKod(String kod) {
		Kod = kod;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public Airport(String naziv, String kod, double x, double y) {
		//super();
		Naziv = naziv;
		Kod = kod;
		this.x = x;
		this.y = y;
	}
	
	
	
}
