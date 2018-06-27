package com.rohitawate.everest.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import com.jfoenix.controls.JFXButton;
import com.rohitawate.everest.misc.Services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public abstract class AbstractSearchablePaneController<T> implements Initializable {

	@FXML
	private StackPane promptLayer, searchLayer, searchFailedLayer;

	@FXML
	private JFXButton clearSearchFieldButton;

	@FXML
	private TextField searchTextField;

	@FXML
	private VBox searchTab, searchBox, searchPane;

	private List<Searchable<T>> searchableItems;

	
	protected static class SearchEntry<T> {
		private final Parent fxmlItem;
		private final Searchable<T> searchable;
		public SearchEntry(Parent fxmlItem, Searchable<T> searchable) {
			super();
			this.fxmlItem = fxmlItem;
			this.searchable = searchable;
		}
		public Parent getFxmlItem() {
			return fxmlItem;
		}
		public Searchable<T> getSearchable() {
			return searchable;
		}
		
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		searchableItems = new ArrayList<>();
		searchLayer.visibleProperty().bind(searchTextField.textProperty().isNotEmpty());

		searchTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
			searchBox.getChildren().remove(0, searchBox.getChildren().size());
			searchFailedLayer.setVisible(false);
			List<Searchable<T>> searchResults = getSearchResults(searchTextField.getText());

			//TODO: this is calculating relativityIndex again
			searchResults.sort((controller1, controller2) -> {
				int relativity1 = controller1.getRelativityIndex(searchTextField.getText());
				int relativity2 = controller2.getRelativityIndex(searchTextField.getText());
				return relativity2 - relativity1;
			});

			if (searchResults.size() != 0) {
				for (Searchable<T> controller : searchResults) {
					addSearchItem(controller.getDashboardState());
				}
			} else {
				searchFailedLayer.setVisible(true);
			}
		}));

		clearSearchFieldButton.setOnAction(e -> searchTextField.clear());

		Platform.runLater(this::loadInitialItemsAsync);
	}
	
	
	private void loadInitialItemsAsync() {
		Task<List<T>> entryLoader = new Task<List<T>>() {
			@Override
			protected List<T> call() {
				return loadInitialEntries();
			}
		};

		entryLoader.setOnSucceeded(e -> {
			try {
				List<T> entries = entryLoader.get();
				if (entries.size() == 0) {
					promptLayer.setVisible(true);
					return;
				}

				for (T state : entries)
					addHistoryItem(state);
			} catch (InterruptedException | ExecutionException E) {
				Services.loggingService.logSevere("Task thread interrupted while populating HistoryTab.", E,
						LocalDateTime.now());
			}
		});
		entryLoader.setOnFailed(e -> Services.loggingService.logWarning("Failed to load history.",
				(Exception) entryLoader.getException(), LocalDateTime.now()));
		new Thread(entryLoader).start();
		
	}

	private void addSearchItem(T state) {
		appendToList(state, searchBox, false);
	}

	protected abstract List<T> loadInitialEntries();
	
	public void focusSearchField() {
        searchTextField.requestFocus();
	}

	private Searchable<T> appendToList(T state, VBox layer, boolean appendToStart) {
		promptLayer.setVisible(false);
		try {
			SearchEntry<T> searchEntry = createEntryFromState(state);

			if (appendToStart)
				layer.getChildren().add(0, searchEntry.getFxmlItem());
			else
				layer.getChildren().add(searchEntry.getFxmlItem());
			
			return searchEntry.getSearchable();
		} catch (IOException e) {
			Services.loggingService.logSevere("Could not append HistoryItem to list.", e, LocalDateTime.now());
		}
		
		return null;
	}
	
	
	protected abstract SearchEntry<T> createEntryFromState(T state) throws IOException;
	

	public void addHistoryItem(T state) {
		Searchable<T> controller = appendToList(state, searchTab, true);
		searchableItems.add(controller);
	}
	
	private List<Searchable<T>> getSearchResults(String searchString) {
		List<Searchable<T>> filteredList = new ArrayList<>();

		for (Searchable<T> controller : searchableItems) {

			int relativityIndex = controller.getRelativityIndex(searchString);

			// Split the string into words and get total relativity index as sum of
			// individual indices.
			String words[] = searchString.split("\\s");
			for (String word : words)
				relativityIndex += controller.getRelativityIndex(word);

			if (relativityIndex != 0)
				filteredList.add(controller);
		}

		return filteredList;
	}
}
