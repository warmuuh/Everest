/*
 * Copyright 2018 Rohit Awate.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rohitawate.everest.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.models.DashboardState;
import com.rohitawate.everest.models.RequestCollection;
import com.rohitawate.everest.models.requests.DataDispatchRequest;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

public class CollectionsItemController implements Searchable<RequestCollection> {
    @FXML
    private TitledPane collectionsItemBox;

    @FXML
    private VBox requestList;
    
    private RequestCollection requestCollection;


	private List<Consumer<DashboardState>> stateClickHandler = new LinkedList<>();

	
	
    @Override
	public RequestCollection getDashboardState() {
        return requestCollection;
    }

    public void setRequestCollection(RequestCollection requestCollection) {
        this.requestCollection = requestCollection;
        collectionsItemBox.setText(requestCollection.getName());
        
        
        DashboardState state = new DashboardState(new DataDispatchRequest("GET"));
        try {
			state.setTarget("http://google.com");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

        
        
        try {
			inserNewItem(state);
		} catch (IOException e) {
			Services.loggingService.logSevere("Failed to add request to collection", e, LocalDateTime.now());
		}
        
    }

    private void inserNewItem(DashboardState state) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/HistoryItem.fxml"));
		Parent historyItem = loader.load();

		HistoryItemController controller = loader.getController();

		controller.setRequestType(state.getHttpMethod());

		controller.setAddress(state.getTarget().toString());
		controller.setDashboardState(state);

		
		requestList.getChildren().add(historyItem);
		
		// Clicking on HistoryItem opens it up in a new tab
		historyItem.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton() == MouseButton.PRIMARY)
				handleClick(state);
		});
//		return new SearchEntry<DashboardState>(historyItem, controller);
	}

	@Override
	public int getRelativityIndex(String searchString) {
        searchString = searchString.toLowerCase();
        String comparisonString;

        // Checks if matches with name
        comparisonString = requestCollection.getName().toString().toLowerCase();
        if (comparisonString.contains(searchString))
            return 10;

        return 0;
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
