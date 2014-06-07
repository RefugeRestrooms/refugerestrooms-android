package org.refugerestrooms.android.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import android.location.Address;

/**
 * Placeholder
 * @author jmp
 */
public class Bathroom {
	
	//TODO Other fields
	@SerializedName("name")
	private String mName;
	@SerializedName("address")
	private Address mAddress;
	@SerializedName("accessible")
	private boolean mAccessible;
	@SerializedName("unisex")
	private boolean mUnisex;
	@SerializedName("directions")
	private String mDirections;
	@SerializedName("comments")
	private String mComments;
	@SerializedName("score")
	private int mScore;

	public Bathroom(String mName, Address mAddress, boolean mAccessible,
			boolean mUnisex, String mDirections, String mComments, int score) {
		this.mName = mName;
		this.mAddress = mAddress;
		this.mAccessible = mAccessible;
		this.mUnisex = mUnisex;
		this.mDirections = mDirections;
		this.mComments = mComments;
		this.mScore = score;
	}

	public String getName() {
		return mName;
	}

	public Address getAddress() {
		return mAddress;
	}

	public boolean isAccessible() {
		return mAccessible;
	}

	public boolean isUnisex() {
		return mUnisex;
	}

	public String getDirections() {
		return mDirections;
	}

	public int getScore() {
		return mScore;
	}

	public String getComments() {
		return mComments;
	}

	public void setName(String name) {
		this.mName = name;
	}
	
	@Override
	public String toString() {
		return mName;
	}
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this, Bathroom.class);
	}
	
	public static Bathroom fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, Bathroom.class);
	}
}
