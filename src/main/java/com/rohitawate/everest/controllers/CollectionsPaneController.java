package com.rohitawate.everest.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.rohitawate.everest.models.RequestCollection;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class CollectionsPaneController extends AbstractSearchablePaneController<RequestCollection> {

	@Override
	protected List<RequestCollection> loadInitialEntries() {
		return Arrays.asList(new RequestCollection("TestCollection"), new RequestCollection("AnotherTestCollection"));
	}

	@Override
	protected SearchEntry<RequestCollection> createEntryFromState(RequestCollection state) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/CollectionsItem.fxml"));
		Parent historyItem = loader.load();

		CollectionsItemController controller = loader.getController();
		controller.setRequestCollection(state);

		return new SearchEntry<RequestCollection>(historyItem, controller);

	}

}
