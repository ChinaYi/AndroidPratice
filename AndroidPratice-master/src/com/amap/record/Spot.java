package com.amap.record;

public class Spot {
	private String spotName;
	private double size;
	private double spotLat;
	private double spotLng;
	private int mid = 0;
	
	
	public Spot() {
		super();
	}
	
	public Spot(String spotName, double size, double spotLat, double spotLng, int mid) {
		super();
		this.spotName = spotName;
		this.size = size;
		this.spotLat = spotLat;
		this.spotLng = spotLng;
		this.mid = mid;
	}
	
	public int getMid() {
		return mid;
	}

	public void setMid(int mid) {
		this.mid = mid;
	}



	public String getSpotName() {
		return spotName;
	}
	public void setSpotName(String spotName) {
		this.spotName = spotName;
	}
	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}
	public double getSpotLat() {
		return spotLat;
	}
	public void setSpotLat(double spotLat) {
		this.spotLat = spotLat;
	}
	public double getSpotLng() {
		return spotLng;
	}
	public void setSpotLng(double spotLng) {
		this.spotLng = spotLng;
	}

	@Override
	public String toString() {
		return "Spot [spotName=" + spotName + ", size=" + size + ", spotLat=" + spotLat + ", spotLng=" + spotLng + "]";
	}
	
	
	
}
