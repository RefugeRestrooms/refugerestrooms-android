package org.refugerestrooms.android.model;

/**
 * Placeholder
 * @author jmp
 */
public class Bathroom {

	//TODO Other fields
	private String mName;

	public Bathroom(String name) {
		super();
		this.mName = name;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}
	
	@Override
	public String toString() {
		return mName;
	}
	
}
