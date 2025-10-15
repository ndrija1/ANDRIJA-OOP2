package Aero;

public class Flight {
	private String odAerodroma,doAerodroma;
	private String vremePoletanja;
	int length;
	
	
	public Flight(String odAerodroma, String doAerodroma, String vremePoletanja, int trajanje) {
		//super();
		this.odAerodroma = odAerodroma;
		this.doAerodroma = doAerodroma;
		this.vremePoletanja = vremePoletanja;
		this.length = trajanje;
	}
	
	public String getOdAerodroma() {
		return odAerodroma;
	}
	public void setOdAerodroma(String odAerodroma) {
		this.odAerodroma = odAerodroma;
	}
	public String getDoAerodroma() {
		return doAerodroma;
	}
	public void setDoAerodroma(String doAerodroma) {
		this.doAerodroma = doAerodroma;
	}
	public String getVremePoletanja() {
		return vremePoletanja;
	}
	public void setVremePoletanja(String vremePoletanja) {
		this.vremePoletanja = vremePoletanja;
	}
	public int getTrajanje() {
		return length;
	}
	public void setTrajanje(int trajanje) {
		this.length = trajanje;
	}
	
	
}
