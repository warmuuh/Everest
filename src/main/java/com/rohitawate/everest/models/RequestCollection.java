package com.rohitawate.everest.models;

import java.util.List;

public class RequestCollection {

	private String id;
	
	private String name;
	
	private String description;
	
	private List<DashboardState> requests;

	public RequestCollection() {
	}

	public RequestCollection(String name) {
		super();
		this.name = name;
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<DashboardState> getRequests() {
		return requests;
	}

	public void setRequests(List<DashboardState> requests) {
		this.requests = requests;
	}
	
}
