package application.gui;

import application.data.DataBank;
import application.gui.canvas.CanvasController;
import application.node.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.dialog.Dialogs;

import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
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
    private TabPane flowTabPane;

    @FXML
    private javafx.scene.canvas.Canvas canvasFlow;

    @FXML
    private TabPane tabPaneSource;

    private CanvasController canvasController;
    private ContextMenu canvasFlowContextMenu;
    private ContextMenu programListContextMenu;
    private Boolean skipCanvasClick = false;
    private static Controller controller;
    private Scene scene;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        Controller.controller = this;

        assert stackPane != null : "fx:id=\"stackPane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
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

        assert flowTabPane != null : "fx:id=\"flowTabPane\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        flowTabPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                canvasFlow.setWidth(newSceneWidth.intValue());
                canvasController.drawProgram();
            }
        });

        flowTabPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                canvasFlow.setHeight(newSceneHeight.intValue());
                canvasController.drawProgram();
            }
        });

        stackPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                splitPanePageCentral.setPrefWidth(newSceneWidth.intValue());
            }
        });

        stackPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                splitPanePageCentral.setPrefHeight(newSceneHeight.intValue());
            }
        });

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

        canvasFlow.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Program program = DataBank.currentlyEditProgram;
                if (program != null) {
                    List<DrawableNode> clickNodes = program.getFlowController().getClickedNodes(event.getX() - canvasController.getOffsetWidth(), event.getY() - canvasController.getOffsetHeight());
                    if (clickNodes.size() > 0) {
                        scene.setCursor(Cursor.HAND);
                    } else {
                        scene.setCursor(Cursor.DEFAULT);
                    }
                }
            }
        });

        canvasFlow.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (canvasFlowContextMenu != null) {
                    canvasFlowContextMenu.hide();
                }
                if (skipCanvasClick) {
                    skipCanvasClick = false;
                } else {
                    Program program = DataBank.currentlyEditProgram;
                    if (program != null) {
                        List<DrawableNode> clickNodes = program.getFlowController().getClickedNodes(event.getX() - canvasController.getOffsetWidth(), event.getY() - canvasController.getOffsetHeight());
                        if (clickNodes.size() > 0) {
                            DrawableNode drawableNode = clickNodes.get(0);

                            Tab nodeTab = null;
                            // We loop to see if the tab already exists for this node
                            for (Tab loopTab : tabPaneSource.getTabs()) {
                                if (loopTab.getId() != null) {
                                    if (loopTab.getId().equals(drawableNode.getId().toString())) {
                                        nodeTab = loopTab;
                                        break;
                                    }
                                }
                            }

                            // If the tab does not already exist we create it
                            if (nodeTab == null) {
                                nodeTab = drawableNode.createInterface();
                                tabPaneSource.getTabs().add(nodeTab);
                            }

                            // Finally we select the tab
                            selectTab(nodeTab);
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
                    if (canvasFlowContextMenu != null) {
                        canvasFlowContextMenu.hide();
                    }

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
                            SwitchNode newSwitchNode = new SwitchNode(0.0, 0.0, new BigInteger(40, random).toString(32));
                            program.getFlowController().addNode(newSwitchNode);
                            DataBank.saveNode(newSwitchNode); // We need to save the node after creating it to assign the ID correctly
                            canvasController.drawProgram();
                        }
                    });
                    menuItemFlowSplitNode.setId("SplitNode-");

                    MenuItem menuItemFlowConsoleNode = new MenuItem("Add Console Node");
                    menuItemFlowConsoleNode.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Program program = DataBank.currentlyEditProgram;

                            SecureRandom random = new SecureRandom();
                            ConsoleNode newConsoleNode = new ConsoleNode(0.0, 0.0, new BigInteger(40, random).toString(32));
                            program.getFlowController().addNode(newConsoleNode);
                            DataBank.saveNode(newConsoleNode); // We need to save the node after creating it to assign the ID correctly
                            canvasController.drawProgram();
                        }
                    });
                    menuItemFlowConsoleNode.setId("ConsoleNode-");

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
                                canvasController.drawProgram();
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

                    canvasFlowContextMenu = new ContextMenu();
                    canvasFlowContextMenu.getItems().add(menuItemFlowAddNode);
                    canvasFlowContextMenu.getItems().add(menuItemFlowAddResultSet);
                    canvasFlowContextMenu.getItems().add(menuItemFlowSplitNode);
                    canvasFlowContextMenu.getItems().add(menuItemFlowConsoleNode);
                    if (clickNodes.size() > 0) {
                        canvasFlowContextMenu.getItems().add(menuItemFlowRemoveNode);
                        canvasFlowContextMenu.getItems().add(menuItemFlowStartNode);
                    }

                    canvasFlowContextMenu.show(canvasFlow, event.getScreenX(), event.getScreenY());
                }
            }
        });

        programList.getItems().addAll(DataBank.getPrograms());
        programList.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            private String clickedName = "";

            @Override
            public void handle(ContextMenuEvent event) {
                if (programListContextMenu != null) {
                    programListContextMenu.hide();
                }

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

                programListContextMenu = new ContextMenu();

                if (clickedName == null) {
                    programListContextMenu.getItems().add(menuItemNewProgram);
                } else {
                    programListContextMenu.getItems().add(menuItemNewProgram);
                    programListContextMenu.getItems().add(menuItemDeleteProgram);
                    programListContextMenu.getItems().add(menuItemCompile);
                    programListContextMenu.getItems().add(menuItemRun);
                }

                programListContextMenu.show(programList, event.getScreenX(), event.getScreenY());

                clickedName = null;
            }
        });

        programList.setEditable(true);
        Callback<ListView<Program>, ListCell<Program>> onCommit = TextFieldListCell.forListView(new StringConverter<Program>() {
            @Override
            public String toString(Program program) {
                return program.toString();
            }

            @Override public Program fromString(String input) {
                Program program = DataBank.currentlyEditProgram;
                program.setName(input);
                return program;
            }
        });
        programList.setCellFactory(onCommit);

        programList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (programListContextMenu != null) {
                    programListContextMenu.hide();
                }
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

//        console.setOnKeyPressed(new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent ke) {
//                if (ke.getCode().equals(KeyCode.ENTER)) {
//                    String consoleText = console.getText();
//                    String commandText = consoleText.substring(consoleText.lastIndexOf("$") + 1, consoleText.length());
//                    System.out.println("Sending -> " + commandText);
//                    SSHManager sshManager = (SSHManager) DataBank.loadVariable("ssh", "27");
//                    sshManager.sendShellCommand(commandText);
//                }
//            }
//        });

        menuBarMenuItemQuit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ((Stage) scene.getWindow()).close();
            }
        });

        tabPaneSource.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    }


    public void selectTab(Tab tab) {
        SingleSelectionModel<Tab> selectionModel = tabPaneSource.getSelectionModel();
        selectionModel.select(tab);
    }

    public TextField createNodeNameField(DrawableNode drawableNode) {
        TextField nameField = new TextField();
        nameField.setLayoutX(57);
        nameField.setLayoutY(13);
        nameField.setId("fieldName-" + drawableNode.getId());
        nameField.setText(drawableNode.getContainedText());

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

    public void updateCanvasControllerNow() {
        canvasController.drawProgram();
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

    public void setCursor(Cursor cursor) {
        scene.setCursor(cursor);
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


