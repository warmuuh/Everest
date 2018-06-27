package com.rohitawate.everest.controllers;

public interface Searchable<T> {

	int getRelativityIndex(String searchString);

	T getDashboardState();

}