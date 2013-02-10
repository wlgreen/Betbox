package com.betbox.app.data;

public class Bet {
	public static final String PROPERTY_CONTENT = "Content";
	public static final String PROPERTY_TIME = "TimeOfCreation";
	public static final String PROPERTY_POOL = "StandPool";
	public static final String PROPERTY_STATUS = "status";
	public static final String STATUS_OPEN = "open";
	public static final String STATUS_CLOSE = "close";	
	public String id;
	public String content;
    public StandPool pool;
	public String creationTime;
	public String status;

	public Bet(String content) {
		this.content = content;
		this.pool = new StandPool();
		this.creationTime = "unknown";
		this.status = Bet.STATUS_OPEN;
	}
	
	public Bet(String content, String time, String pool, String status) {
		this.content = content;
		this.creationTime = time;
		this.pool = new StandPool(pool);
		if (status != null)
		this.status = status;
		else
			this.status = "";
	}
	
	@Override
	public String toString() {
		String output = content + "\n (" + pool.toString() + ") \n" + "Created at " + creationTime;
		if (status.equals(STATUS_OPEN)) {
			output += "\n Still open";
		} else if (status.equals(STATUS_CLOSE))
			output += "\n Closed";
		return output;
	}
}
