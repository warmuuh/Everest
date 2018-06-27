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

import com.rohitawate.everest.models.RequestCollection;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CollectionsItemController implements Searchable<RequestCollection> {
    @FXML
    private Label name;
    
    private RequestCollection requestCollection;

    public void setName(String name) {
		this.name.setText(name);
	}

    @Override
	public RequestCollection getDashboardState() {
        return requestCollection;
    }

    public void setRequestCollection(RequestCollection requestCollection) {
        this.requestCollection = requestCollection;
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

}
