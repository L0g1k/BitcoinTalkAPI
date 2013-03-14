package dev.bitcoin.bitcointalk.model.appengine;

import java.util.Date;

public interface HasFreshness {

	public Date getLastUpated();
	public int getFreshnessTime();
	public String getId();
	public boolean isBeingUpdated();
	public void setBeingUpdated(boolean updated);
}
