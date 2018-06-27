package com.rohitawate.everest.controllers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.models.DashboardState;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;

public class SearchPaneController extends AbstractSearchablePaneController {

	private List<Consumer<DashboardState>> stateClickHandler = new LinkedList<>();


	@Override
	protected List<DashboardState> loadInitialEntries() {
		return Services.historyManager.getHistory();
	}

	protected SearchEntry createEntryFromState(DashboardState state) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HistoryItem.fxml"));
		Parent historyItem = loader.load();

		HistoryItemController controller = loader.getController();

		controller.setRequestType(state.getHttpMethod());

		controller.setAddress(state.getTarget().toString());
		controller.setDashboardState(state);

		// Clicking on HistoryItem opens it up in a new tab
		historyItem.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton() == MouseButton.PRIMARY)
				handleClick(state);
		});
		return new SearchEntry(historyItem, controller);
	}

	private void handleClick(DashboardState state) {
		for (Consumer<DashboardState> consumer : stateClickHandler) {
			consumer.accept(state);
		}
	}

	public void addHandler(Consumer<DashboardState> handler) {
		stateClickHandler.add(handler);
	}

}
