package com.rohitawate.everest.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.jfoenix.controls.JFXButton;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.models.DashboardState;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SearchPaneController implements Initializable {

	@FXML
	private StackPane historyPromptLayer, searchLayer, searchFailedLayer;

	@FXML
	private JFXButton clearSearchFieldButton;

	@FXML
	private TextField historyTextField;

	@FXML
	private VBox historyTab, searchBox, historyPane;

	private List<HistoryItemController> historyItemControllers;

	
	private List<Consumer<DashboardState>> stateClickHandler = new LinkedList<>();
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		historyItemControllers = new ArrayList<>();
		searchLayer.visibleProperty().bind(historyTextField.textProperty().isNotEmpty());

		historyTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
			searchBox.getChildren().remove(0, searchBox.getChildren().size());
			searchFailedLayer.setVisible(false);
			List<HistoryItemController> searchResults = getSearchResults(historyTextField.getText());

			// Method of sorting the HistoryItemControllers
			searchResults.sort((controller1, controller2) -> {
				int relativity1 = controller1.getRelativityIndex(historyTextField.getText());
				int relativity2 = controller2.getRelativityIndex(historyTextField.getText());
				if (relativity1 < relativity2)
					return 1;
				else if (relativity1 > relativity2)
					return -1;
				else
					return 0;
			});

			if (searchResults.size() != 0) {
				for (HistoryItemController controller : searchResults) {
					addSearchItem(controller.getDashboardState());
				}
			} else {
				searchFailedLayer.setVisible(true);
			}
		}));

		clearSearchFieldButton.setOnAction(e -> historyTextField.clear());

		Platform.runLater(() -> {

			// Loads the history
			Task<List<DashboardState>> historyLoader = new Task<List<DashboardState>>() {
				@Override
				protected List<DashboardState> call() {
					return Services.historyManager.getHistory();
				}
			};

			// Appends the history items to the HistoryTab
			historyLoader.setOnSucceeded(e -> {
				try {
					List<DashboardState> history = historyLoader.get();
					if (history.size() == 0) {
						historyPromptLayer.setVisible(true);
						return;
					}

					for (DashboardState state : history)
						addHistoryItem(state);
				} catch (InterruptedException | ExecutionException E) {
					Services.loggingService.logSevere("Task thread interrupted while populating HistoryTab.", E,
							LocalDateTime.now());
				}
			});
			historyLoader.setOnFailed(e -> Services.loggingService.logWarning("Failed to load history.",
					(Exception) historyLoader.getException(), LocalDateTime.now()));
			new Thread(historyLoader).start();
		});
	}

	private void addSearchItem(DashboardState state) {
		appendToList(state, searchBox, false);
	}

	public void addHistoryItem(DashboardState state) {
		HistoryItemController controller = appendToList(state, historyTab, true);
		historyItemControllers.add(controller);
	}
	
	
	public void focusSearchField() {
        historyTextField.requestFocus();
	}

	private HistoryItemController appendToList(DashboardState state, VBox layer, boolean appendToStart) {
		historyPromptLayer.setVisible(false);
		HistoryItemController controller = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HistoryItem.fxml"));
			Parent historyItem = loader.load();

			controller = loader.getController();

			controller.setRequestType(state.getHttpMethod());

			controller.setAddress(state.getTarget().toString());
			controller.setDashboardState(state);

			if (appendToStart)
				layer.getChildren().add(0, historyItem);
			else
				layer.getChildren().add(historyItem);

			// Clicking on HistoryItem opens it up in a new tab
			historyItem.setOnMouseClicked(mouseEvent -> {
				if (mouseEvent.getButton() == MouseButton.PRIMARY)
					handleClick(state);
			});
		} catch (IOException e) {
			Services.loggingService.logSevere("Could not append HistoryItem to list.", e, LocalDateTime.now());
		}
		return controller;
	}

	private void handleClick(DashboardState state) {
		for (Consumer<DashboardState> consumer : stateClickHandler) {
			consumer.accept(state);
		}
	}

	public void addHandler(Consumer<DashboardState> handler) {
		stateClickHandler.add(handler);
	}
	
	private List<HistoryItemController> getSearchResults(String searchString) {
		List<HistoryItemController> filteredList = new ArrayList<>();

		for (HistoryItemController controller : historyItemControllers) {

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
