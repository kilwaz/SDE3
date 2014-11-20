package application.gui;

import application.data.DataBank;
import application.gui.canvas.CanvasController;
import application.node.DrawableNode;
import application.utils.AppParams;
import application.utils.ThreadManager;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.StatusBar;
import org.controlsfx.dialog.Dialogs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
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

    @FXML
    private StatusBar statusBar;

    @FXML
    private ToolBar toolBar;

    @FXML
    private Button runButtonToolBar;

    private CanvasController canvasController;
    private ContextMenu canvasFlowContextMenu;
    private ContextMenu programListContextMenu;
    private PopOver canvasPopOver;
    private Boolean skipCanvasClick = false;
    private static Controller controller;
    private Scene scene;

    private Double lastCanvasContextMenuX = 0d;
    private Double lastCanvasContextMenuY = 0d;

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

        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert toolBar != null : "fx:id=\"toolBar\" was not injected: check your FXML file 'ApplicationScene.fxml'.";
        assert runButtonToolBar != null : "fx:id=\"runButtonToolBar\" was not injected: check your FXML file 'ApplicationScene.fxml'.";

        flowTabPane.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            canvasFlow.setWidth(newSceneWidth.intValue());
            canvasController.drawProgram();
        });

        flowTabPane.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> {
            canvasFlow.setHeight(newSceneHeight.intValue());
            canvasController.drawProgram();
        });

        stackPane.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> splitPanePageCentral.setPrefWidth(newSceneWidth.intValue()));
        stackPane.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> splitPanePageCentral.setPrefHeight(newSceneHeight.intValue()));

        canvasPopOver = new PopOver();

        canvasController = new CanvasController(canvasFlow);

        canvasFlow.setOnMouseDragged(event -> canvasController.canvasDragged(event));
        canvasFlow.setOnMousePressed(event -> canvasController.canvasMouseDown(event));
        canvasFlow.setOnMouseReleased(event -> skipCanvasClick = canvasController.canvasMouseUp(event));

        canvasFlow.setOnScroll(event -> {
            canvasController.setScale(canvasController.getScale() + event.getDeltaY() / 400);
            canvasController.drawProgram();
        });

        canvasFlow.setOnMouseMoved(event -> {
            Program program = DataBank.currentlyEditProgram;
            if (program != null) {
                List<DrawableNode> clickNodes = program.getFlowController().getClickedNodes(event.getX() - canvasController.getOffsetWidth(), event.getY() - canvasController.getOffsetHeight());
                if (clickNodes.size() > 0) {
                    scene.setCursor(Cursor.HAND);
                } else {
                    scene.setCursor(Cursor.DEFAULT);
                }
            }
        });

        canvasFlow.setOnMouseClicked(event -> {
            if (canvasFlowContextMenu != null) {
                canvasFlowContextMenu.hide();
            }
            if (canvasPopOver != null) {
                canvasPopOver.hide();
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
        });

        canvasFlow.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                Program program = DataBank.currentlyEditProgram;
                if (program != null) {
                    if (canvasFlowContextMenu != null) {
                        canvasFlowContextMenu.hide();
                    }

                    lastCanvasContextMenuX = event.getX();
                    lastCanvasContextMenuY = event.getY();

                    List<DrawableNode> clickNodes = program.getFlowController().getClickedNodes(event.getX() - canvasController.getOffsetWidth(), event.getY() - canvasController.getOffsetHeight());

                    if (clickNodes.size() > 0) {
                        DrawableNode drawableNode = clickNodes.get(0);

                        Button copyButton = new Button("Copy");
                        copyButton.setOnAction(actionEvent -> {
                            DrawableNode copyNode = program.getFlowController().getNodeById(Integer.parseInt(((Button) actionEvent.getSource()).getId().replace("CopyNode-", "")));

                            try {
                                Class<?> clazz = Class.forName("application.node." + copyNode.getClass().getSimpleName());
                                Constructor<?> ctor = clazz.getConstructor(copyNode.getClass());
                                DrawableNode newNode = (DrawableNode) ctor.newInstance(copyNode);

                                program.getFlowController().addNode(newNode);
                                DataBank.saveNode(newNode); // We need to save the node after creating it to assign the ID correctly
                                canvasController.drawProgram();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            canvasPopOver.hide();
                        });
                        copyButton.setId("CopyNode-" + drawableNode.getId());

                        Button startNodeButton = new Button("Start Node");
                        startNodeButton.setOnAction(event1 -> {
                            DrawableNode startNode = program.getFlowController().getNodeById(Integer.parseInt(((Button) event1.getSource()).getId().replace("StartNode-", "")));
                            program.getFlowController().setStartNode(startNode);
                            canvasController.drawProgram();
                            DataBank.saveProgram(program);
                            canvasPopOver.hide();
                        });
                        startNodeButton.setId("StartNode-" + drawableNode.getId());

                        Button removeNodeButton = new Button("Remove Node");
                        removeNodeButton.setOnAction(actionEvent -> {
                            DrawableNode removedNode = program.getFlowController().getNodeById(Integer.parseInt(((Button) actionEvent.getSource()).getId().replace("RemoveNode-", "")));

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
                            canvasPopOver.hide();
                        });
                        removeNodeButton.setId("RemoveNode-" + drawableNode.getId());


                        VBox node = new VBox(5);
                        HBox header = new HBox(5);
                        header.setAlignment(Pos.CENTER);

                        // Text showing title of node
                        Text nodeText = new Text(drawableNode.getContainedText());
                        nodeText.setFill(drawableNode.getFillColour());
                        nodeText.setFont(AppParams.getFont(14));

                        // Color picker for node preset to current color
                        ColorPicker colorPicker = new ColorPicker();
                        colorPicker.setValue(drawableNode.getFillColour());
                        colorPicker.setStyle("-fx-background-color: null; -fx-color-label-visible: false;");

                        colorPicker.setOnAction(new EventHandler() {
                            public void handle(Event t) {
                                drawableNode.setFillColour(colorPicker.getValue());
                                canvasController.drawProgram();
                            }
                        });

                        header.getChildren().add(nodeText);
                        header.getChildren().add(colorPicker);

                        node.setPadding(new Insets(10, 6, 10, 6));

                        node.getChildren().add(header);
                        node.getChildren().add(copyButton);
                        node.getChildren().add(startNodeButton);
                        node.getChildren().add(removeNodeButton);

                        canvasPopOver.show(canvasFlow, event.getScreenX(), event.getScreenY());
                        canvasPopOver.setContentNode(node);
                    } else {
                        canvasPopOver.hide();

                        List<MenuItem> nodeMenuItems = new ArrayList<>();
                        for (String nodeName : DrawableNode.NODE_NAMES) {
                            nodeMenuItems.add(createNodeMenuItem(nodeName));
                        }

                        canvasFlowContextMenu = new ContextMenu();
                        canvasFlowContextMenu.getItems().addAll(nodeMenuItems);
                        canvasFlowContextMenu.show(canvasFlow, event.getScreenX(), event.getScreenY());
                    }
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
                menuItemNewProgram.setOnAction(event1 -> {
                    Program program = DataBank.createNewProgram("New program");

                    programList.getItems().add(program);
                });

                MenuItem menuItemDeleteProgram = new MenuItem("Delete Program");
                menuItemDeleteProgram.setOnAction(event1 -> {
                    Program program = DataBank.currentlyEditProgram;

                    org.controlsfx.control.action.Action action = Dialogs.create()
                            .owner(null)
                            .title("Deleting program")
                            .message("Are you sure you want to delete " + program.getName()).showConfirm();
                    if ("YES".equals(action.toString())) {
                        DataBank.deleteProgram(program);
                        programList.getItems().remove(program);
                    }
                });

                MenuItem menuItemCompile = new MenuItem("Compile...");
                menuItemCompile.setOnAction(event1 -> {
                    Program program = DataBank.currentlyEditProgram;
                    program.compile();
                });

                MenuItem menuItemRun = new MenuItem("Run...");
                menuItemRun.setOnAction(event1 -> {
                    Program program = DataBank.currentlyEditProgram;
                    program.run();
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

            @Override
            public Program fromString(String input) {
                Program program = DataBank.currentlyEditProgram;
                program.setName(input);
                return program;
            }
        });
        programList.setCellFactory(onCommit);

        programList.setOnMouseClicked(event -> {
            if (programListContextMenu != null) {
                programListContextMenu.hide();
            }
        });

        programList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldProgram, newProgram) -> {
                    DataBank.currentlyEditProgram = newProgram;
                    newProgram.getFlowController().checkConnections();
                    canvasController.drawProgram();
                });


        runButtonToolBar.setOnAction(event -> {
                    Program program = DataBank.currentlyEditProgram;
                    program.run();
                }
        );

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

        menuBarMenuItemQuit.setOnAction(event -> ((Stage) scene.getWindow()).close());

        tabPaneSource.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        updateThreadCount(ThreadManager.getInstance().getActiveThreads());
    }

    public MenuItem createNodeMenuItem(String className) {
        MenuItem menuItem = new MenuItem("Add " + className);
        menuItem.setOnAction(event -> {
            Program program = DataBank.currentlyEditProgram;

            try {
                Class<?> clazz = Class.forName("application.node." + className);
                Constructor<?> ctor = clazz.getConstructor(Double.class, Double.class, String.class);
                DrawableNode newNode = (DrawableNode) ctor.newInstance(lastCanvasContextMenuX - canvasController.getOffsetWidth(), lastCanvasContextMenuY - canvasController.getOffsetHeight(), "New " + className);

                program.getFlowController().addNode(newNode);
                DataBank.saveNode(newNode); // We need to save the node after creating it to assign the ID correctly
                canvasController.drawProgram();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        menuItem.setId(className + "-");

        return menuItem;
    }

    public void selectTab(Tab tab) {
        SingleSelectionModel<Tab> selectionModel = tabPaneSource.getSelectionModel();
        selectionModel.select(tab);
    }

    public TextField createNodeNameField(DrawableNode drawableNode) {
        TextField nameField = new TextField();
        nameField.setId("fieldName-" + drawableNode.getId());
        nameField.setText(drawableNode.getContainedText());

        nameField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                Program program = DataBank.currentlyEditProgram;
                DrawableNode nodeToUpdate = program.getFlowController().getNodeById(Integer.parseInt(textField.getId().replace("fieldName-", "")));
                nodeToUpdate.setContainedText(textField.getText());
                program.getFlowController().checkConnections(); // Renaming a node might make or break connections

                tabPaneSource.getTabs().stream().filter(loopTab -> loopTab.getId() != null).forEach(loopTab -> {
                    if (loopTab.getId().equals(nodeToUpdate.getId().toString())) {
                        loopTab.setText(textField.getText());
                    }
                });

                DataBank.saveNode(nodeToUpdate);
                canvasController.drawProgram();
            }
        });

        return nameField;
    }

    public TextField createNextNodeField(DrawableNode drawableNode) {
        TextField nameField = new TextField();
        nameField.setId("fieldNextNode-" + drawableNode.getId());
        nameField.setText(drawableNode.getNextNodeToRun());

        nameField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                Program program = DataBank.currentlyEditProgram;
                DrawableNode nodeToUpdate = program.getFlowController().getNodeById(Integer.parseInt(textField.getId().replace("fieldNextNode-", "")));
                nodeToUpdate.setNextNodeToRun(textField.getText());
                program.getFlowController().checkConnections(); // Renaming a node might make or break connections

                DataBank.saveNode(nodeToUpdate);
                canvasController.drawProgram();
            }
        });

        return nameField;
    }

    public Label createNodeNameLabel() {
        Label nameFieldLabel = new Label();
        nameFieldLabel.setText("Name:");
        return nameFieldLabel;
    }

    public Label createNextNodeLabel() {
        Label nameFieldLabel = new Label();
        nameFieldLabel.setText("Next:");
        return nameFieldLabel;
    }

    public Label createNodeInfoLabel(DrawableNode node) {
        Label nameFieldLabel = new Label();
        nameFieldLabel.setText(node.getNodeType() + " (#" + node.getId() + ")");
        nameFieldLabel.setTextFill(Color.GRAY);
        return nameFieldLabel;
    }

    public Tab createDefaultNodeTab(DrawableNode node) {
        Tab tab = new Tab();
        tab.setText(node.getContainedText());
        tab.setId(node.getId().toString());

        AnchorPane tabAnchorPane = new AnchorPane();
        HBox hbox = new HBox(5);
        hbox.setLayoutX(11);
        hbox.setLayoutY(13);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(createNodeNameLabel());
        hbox.getChildren().add(createNodeNameField(node));
        hbox.getChildren().add(createNodeInfoLabel(node));
        hbox.getChildren().add(createNextNodeLabel());
        hbox.getChildren().add(createNextNodeField(node));

        tabAnchorPane.getChildren().add(hbox);

        tabAnchorPane.setMaxHeight(Integer.MAX_VALUE);
        tabAnchorPane.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setBottomAnchor(tabAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(tabAnchorPane, 0.0);
        AnchorPane.setRightAnchor(tabAnchorPane, 0.0);
        AnchorPane.setTopAnchor(tabAnchorPane, 0.0);

        tab.setContent(tabAnchorPane);

        return tab;
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

    public void updateThreadCount(Integer threadCount) {
        class OneShotTask implements Runnable {
            Integer threadCount;

            OneShotTask(Integer threadCount) {
                this.threadCount = threadCount;
            }

            public void run() {
                statusBar.setText("Active threads: " + threadCount);
            }
        }

        Platform.runLater(new OneShotTask(threadCount));
    }
}


