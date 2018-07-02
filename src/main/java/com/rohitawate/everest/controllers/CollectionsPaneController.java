package com.rohitawate.everest.controllers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.models.DashboardState;
import com.rohitawate.everest.models.RequestCollection;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class CollectionsPaneController extends AbstractSearchablePaneController<RequestCollection> {

	private List<Consumer<DashboardState>> stateClickHandler = new LinkedList<>();

	@Override
	protected List<RequestCollection> loadInitialEntries() {
		return Services.historyManager.getRequestCollections();
	}

	@Override
	protected SearchEntry<RequestCollection> createEntryFromState(RequestCollection state) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/CollectionsItem.fxml"));
		Parent historyItem = loader.load();

		CollectionsItemController controller = loader.getController();
		controller.setRequestCollection(state);

		controller.addHandler(this::handleClick);
		
		
		return new SearchEntry<RequestCollection>(historyItem, controller);

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
