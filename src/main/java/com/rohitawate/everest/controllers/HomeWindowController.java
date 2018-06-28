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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.rohitawate.everest.misc.KeyMap;
import com.rohitawate.everest.misc.Services;
import com.rohitawate.everest.misc.ThemeManager;
import com.rohitawate.everest.models.DashboardState;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HomeWindowController implements Initializable {
    @FXML
    private StackPane homeWindowSP;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TabPane homeWindowTabPane;
    
    @FXML
    private SearchPaneController searchPaneController;

    @FXML
    private CollectionsPaneController collectionsPaneController;
    
    private HashMap<Tab, DashboardController> tabControllerMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Using LinkedHashMap because they retain order
        tabControllerMap = new LinkedHashMap<>();
        recoverState();


        searchPaneController.addHandler(this::addTab);
        collectionsPaneController.addHandler(this::addTab);
        homeWindowSP.setFocusTraversable(true);

        Platform.runLater(() -> {
            homeWindowSP.requestFocus();
            this.setGlobalShortcuts();

            // Saves the state of the application before closing
            Stage thisStage = (Stage) homeWindowSP.getScene().getWindow();
            thisStage.setOnCloseRequest(e -> saveState());

         

        });
    }

    private void setGlobalShortcuts() {
        Scene thisScene = homeWindowSP.getScene();

        thisScene.setOnKeyPressed(e -> {
            if (KeyMap.newTab.match(e)) {
                addTab();
            } else if (KeyMap.focusAddressBar.match(e)) {
                Tab activeTab = getActiveTab();
                tabControllerMap.get(activeTab).addressField.requestFocus();
            } else if (KeyMap.focusMethodBox.match(e)) {
                Tab activeTab = getActiveTab();
                tabControllerMap.get(activeTab).httpMethodBox.show();
            } else if (KeyMap.sendRequest.match(e)) {
                Tab activeTab = getActiveTab();
                tabControllerMap.get(activeTab).sendRequest();
//            } else if (KeyMap.toggleHistory.match(e)) {
//                toggleHistoryPane();
            } else if (KeyMap.closeTab.match(e)) {
                Tab activeTab = getActiveTab();
                if (homeWindowTabPane.getTabs().size() == 1)
                    addTab();
                homeWindowTabPane.getTabs().remove(activeTab);
                tabControllerMap.remove(activeTab);
            } else if (KeyMap.searchHistory.match(e)) {
            	searchPaneController.focusSearchField();
            } else if (KeyMap.focusParams.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                controller.requestOptionsTab.getSelectionModel().select(controller.paramsTab);
            } else if (KeyMap.focusAuth.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                controller.requestOptionsTab.getSelectionModel().select(controller.authTab);
            } else if (KeyMap.focusHeaders.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                controller.requestOptionsTab.getSelectionModel().select(controller.headersTab);
            } else if (KeyMap.focusBody.match(e)) {
                Tab activeTab = getActiveTab();
                DashboardController controller = tabControllerMap.get(activeTab);
                String httpMethod = controller.httpMethodBox.getValue();
                if (!httpMethod.equals("GET") && !httpMethod.equals("DELETE")) {
                    controller.requestOptionsTab.getSelectionModel().select(controller.bodyTab);
                }
            } else if (KeyMap.refreshTheme.match(e)) {
                ThemeManager.refreshTheme();
            }
        });
    }

    private Tab getActiveTab() {
        return homeWindowTabPane.getSelectionModel().getSelectedItem();
    }

//    private void toggleHistoryPane() {
//        if (historyPane.isVisible()) {
//            historyPane = (VBox) splitPane.getItems().remove(0);
//        } else {
//            splitPane.getItems().add(0, historyPane);
//        }
//
//        historyPane.setVisible(!historyPane.isVisible());
//    }

    private void addTab() {
        addTab(null);
    }

    private void addTab(DashboardState dashboardState) {
        try {
            Tab newTab = new Tab();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/homewindow/Dashboard.fxml"));
            Parent dashboard = loader.load();
            DashboardController controller = loader.getController();

            if (dashboardState != null)
                controller.setState(dashboardState);

            newTab.setContent(dashboard);

            // Binds the addressField text to the Tab text
            StringProperty addressProperty = controller.addressField.textProperty();
            newTab.textProperty().bind(
                    Bindings.when(addressProperty.isNotEmpty())
                            .then(addressProperty)
                            .otherwise("New Tab"));

            // Tab closing procedure
            newTab.setOnCloseRequest(e -> {
                if (homeWindowTabPane.getTabs().size() == 1)
                    addTab();
                tabControllerMap.remove(newTab);
            });

            homeWindowTabPane.getTabs().add(newTab);
            homeWindowTabPane.getSelectionModel().select(newTab);
            tabControllerMap.put(newTab, controller);
        } catch (IOException e) {
            Services.loggingService.logSevere("Could not add a new tab.", e, LocalDateTime.now());
        }
    }

    private void saveState() {
        List<DashboardState> dashboardStates = new ArrayList<>();

        // Get the states of all the tabs
        for (DashboardController controller : tabControllerMap.values())
            dashboardStates.add(controller.getState());

        try {

            File configFolder = new File("Everest/config/");
            if (!configFolder.exists())
                configFolder.mkdirs();

            OutputStream fileStream = new FileOutputStream("Everest/config/everest.state");
            ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(dashboardStates);
            objectStream.close();
            fileStream.close();
            Services.loggingService.logInfo("Application state was saved successfully.", LocalDateTime.now());
        } catch (IOException e) {
            Services.loggingService.logSevere("Failed to save the application's state.", e, LocalDateTime.now());
        }
    }

    private void recoverState() {
        try {
            InputStream fileStream = new FileInputStream("Everest/config/everest.state");
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);

            Services.loggingService.logInfo("Application state file found.", LocalDateTime.now());

            List<DashboardState> dashboardStates = (List<DashboardState>) objectStream.readObject();
            objectStream.close();
            fileStream.close();

            if (dashboardStates.size() > 0) {
                for (DashboardState dashboardState : dashboardStates)
                    addTab(dashboardState);
            } else {
                addTab();
            }
        } catch (FileNotFoundException e) {
            Services.loggingService.logWarning("Application state file not found. Loading default state.", e, LocalDateTime.now());
            addTab();
        } catch (IOException | ClassNotFoundException e) {
            Services.loggingService.logWarning("Application state file is possibly corrupted. Could not recover the state.\nLoading default state.", e, LocalDateTime.now());
            addTab();
        } finally {
            Services.loggingService.logInfo("Application loaded.", LocalDateTime.now());
        }
    }

	public void addHistoryItem(DashboardState state) {
		searchPaneController.addHistoryItem(state);
	}

  
}
