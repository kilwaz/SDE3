package application;

import application.net.SSHManager;
import application.tester.TestResult;
import application.tester.TestResultNode;
import application.utils.DataBank;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TextArea console;

    @FXML
    private StackPane stackPane;

    @FXML
    private ListView<Program> programList;

    @FXML
    private AnchorPane programAccordion;

    @FXML
    private AnchorPane leftAccordionAnchorPane;

    @FXML
    private AnchorPane lowerMainSplitPane;

    @FXML
    private AnchorPane sourceTabAnchorPane;

    @FXML
    private AnchorPane rightContextAnchorPane;

    @FXML
    private AnchorPane canvasAnchorPane;

    @FXML
    private TitledPane programTitlePane;

    @FXML
    private Accordion leftAccordion;

    @FXML
    private MenuItem menuContextNewProgram;

    @FXML
    private MenuItem menuBarMenuItemQuit;

    @FXML
    private SplitPane splitPanePageCentral;

    @FXML
    private javafx.scene.canvas.Canvas canvasFlow;

    @FXML
    private TabPane tabPaneSource;

    private CanvasController canvasController;
    private Boolean skipCanvasClick = false;
    private static Controller controller;
    private Scene scene;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        Controller.controller = this;

        assert console != null : "fx:id=\"console\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert programList != null : "fx:id=\"programList\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        assert programAccordion != null : "fx:id=\"programAccordion\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert leftAccordionAnchorPane != null : "fx:id=\"leftAccordionAnchorPane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert lowerMainSplitPane != null : "fx:id=\"lowerMainSplitPane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert sourceTabAnchorPane != null : "fx:id=\"lowerMainSplitPane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert rightContextAnchorPane != null : "fx:id=\"lowerMainSplitPane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert canvasAnchorPane != null : "fx:id=\"lowerMainSplitPane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        assert programTitlePane != null : "fx:id=\"programTitlePane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert leftAccordion != null : "fx:id=\"leftAccordion\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        assert menuContextNewProgram != null : "fx:id=\"menuContextNewProgram\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        assert splitPanePageCentral != null : "fx:id=\"splitPanePageCentral\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        assert canvasFlow != null : "fx:id=\"canvasFlow\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        assert tabPaneSource != null : "fx:id=\"tabPaneSource\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        assert menuBarMenuItemQuit != null : "fx:id=\"menuBarMenuItemQuit\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        canvasController = new CanvasController(canvasFlow);

        canvasFlow.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                canvasController.canvasDragged(event);
            }
        });

        canvasFlow.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                canvasController.canvasMouseDown(event);
            }
        });

        canvasFlow.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                skipCanvasClick = canvasController.canvasMouseUp(event);
            }
        });

        canvasFlow.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                canvasController.setScale(canvasController.getScale() + event.getDeltaY() / 400);
                canvasController.drawProgram();
            }
        });

        canvasFlow.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (skipCanvasClick) {
                    skipCanvasClick = false;
                } else {
                    Program program = DataBank.currentlyEditProgram;
                    if (program != null) {
                        List<DrawableNode> clickNodes = program.getFlowController().getClickedNodes(event.getX(), event.getY());
                        if (clickNodes.size() > 0) {
                            DrawableNode drawableNode = clickNodes.get(0);
                            if (drawableNode instanceof SourceNode) {
                                createOrShowSourceTab((SourceNode) drawableNode);
                            } else if (drawableNode instanceof TestResultNode) {
                                createOrShowResultSetTab((TestResultNode) drawableNode);
                            } else if (drawableNode instanceof SplitNode) {
                                createOrShowSplitTab((SplitNode) drawableNode);
                            }
                        }
                    }
                }
            }
        });

        canvasFlow.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                Program program = DataBank.currentlyEditProgram;
                if (program != null) {
                    List<DrawableNode> clickNodes = program.getFlowController().getClickedNodes(event.getX(), event.getY());

                    MenuItem menuItemFlowAddNode = new MenuItem("Add Node");
                    menuItemFlowAddNode.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Program program = DataBank.currentlyEditProgram;

                            SecureRandom random = new SecureRandom();
                            SourceNode newSourceNode = new SourceNode(0.0, 0.0, new BigInteger(40, random).toString(32));
                            program.getFlowController().addNode(newSourceNode);
                            DataBank.saveNode(newSourceNode); // We need to save the node after creating it to assign the ID correctly
                            canvasController.drawProgram();
                        }
                    });
                    menuItemFlowAddNode.setId("AddNode-");

                    MenuItem menuItemFlowAddResultSet = new MenuItem("Add Result Node");
                    menuItemFlowAddResultSet.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Program program = DataBank.currentlyEditProgram;

                            SecureRandom random = new SecureRandom();
                            TestResultNode newResultSet = new TestResultNode(0.0, 0.0, new BigInteger(40, random).toString(32));
                            program.getFlowController().addNode(newResultSet);
                            DataBank.saveNode(newResultSet); // We need to save the node after creating it to assign the ID correctly
                            canvasController.drawProgram();
                        }
                    });
                    menuItemFlowAddResultSet.setId("ResultNode-");

                    MenuItem menuItemFlowSplitNode = new MenuItem("Add Split Node");
                    menuItemFlowSplitNode.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Program program = DataBank.currentlyEditProgram;

                            SecureRandom random = new SecureRandom();
                            SplitNode newSplitNode = new SplitNode(0.0, 0.0, new BigInteger(40, random).toString(32));
                            program.getFlowController().addNode(newSplitNode);
                            DataBank.saveNode(newSplitNode); // We need to save the node after creating it to assign the ID correctly
                            canvasController.drawProgram();
                        }
                    });
                    menuItemFlowSplitNode.setId("SplitNode-");

                    MenuItem menuItemFlowStartNode = new MenuItem("Set Start Node");
                    MenuItem menuItemFlowRemoveNode = new MenuItem("Remove Node");
                    if (clickNodes.size() > 0) {
                        DrawableNode drawableNode = clickNodes.get(0);
                        menuItemFlowStartNode.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Program program = DataBank.currentlyEditProgram;
                                DrawableNode startNode = program.getFlowController().getNodeById(Integer.parseInt(((MenuItem) event.getSource()).getId().replace("StartNode-", "")));
                                program.getFlowController().setStartNode(startNode);
                                DataBank.saveProgram(program);
                            }
                        });
                        menuItemFlowStartNode.setId("StartNode-" + drawableNode.getId());

                        menuItemFlowRemoveNode.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Program program = DataBank.currentlyEditProgram;
                                DrawableNode removedNode = program.getFlowController().getNodeById(Integer.parseInt(((MenuItem) event.getSource()).getId().replace("RemoveNode-", "")));

                                program.getFlowController().removeNode(removedNode);
                                DataBank.deleteNode(removedNode);

                                canvasController.drawProgram();

                                Tab tabToRemove = null;
                                for (Tab loopTab : tabPaneSource.getTabs()) {
                                    if (loopTab.getId() != null) {
                                        if (loopTab.getId().equals(removedNode.getId().toString())) {
                                            tabToRemove = loopTab;
                                        }
                                    }
                                }

                                if (tabToRemove != null) {
                                    EventHandler<Event> handler = tabToRemove.getOnClosed();
                                    if (null != handler) {
                                        handler.handle(null);
                                    } else {
                                        tabToRemove.getTabPane().getTabs().remove(tabToRemove);
                                    }
                                }
                            }
                        });
                        menuItemFlowRemoveNode.setId("RemoveNode-" + drawableNode.getId());
                    }

                    ContextMenu contextMenu = new ContextMenu();
                    contextMenu.getItems().add(menuItemFlowAddNode);
                    contextMenu.getItems().add(menuItemFlowAddResultSet);
                    contextMenu.getItems().add(menuItemFlowSplitNode);
                    if (clickNodes.size() > 0) {
                        contextMenu.getItems().add(menuItemFlowRemoveNode);
                        contextMenu.getItems().add(menuItemFlowStartNode);
                    }

                    contextMenu.show(canvasFlow, event.getScreenX(), event.getScreenY());
                }
            }
        });

        programList.getItems().addAll(DataBank.getPrograms());
        programList.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            private String clickedName = "";

            @Override
            public void handle(ContextMenuEvent event) {
                clickedName = programList.getSelectionModel().getSelectedItem().getName();

                MenuItem menuItemNewProgram = new MenuItem("New Program");
                menuItemNewProgram.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Program program = DataBank.createNewProgram("New program");

                        programList.getItems().add(program);
                    }
                });

                MenuItem menuItemDeleteProgram = new MenuItem("Delete Program");
                menuItemDeleteProgram.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Program program = DataBank.currentlyEditProgram;

                        org.controlsfx.control.action.Action action = Dialogs.create()
                                .owner(null)
                                .title("Deleting program")
                                .message("Are you sure you want to delete " + program.getName()).showConfirm();
                        if ("YES".equals(action.toString())) {
                            DataBank.deleteProgram(program);
                            programList.getItems().remove(program);
                        }
                    }
                });

                MenuItem menuItemCompile = new MenuItem("Compile...");
                menuItemCompile.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Program program = DataBank.currentlyEditProgram;
                        program.compile();
                    }
                });

                MenuItem menuItemRun = new MenuItem("Run...");
                menuItemRun.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Program program = DataBank.currentlyEditProgram;
                        program.run();
                    }
                });

                ContextMenu contextMenu = new ContextMenu();

                if (clickedName == null) {
                    contextMenu.getItems().add(menuItemNewProgram);
                } else {
                    contextMenu.getItems().add(menuItemNewProgram);
                    contextMenu.getItems().add(menuItemDeleteProgram);
                    contextMenu.getItems().add(menuItemCompile);
                    contextMenu.getItems().add(menuItemRun);
                }

                contextMenu.show(programList, event.getScreenX(), event.getScreenY());

                clickedName = null;
            }
        });

        programList.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Program>() {

                    public void changed(ObservableValue<? extends Program> ov, Program oldProgram, Program newProgram) {
                        DataBank.currentlyEditProgram = newProgram;
                        newProgram.getFlowController().checkConnections();
                        canvasController.drawProgram();
                    }
                });

        console.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    String consoleText = console.getText();
                    String commandText = consoleText.substring(consoleText.lastIndexOf("$") + 1, consoleText.length());
                    System.out.println("Sending -> " + commandText);
                    SSHManager sshManager = (SSHManager) DataBank.loadVariable("ssh", "27");
                    sshManager.sendShellCommand(commandText);
                }
            }
        });

        menuBarMenuItemQuit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ((Stage) scene.getWindow()).close();
            }
        });

        tabPaneSource.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    }

    public void createOrShowSplitTab(SplitNode splitNode) {
        // Test to see if the tab exists and if so show it
        for (Tab loopTab : tabPaneSource.getTabs()) {
            if (loopTab.getId() != null) {
                if (loopTab.getId().equals(splitNode.getId().toString())) {
                    SingleSelectionModel<Tab> selectionModel = tabPaneSource.getSelectionModel();
                    selectionModel.select(loopTab);

                    TextField textField = (TextField) stackPane.lookup("#fieldName-" + splitNode.getId());
                    textField.setText(splitNode.getContainedText());

                    return;
                }
            }
        }
        // As the tab doesn't exist we create it here
        Tab tab = new Tab();
        AnchorPane tabAnchorPane = new AnchorPane();

        tabAnchorPane.setMaxHeight(Integer.MAX_VALUE);
        tabAnchorPane.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(tabAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(tabAnchorPane, 0.0);
        AnchorPane.setRightAnchor(tabAnchorPane, 0.0);
        AnchorPane.setTopAnchor(tabAnchorPane, 0.0);

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        List<Split> splits = splitNode.getSplits();
        for (int i = 0; i < splits.size(); i++) {
            rows.getChildren().add(createSplitNodeRow(i, splits.get(i), splitNode.getId()));
        }

        tabAnchorPane.getChildren().add(createNodeNameField(splitNode));
        tabAnchorPane.getChildren().add(createNodeNameLabel());
        tabAnchorPane.getChildren().add(rows);
        tab.setText(splitNode.getContainedText());
        tab.setId(splitNode.getId().toString());
        tab.setContent(tabAnchorPane);

        tabPaneSource.getTabs().add(tab);

        // Go back to the beginning and run the code to show the tab, it should now exist
        createOrShowSplitTab(splitNode);
    }

    public HBox createSplitNodeRow(Integer index, Split split, Integer splitId) {
        HBox row = new HBox(5);
        Button firstSplitButton = new Button();
        if (split.isEnabled()) {
            firstSplitButton.setText("Enabled");
        } else {
            firstSplitButton.setText("Disabled");
        }
        firstSplitButton.setPrefWidth(80);
        firstSplitButton.setId("splitButton-" + split.getId() + "-" + splitId);
        firstSplitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Button button = (Button) event.getSource();
                Program program = DataBank.currentlyEditProgram;
                String[] fieldId = button.getId().split("-");
                SplitNode splitNode = (SplitNode) program.getFlowController().getNodeById(Integer.parseInt(fieldId[2]));

                if ("Disabled".equals(button.getText())) {
                    button.setText("Enabled");
                    splitNode.updateSplitEnabled(Integer.parseInt(fieldId[1]), true);
                } else {
                    button.setText("Disabled");
                    splitNode.updateSplitEnabled(Integer.parseInt(fieldId[1]), false);
                }

                program.getFlowController().checkConnections(); // Toggling a switch will make or break connections
            }
        });
        row.getChildren().add(firstSplitButton);

        TextField firstSplitField = new TextField();
        firstSplitField.setText(split.getTarget());
        firstSplitField.setId("splitField-" + split.getId() + "-" + splitId);
        firstSplitField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TextField textField = (TextField) event.getSource();
                if (!textField.getText().isEmpty()) {
                    Program program = DataBank.currentlyEditProgram;
                    String[] fieldId = textField.getId().split("-");
                    SplitNode splitNode = (SplitNode) program.getFlowController().getNodeById(Integer.parseInt(fieldId[2]));
                    splitNode.updateSplitTarget(Integer.parseInt(fieldId[1]), textField.getText());

                    program.getFlowController().checkConnections(); // Renaming a node might make or break connections

                    DataBank.saveNode(splitNode);
                    canvasController.drawProgram();
                }
            }
        });

        row.getChildren().add(firstSplitField);

        return row;
    }

    public void createOrShowResultSetTab(TestResultNode testResultNode) {
        // Test to see if the tab exists and if so show it
        for (Tab loopTab : tabPaneSource.getTabs()) {
            if (loopTab.getId() != null) {
                if (loopTab.getId().equals(testResultNode.getId().toString())) {
                    SingleSelectionModel<Tab> selectionModel = tabPaneSource.getSelectionModel();
                    selectionModel.select(loopTab);

                    TextField textField = (TextField) stackPane.lookup("#fieldName-" + testResultNode.getId());
                    textField.setText(testResultNode.getContainedText());

                    return;
                }
            }
        }
        // As the tab doesn't exist we create it here
        Tab tab = new Tab();

        AnchorPane tabAnchorPane = new AnchorPane();
        TableView<TestResult> resultsTable = new TableView<TestResult>();
        resultsTable.setId("resultsTable-" + testResultNode.getId());

        TableColumn expectedOutput = new TableColumn("Expected Output");
        expectedOutput.setMinWidth(120);
        expectedOutput.setCellValueFactory(new PropertyValueFactory<TestResult, String>("outcome"));

        TableColumn actualOutput = new TableColumn("Actual Output");
        actualOutput.setMinWidth(120);
        actualOutput.setCellValueFactory(new PropertyValueFactory<TestResult, String>("expected"));

        TableColumn duration = new TableColumn("Duration");
        duration.setMinWidth(120);
        duration.setCellValueFactory(new PropertyValueFactory<TestResult, String>("duration"));

        resultsTable.setItems(testResultNode.getResultList());
        resultsTable.getColumns().addAll(expectedOutput);
        resultsTable.getColumns().addAll(actualOutput);
        resultsTable.getColumns().addAll(duration);
        resultsTable.setLayoutX(11);
        resultsTable.setLayoutY(50);

        resultsTable.setMaxHeight(Integer.MAX_VALUE);
        resultsTable.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(resultsTable, 0.0);
        AnchorPane.setLeftAnchor(resultsTable, 11.0);
        AnchorPane.setRightAnchor(resultsTable, 0.0);
        AnchorPane.setTopAnchor(resultsTable, 50.0);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tabAnchorPane.setMaxHeight(Integer.MAX_VALUE);
        tabAnchorPane.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(tabAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(tabAnchorPane, 0.0);
        AnchorPane.setRightAnchor(tabAnchorPane, 0.0);
        AnchorPane.setTopAnchor(tabAnchorPane, 0.0);

        tabAnchorPane.getChildren().add(createNodeNameField(testResultNode));
        tabAnchorPane.getChildren().add(createNodeNameLabel());
        tabAnchorPane.getChildren().add(resultsTable);
        tab.setText(testResultNode.getContainedText());
        tab.setId(testResultNode.getId().toString());
        tab.setContent(tabAnchorPane);

        tabPaneSource.getTabs().add(tab);

        // Go back to the beginning and run the code to show the tab, it should now exist
        createOrShowResultSetTab(testResultNode);
    }

    public TextField createNodeNameField(DrawableNode drawableNode) {
        TextField nameField = new TextField();
        nameField.setLayoutX(57);
        nameField.setLayoutY(13);
        nameField.setId("fieldName-" + drawableNode.getId());

        nameField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TextField textField = (TextField) event.getSource();
                if (!textField.getText().isEmpty()) {
                    Program program = DataBank.currentlyEditProgram;
                    DrawableNode nodeToUpdate = program.getFlowController().getNodeById(Integer.parseInt(textField.getId().replace("fieldName-", "")));
                    nodeToUpdate.setContainedText(textField.getText());
                    program.getFlowController().checkConnections(); // Renaming a node might make or break connections

                    for (Tab loopTab : tabPaneSource.getTabs()) {
                        if (loopTab.getId() != null) {
                            if (loopTab.getId().equals(nodeToUpdate.getId().toString())) {
                                loopTab.setText(textField.getText());
                            }
                        }
                    }

                    DataBank.saveNode(nodeToUpdate);

                    canvasController.drawProgram();
                }
            }
        });

        return nameField;
    }

    public Label createNodeNameLabel() {
        Label nameFieldLabel = new Label();
        nameFieldLabel.setText("Name:");
        nameFieldLabel.setLayoutX(11);
        nameFieldLabel.setLayoutY(17);
        return nameFieldLabel;
    }

    public void createOrShowSourceTab(SourceNode sourceNode) {
        // Test to see if the tab exists and if so show it
        for (Tab loopTab : tabPaneSource.getTabs()) {
            if (loopTab.getId() != null) {
                if (loopTab.getId().equals(sourceNode.getId().toString())) {
                    SingleSelectionModel<Tab> selectionModel = tabPaneSource.getSelectionModel();
                    selectionModel.select(loopTab);

                    TextField textField = (TextField) stackPane.lookup("#fieldName-" + sourceNode.getId());
                    textField.setText(sourceNode.getContainedText());
                    return;
                }
            }
        }

        // As the tab doesn't exist we create it here
        Tab tab = new Tab();
        tab.setText(sourceNode.getContainedText());
        tab.setId(sourceNode.getId().toString());

        AnchorPane tabAnchorPane = new AnchorPane();
        tabAnchorPane.getChildren().add(createNodeNameField(sourceNode));
        tabAnchorPane.getChildren().add(createNodeNameLabel());

        SourceTextArea sourceTextArea = new SourceTextArea(sourceNode);

        AnchorPane.setBottomAnchor(sourceTextArea, 0.0);
        AnchorPane.setLeftAnchor(sourceTextArea, 11.0);
        AnchorPane.setRightAnchor(sourceTextArea, 0.0);
        AnchorPane.setTopAnchor(sourceTextArea, 50.0);

        tabAnchorPane.setMaxHeight(Integer.MAX_VALUE);
        tabAnchorPane.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(tabAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(tabAnchorPane, 0.0);
        AnchorPane.setRightAnchor(tabAnchorPane, 0.0);
        AnchorPane.setTopAnchor(tabAnchorPane, 0.0);

        tabAnchorPane.getChildren().add(sourceTextArea);
        tab.setContent(tabAnchorPane);

        tabPaneSource.getTabs().add(tab);

        // Go back to the beginning and run the code to show the tab, it should now exist
        createOrShowSourceTab(sourceNode);
    }

    public void writeNewLineToConsole(String text) {
        console.appendText(System.getProperty("line.separator"));
        console.appendText(text);
    }

    public void writeToConsole(String text) {
        class OneShotTask implements Runnable {
            String str;

            OneShotTask(String s) {
                str = s;
            }

            public void run() {
                console.appendText(str);
            }
        }

        Platform.runLater(new OneShotTask(text));
    }

    // Use this one when not on GUI thread
    public void updateCanvasControllerLater() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                canvasController.drawProgram();
            }
        });
    }

    public void showError(Dialogs dialogs) {
        class OneShotTask implements Runnable {
            Dialogs dialogs;

            OneShotTask(Dialogs dialogs) {
                this.dialogs = dialogs;
            }

            public void run() {
                dialogs.showError();
            }
        }

        Platform.runLater(new OneShotTask(dialogs));
    }

    public void showException(Dialogs dialogs, Exception ex) {
        class OneShotTask implements Runnable {
            Exception ex;
            Dialogs dialogs;

            OneShotTask(Dialogs dialogs, Exception ex) {
                this.dialogs = dialogs;
                this.ex = ex;
            }

            public void run() {
                dialogs.showException(ex);
            }
        }

        Platform.runLater(new OneShotTask(dialogs, ex));
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Object getElementById(String id) {
        return scene.lookup("#" + id);
    }

    public static Controller getInstance() {
        return Controller.controller;
    }
}


