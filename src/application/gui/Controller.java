package application.gui;

import application.Main;
import application.data.DataBank;
import application.data.DatabaseConnectionWatcher;
import application.data.NodeColour;
import application.data.User;
import application.data.model.dao.ProgramDAO;
import application.data.model.dao.RecordedHeaderDAO;
import application.data.model.dao.RecordedProxyDAO;
import application.data.model.dao.RecordedRequestDAO;
import application.error.Error;
import application.gui.canvas.CanvasController;
import application.gui.dialog.ConfirmDialog;
import application.gui.window.*;
import application.node.design.DrawableNode;
import application.node.implementations.BatchNode;
import application.utils.AppParams;
import application.utils.managers.SessionManager;
import application.utils.managers.ThreadManager;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.StatusBar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    private static Controller controller;
    private static Logger log = Logger.getLogger(Controller.class);
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
    private AnchorPane rightContextAnchorPane;
    @FXML
    private TitledPane programTitlePane;
    @FXML
    private Accordion leftAccordion;
    @FXML
    private MenuItem menuBarMenuItemQuit;
    @FXML
    private MenuItem menuBarMenuItemLog;
    @FXML
    private MenuItem menuBarMenuItemThread;
    @FXML
    private MenuItem menuBarMenuItemExportProgram;
    @FXML
    private MenuItem menuBarMenuItemExportNode;
    @FXML
    private MenuItem menuBarMenuItemImport;
    @FXML
    private MenuItem menuBarMenuItemError;
    @FXML
    private MenuItem menuBarMenuItemClearRequestData;
    @FXML
    private MenuItem menuBarMenuItemExportNodeColours;
    @FXML
    private SplitPane splitPanePageCentral;
    @FXML
    private TabPane flowTabPane;
    @FXML
    private javafx.scene.canvas.Canvas canvasFlow;
    @FXML
    private TabPane nodeTabPane;
    @FXML
    private StatusBar statusBar;
    @FXML
    private ToolBar toolBar;
    @FXML
    private Button runButtonToolBar;
    private Button settingsButtonToolBar = null;
    private Label activeThreadsStatusBar = null;
    private CanvasController canvasController;
    private ContextMenu canvasFlowContextMenu;
    private ContextMenu programListContextMenu;
    private PopOver canvasPopOver;
    private PopOver nodeInformation;
    private Boolean skipCanvasClick = false;
    private Scene scene;
    private Double lastCanvasContextMenuX = 0d;
    private Double lastCanvasContextMenuY = 0d;

    public static Controller getInstance() {
        return Controller.controller;
    }

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        Controller.controller = this;

        flowTabPane.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            canvasFlow.setWidth(newSceneWidth.intValue());
            canvasController.drawProgram();
        });

        flowTabPane.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> {
            canvasFlow.setHeight(newSceneHeight.intValue());
            canvasController.drawProgram();
        });

        activeThreadsStatusBar = new Label();
        // Database connection read out, adds the current watcher
        statusBar.getRightItems().add(DatabaseConnectionWatcher.getInstance());
        // Active threads read out
        statusBar.getLeftItems().add(activeThreadsStatusBar);
        statusBar.setText(""); // Stops the status bar from saying OK

        stackPane.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> splitPanePageCentral.setPrefWidth(newSceneWidth.intValue()));
        stackPane.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> splitPanePageCentral.setPrefHeight(newSceneHeight.intValue()));

        canvasPopOver = new PopOver();
        nodeInformation = new PopOver();

        canvasController = new CanvasController(canvasFlow);

        canvasFlow.setOnMouseDragged(canvasController::canvasDragged);
        canvasFlow.setOnMousePressed(canvasController::canvasMouseDown);
        canvasFlow.setOnMouseReleased(event -> skipCanvasClick = canvasController.canvasMouseUp(event));

        canvasFlow.setOnScroll(event -> {
            canvasController.setScale(canvasController.getScale() + event.getDeltaY() / 400);
            canvasController.updateAStarNetwork();
            canvasController.drawProgram();
        });

        canvasFlow.setOnMouseMoved(event -> {
            Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
            if (selectedProgram != null) {
                List<DrawableNode> clickNodes = selectedProgram.getFlowController().getClickedNodes(event.getX() - canvasController.getOffsetWidth(), event.getY() - canvasController.getOffsetHeight());
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
                Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                if (selectedProgram != null) {
                    for (DrawableNode drawableNode : selectedProgram.getFlowController().getSelectedNodes()) {
                        Tab nodeTab = null;
                        // We loop to see if the tab already exists for this node
                        for (Tab loopTab : nodeTabPane.getTabs()) {
                            if (loopTab.getId() != null) {
                                if (loopTab.getId().equals(drawableNode.getUuidStringWithoutHyphen())) {
                                    nodeTab = loopTab;
                                    break;
                                }
                            }
                        }

                        // If the tab does not already exist we create it
                        if (nodeTab == null) {
                            nodeTab = drawableNode.createInterface();


                            // Add context menu to close all tabs
                            ContextMenu contextMenu = new ContextMenu();

                            MenuItem closeAllTabsItem = new MenuItem("Close all tabs");
                            closeAllTabsItem.setOnAction(closeAllEvent -> nodeTabPane.getTabs().clear());

                            MenuItem closeOtherTabsItem = new MenuItem("Close other tabs");
                            closeOtherTabsItem.setId(nodeTab.getId() + "Item");
                            closeOtherTabsItem.setOnAction(closeAllEvent -> {
                                MenuItem menuItemSource = (MenuItem) closeAllEvent.getSource();
                                Tab sourceTab = null;

                                for (Tab loopTab : nodeTabPane.getTabs()) {
                                    if (loopTab.getId() != null) {
                                        if (menuItemSource.getId().contains(loopTab.getId())) {
                                            sourceTab = loopTab;
                                            break;
                                        }
                                    }
                                }

                                nodeTabPane.getTabs().clear();
                                if (sourceTab != null) {
                                    nodeTabPane.getTabs().add(sourceTab);
                                }
                            });

                            contextMenu.getItems().add(closeAllTabsItem);
                            contextMenu.getItems().add(closeOtherTabsItem);
                            nodeTab.setContextMenu(contextMenu);

                            nodeTabPane.getTabs().add(nodeTab);
                        }

                        // Finally we select the tab
                        selectTab(nodeTab);

                        // Set the node as the selected node
                        canvasController.drawProgram();
                    }
                }
            }
        });

        canvasFlow.setOnContextMenuRequested(event -> {
            Program program = SessionManager.getInstance().getCurrentSession().getUser().getCurrentProgram();
            if (program != null) {
                if (canvasFlowContextMenu != null) {
                    canvasFlowContextMenu.hide();
                }

                lastCanvasContextMenuX = event.getX();
                lastCanvasContextMenuY = event.getY();

                List<DrawableNode> clickNodes = program.getFlowController().getClickedNodes(event.getX() - canvasController.getOffsetWidth(), event.getY() - canvasController.getOffsetHeight());

                // This is when right clicking on a specific node, you get the edit node menu
                if (clickNodes.size() > 0) {
                    DrawableNode drawableNode = clickNodes.get(0);

                    Button copyButton = new Button("Copy");
                    copyButton.setOnAction(actionEvent -> {
                        DrawableNode copyNode = program.getFlowController().getNodeByUuidWithoutHyphen(((Button) actionEvent.getSource()).getId().replace("CopyNode-", ""));

                        try {
                            Class<?> clazz = Class.forName("application.node.implementations." + copyNode.getClass().getSimpleName());
                            Constructor<?> ctor = clazz.getConstructor(copyNode.getClass());
                            DrawableNode newNode = (DrawableNode) ctor.newInstance(copyNode);

                            program.getFlowController().addNode(newNode);
                            newNode.save(); // We need to save the node after creating it to assign the ID correctly
                            canvasController.drawProgram();
                        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException ex) {
                            Error.COPY_NODE.record().create(ex);
                        }
                        canvasPopOver.hide();
                    });
                    copyButton.setId("CopyNode-" + drawableNode.getUuidStringWithoutHyphen());

                    Button startNodeButton = new Button("Start Node");
                    startNodeButton.setOnAction(event1 -> {
                        DrawableNode startNode = program.getFlowController().getNodeByUuidWithoutHyphen(((Button) event1.getSource()).getId().replace("StartNode-", ""));
                        program.getFlowController().setStartNode(startNode);
                        canvasController.drawProgram();
                        program.save();
                        canvasPopOver.hide();
                    });
                    startNodeButton.setId("StartNode-" + drawableNode.getUuidStringWithoutHyphen());

                    Button removeNodeButton = new Button("Remove Node");
                    removeNodeButton.setOnAction(actionEvent -> {
                        DrawableNode removedNode = program.getFlowController().getNodeByUuidWithoutHyphen(((Button) actionEvent.getSource()).getId().replace("RemoveNode-", ""));

                        program.getFlowController().removeNode(removedNode);
                        removedNode.delete();

                        canvasController.drawProgram();

                        closeDeleteNodeTabs(removedNode);
                        canvasPopOver.hide();
                    });
                    removeNodeButton.setId("RemoveNode-" + drawableNode.getUuidStringWithoutHyphen());


                    VBox vBox = new VBox(5);
                    HBox hBox = new HBox(5);
                    hBox.setAlignment(Pos.CENTER);

                    // Text showing title of node
                    Text nodeText = new Text(drawableNode.getContainedText());
                    nodeText.setFill(drawableNode.getFillColour());
                    nodeText.setFont(AppParams.getFont(14));

                    // Color picker for node preset to current color
                    ColorPicker colorPicker = new ColorPicker();
                    colorPicker.setValue(drawableNode.getFillColour());
                    colorPicker.setStyle("-fx-background-color: null; -fx-color-label-visible: false;");

                    colorPicker.setOnAction(t -> {
                        NodeColour nodeColour = new NodeColour(colorPicker.getValue(), drawableNode.getNodeType());
                        DataBank.getNodeColours().addNodeColour(nodeColour);
                        nodeColour.save();

                        canvasController.drawProgram();
                    });

                    hBox.getChildren().add(nodeText);
                    hBox.getChildren().add(colorPicker);

                    vBox.setPadding(new Insets(10, 6, 10, 6));

                    vBox.getChildren().add(hBox);
                    vBox.getChildren().add(copyButton);
                    vBox.getChildren().add(startNodeButton);
                    vBox.getChildren().add(removeNodeButton);

                    canvasPopOver.show(canvasFlow, event.getScreenX(), event.getScreenY());
                    canvasPopOver.setContentNode(vBox);
                } else { // This is when not right clicking on a node, you get the create node menu
                    canvasPopOver.hide();

                    List<MenuItem> nodeMenuItems = DrawableNode.getNodeNames().stream().map(Controller.this::createNodeMenuItem).collect(Collectors.toList());

                    canvasFlowContextMenu = new ContextMenu();
                    canvasFlowContextMenu.getItems().addAll(nodeMenuItems);
                    canvasFlowContextMenu.show(canvasFlow, event.getScreenX(), event.getScreenY());
                }
            }
        });

        reloadPrograms();

        programList.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            private String clickedName = "";

            @Override
            public void handle(ContextMenuEvent event) {
                if (programListContextMenu != null) {
                    programListContextMenu.hide();
                }

                if (programList.getSelectionModel().getSelectedItem() != null) {
                    clickedName = programList.getSelectionModel().getSelectedItem().getName();
                }

                MenuItem menuItemNewProgram = new MenuItem("New Program");
                menuItemNewProgram.setOnAction(event1 -> {
                    Program program = Program.create(Program.class);
                    program.setName("New program");
                    program.setParentUser(SessionManager.getInstance().getCurrentSession().getUser());
                    program.save();

                    programList.getItems().add(program);
                    reorderProgramList();
                });

                MenuItem menuItemDeleteProgram = new MenuItem("Delete Program");
                menuItemDeleteProgram.setOnAction(event1 -> {
                    Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();

                    application.gui.dialog.Dialog confirmDialog = new ConfirmDialog()
                            .content("Are you sure you want to delete " + selectedProgram.getName())
                            .title("Deleting program")
                            .onYesAction(() -> {
                                // First close all the node tabs that are open as we will be deleting these nodes
                                selectedProgram.getFlowController().getNodes().forEach(Controller.this::closeDeleteNodeTabs);
                                // Finally delete the program
                                selectedProgram.delete();
                                programList.getItems().remove(selectedProgram);
                            });
                    confirmDialog.show();
                });

                MenuItem menuItemCompile = new MenuItem("Compile...");
                menuItemCompile.setOnAction(event1 -> {
                    Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                    selectedProgram.compile();
                });

                MenuItem menuItemRun = new MenuItem("Run...");
                menuItemRun.setOnAction(event1 -> {
                    Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                    selectedProgram.run();
                });

                // Add program Lock/Unlock to Flow Tab
                MenuItem lock = new MenuItem("Lock/Unlock");
                lock.setOnAction(event1 -> {
                    Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                    if (selectedProgram != null) {
                        selectedProgram.setLocked(!selectedProgram.getLocked());
                        selectedProgram.save();
                        updateFlowLockedStatus();
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
                    programListContextMenu.getItems().add(lock);
                }

                programListContextMenu.show(programList, event.getScreenX(), event.getScreenY());

                clickedName = null;
            }
        });

        programList.setEditable(true);
        Callback<ListView<Program>, ListCell<Program>> onCommit = TextFieldListCell.forListView(new StringConverter<Program>() {
            @Override
            public String toString(Program program) {
                if (program != null) {
                    return "" + program.getName();
                } else {
                    return "Unnamed";
                }
            }

            @Override
            public Program fromString(String input) {
                Program program = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                if (program != null) {
                    program.setName(input);
                    program.save();
                    reorderProgramList();
                }

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
                    // If we only have one program in the list and we rename it newProgram is null and gets deselected in the list
                    // But we still want the flow to show this renamed program so don't update these values with null
                    if (newProgram != null) {
                        SessionManager.getInstance().getCurrentSession().setSelectedProgram(newProgram);

                        User currentUser = SessionManager.getInstance().getCurrentSession().getUser();
                        currentUser.setCurrentProgram(newProgram);
                        currentUser.save();

                        newProgram.loadNodesToFlowController();
                        updateFlowLockedStatus();
                    }
                    canvasController.drawProgram();
                    canvasController.updateAStarNetwork();
                });

        User currentUser = SessionManager.getInstance().getCurrentSession().getUser();

        if (currentUser != null && currentUser.getCurrentProgram() != null) {
            programList.getSelectionModel().select(currentUser.getCurrentProgram());
            programList.scrollTo(currentUser.getCurrentProgram());
        }

        leftAccordion.setExpandedPane(programTitlePane);


        // Run button on toolbar
        runButtonToolBar = new Button();
        runButtonToolBar.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.PLAY).build());
        runButtonToolBar.setStyle("-fx-background-color: null;");
        runButtonToolBar.setOnMouseEntered(event -> runButtonToolBar.setStyle("-fx-background-color: lightgray;"));
        runButtonToolBar.setOnMousePressed(event -> runButtonToolBar.setStyle("-fx-background-color: darkgray;"));
        runButtonToolBar.setOnMouseReleased(event -> runButtonToolBar.setStyle("-fx-background-color: lightgray;"));
        runButtonToolBar.setOnMouseExited(event -> runButtonToolBar.setStyle("-fx-background-color: null;"));
        runButtonToolBar.setOnAction(event -> {
                    Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                    if (selectedProgram != null) {
                        selectedProgram.run();
                    }
                }
        );

        // Settings button on toolbar
        settingsButtonToolBar = new Button();
        settingsButtonToolBar.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.COGS).build());
        settingsButtonToolBar.setStyle("-fx-background-color: null;");
        settingsButtonToolBar.setOnMouseEntered(event -> settingsButtonToolBar.setStyle("-fx-background-color: lightgray;"));
        settingsButtonToolBar.setOnMousePressed(event -> settingsButtonToolBar.setStyle("-fx-background-color: darkgray;"));
        settingsButtonToolBar.setOnMouseReleased(event -> settingsButtonToolBar.setStyle("-fx-background-color: lightgray;"));
        settingsButtonToolBar.setOnMouseExited(event -> settingsButtonToolBar.setStyle("-fx-background-color: null;"));
        settingsButtonToolBar.setOnAction(event -> new SettingsWindow());

        // Setup tool bar  here
        toolBar.getItems().add(runButtonToolBar);
        toolBar.getItems().add(new Separator());
        toolBar.getItems().add(settingsButtonToolBar);

        // Setup menu bar here
        menuBarMenuItemQuit.setOnAction(event -> ((Stage) scene.getWindow()).close());
        menuBarMenuItemLog.setOnAction(event -> new LogWindow());
        menuBarMenuItemThread.setOnAction(event -> new ThreadWindow());
        menuBarMenuItemExportProgram.setOnAction(event -> new ExportWindow(ExportWindow.EXPORT_PROGRAM));
        menuBarMenuItemExportNode.setOnAction(event -> new ExportWindow(ExportWindow.EXPORT_NODE));
        menuBarMenuItemExportNodeColours.setOnAction(event -> new ExportWindow(ExportWindow.EXPORT_NODE_COLOURS));
        menuBarMenuItemImport.setOnAction(event -> new ImportWindow());
        menuBarMenuItemError.setOnAction(event -> new ErrorWindow());
        menuBarMenuItemClearRequestData.setOnAction(event -> {
            RecordedHeaderDAO recordedHeaderDAO = new RecordedHeaderDAO();
            RecordedRequestDAO recordedRequestDAO = new RecordedRequestDAO();
            RecordedProxyDAO recordedProxyDAO = new RecordedProxyDAO();

            recordedHeaderDAO.deleteAllRecordedHeaders();
            recordedRequestDAO.deleteAllRecordedRequests();
            recordedProxyDAO.deleteAllRecordedProxies();
        });

        nodeTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        updateThreadCount(ThreadManager.getInstance().getActiveThreads());

        // As a final step we draw the loaded program with a fully updated network
        Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
        if (selectedProgram != null) {
            selectedProgram.loadNodesToFlowController();
            updateFlowLockedStatus();
        }

        canvasController.drawProgram(); // Not 100% sure why we need to draw first and then update network after
        canvasController.updateAStarNetwork();
    }

    public MenuItem createNodeMenuItem(String className) {
        MenuItem menuItem = new MenuItem("Add " + className);
        menuItem.setOnAction(event -> {
            Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();

            try {
                Class<DrawableNode> clazz = (Class<DrawableNode>) Class.forName("application.node.implementations." + className);
                DrawableNode newNode = BatchNode.create(clazz);
                newNode.setContainedText("New " + className);
                newNode.setX(lastCanvasContextMenuX - canvasController.getOffsetWidth());
                newNode.setY(lastCanvasContextMenuY - canvasController.getOffsetHeight());

                selectedProgram.getFlowController().addNode(newNode);
                newNode.save();
                canvasController.drawProgram();
            } catch (ClassNotFoundException ex) {
                Error.CREATE_NODE_MENU_ITEM.record().create(ex);
            }
        });
        menuItem.setId(className + "-");

        return menuItem;
    }

    public void selectTab(Tab tab) {
        SingleSelectionModel<Tab> selectionModel = nodeTabPane.getSelectionModel();
        selectionModel.select(tab);
    }

    public TextField createNodeNameField(DrawableNode drawableNode) {
        TextField nameField = new TextField();
        nameField.setId("fieldName-" + drawableNode.getUuidStringWithoutHyphen());
        nameField.setText(drawableNode.getContainedText());

        nameField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                DrawableNode nodeToUpdate = selectedProgram.getFlowController().getNodeByUuidWithoutHyphen(textField.getId().replace("fieldName-", ""));
                nodeToUpdate.setContainedText(textField.getText());
                selectedProgram.getFlowController().checkConnections(); // Renaming a node might make or break connections

                nodeTabPane.getTabs().stream().filter(loopTab -> loopTab.getId() != null).forEach(loopTab -> {
                    if (loopTab.getId().equals(nodeToUpdate.getUuidStringWithoutHyphen().toString())) {
                        loopTab.setText(textField.getText());
                    }
                });

                nodeToUpdate.save();
                canvasController.drawProgram();
            }
        });

        return nameField;
    }

    public TextField createNextNodeField(DrawableNode drawableNode) {
        TextField nameField = new TextField();
        nameField.setId("fieldNextNode-" + drawableNode.getUuidStringWithoutHyphen());
        nameField.setText(drawableNode.getNextNodeToRun());

        nameField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
            DrawableNode nodeToUpdate = selectedProgram.getFlowController().getNodeByUuidWithoutHyphen(textField.getId().replace("fieldNextNode-", ""));
            nodeToUpdate.setNextNodeToRun(textField.getText());
            selectedProgram.getFlowController().checkConnections(); // Renaming a node might make or break connections

            nodeToUpdate.save();
            canvasController.drawProgram();
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

        nameFieldLabel.setOnMouseClicked(event -> {
            VBox vBox = new VBox(5);
            vBox.setPadding(new Insets(10, 6, 10, 6));
            vBox.setAlignment(Pos.CENTER);
            vBox.getChildren().add(new Label("Type: " + node.getNodeType()));
            vBox.getChildren().add(new Label("Name: " + node.getContainedText()));
            vBox.getChildren().add(makeSelectable(new Label("ID: " + node.getUuidString())));

            nodeInformation.show(nameFieldLabel, event.getScreenX(), event.getScreenY());
            nodeInformation.setContentNode(vBox);
        });

        nameFieldLabel.setOnMouseEntered(event -> {
            nameFieldLabel.setUnderline(true);
            setCursor(Cursor.HAND);
        });

        nameFieldLabel.setOnMouseExited(event -> {
            nameFieldLabel.setUnderline(false);
            setCursor(Cursor.DEFAULT);
        });

        nameFieldLabel.setText(node.getNodeType());
        nameFieldLabel.setTextFill(Color.GRAY);
        return nameFieldLabel;
    }

    public AnchorPane getContentAnchorPaneOfTab(Tab tab) {
        Node content = tab.getContent();
        if (content instanceof ScrollPane) {
            return (AnchorPane) ((ScrollPane) content).getContent();
        } else if (content instanceof AnchorPane) {
            return (AnchorPane) content;
        }

        // Something odd happened, return null so we can debug
        return null;
    }

    public Tab createDefaultNodeTab(DrawableNode node) {
        return createDefaultNodeTab(node, true);
    }

    public Tab createDefaultNodeTab(DrawableNode node, Boolean scrollable) {
        Tab tab = new Tab();
        tab.setText(node.getContainedText());
        tab.setId(node.getUuidStringWithoutHyphen().toString());

        AnchorPane tabAnchorPane = new AnchorPane();

        VBox vbox = new VBox(5);
        vbox.setLayoutX(11);
        vbox.setLayoutY(13);
        vbox.setAlignment(Pos.BASELINE_LEFT);

        vbox.setMaxWidth(Integer.MAX_VALUE);
        AnchorPane.setLeftAnchor(vbox, 0.0);
        AnchorPane.setRightAnchor(vbox, 0.0);

        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(createNodeNameLabel());
        hbox.getChildren().add(createNodeNameField(node));
        hbox.getChildren().add(createNodeInfoLabel(node));
        hbox.getChildren().add(createNextNodeLabel());
        hbox.getChildren().add(createNextNodeField(node));

        vbox.getChildren().add(hbox);
        vbox.getChildren().add(new Separator());

        tabAnchorPane.getChildren().add(vbox);

        tabAnchorPane.setMaxHeight(Integer.MAX_VALUE);
        tabAnchorPane.setMaxWidth(Integer.MAX_VALUE);

        UI.setAnchorMargins(tabAnchorPane, 0.0, 0.0, 0.0, 0.0);

        // Makes it possible to scroll up and down
        if (scrollable) {
            // The ordering here is Tab < ScrollPane < AnchorPane
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            UI.setAnchorMargins(scrollPane, 0.0, 0.0, 0.0, 0.0);
            scrollPane.setContent(tabAnchorPane);
            tab.setContent(scrollPane);
        } else {
            tab.setContent(tabAnchorPane);
        }

        return tab;
    }

    public void updateCanvasControllerNow() {
        canvasController.drawProgram();
    }

    // Use this one when not on GUI thread
    public void updateCanvasControllerLater() {
        Platform.runLater(canvasController::drawProgram);
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

    public void updateThreadCount(Integer threadCount) {
        class GUIUpdate implements Runnable {
            Integer threadCount;

            GUIUpdate(Integer threadCount) {
                this.threadCount = threadCount;
            }

            public void run() {
                activeThreadsStatusBar.setText("Active threads: " + threadCount);
            }
        }

        Platform.runLater(new GUIUpdate(threadCount));
    }

    public void addNewProgram(Program program) {
        class GUIUpdate implements Runnable {
            Program program;

            GUIUpdate(Program program) {
                this.program = program;
            }

            public void run() {
                programList.getItems().add(program);
            }
        }

        Platform.runLater(new GUIUpdate(program));
    }

    public void setWindowTitle() {
        class GUIUpdate implements Runnable {

            GUIUpdate() {
            }

            public void run() {
                Main.getInstance().getMainStage().setTitle(AppParams.APP_TITLE + " " + AppParams.APP_VERSION);
            }
        }

        Platform.runLater(new GUIUpdate());
    }

    public void closeDeleteNodeTabs(DrawableNode removedNode) {
        Tab tabToRemove = null;
        for (Tab loopTab : nodeTabPane.getTabs()) {
            if (loopTab.getId() != null) {
                if (loopTab.getId().equals(removedNode.getUuidStringWithoutHyphen().toString())) {
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

    public void closeAllNodeTabs() {
        class GUIUpdate implements Runnable {
            GUIUpdate() {
            }

            public void run() {
                List<Tab> tabs = new ArrayList<>();
                tabs.addAll(nodeTabPane.getTabs());
                for (Tab loopTab : tabs) {
                    loopTab.getTabPane().getTabs().remove(loopTab);
                }
            }
        }

        Platform.runLater(new GUIUpdate());
    }

    public void updateFlowLockedStatus() {
        class GUIUpdate implements Runnable {
            GUIUpdate() {
            }

            public void run() {
                Program selectedProgram = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
                if (selectedProgram != null) {
                    Tab flowTab = flowTabPane.getTabs().get(0);
                    if (selectedProgram.getLocked()) {

                        Label awesomeLabel = new Label();
                        awesomeLabel.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.LOCK).build());
                        awesomeLabel.setOnMouseClicked(mouseEvent -> {
                            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                                if (mouseEvent.getClickCount() == 2) {
                                    selectedProgram.setLocked(!selectedProgram.getLocked());
                                    selectedProgram.save();
                                    updateFlowLockedStatus();
                                }
                            }
                        });

                        flowTab.setGraphic(awesomeLabel);
                    } else {
                        flowTab.setGraphic(null);
                    }
                }
            }
        }

        Platform.runLater(new GUIUpdate());
    }

    public void reloadPrograms() {
        if (programList != null) {
            programList.getItems().clear();

            User currentUser = SessionManager.getInstance().getCurrentSession().getUser();

            ProgramDAO programDAO = new ProgramDAO();
            List<Program> programs = programDAO.getProgramsByUser(currentUser);

            programList.getItems().addAll(programs);
            reorderProgramList();
        }
    }

    // Order programs by name
    public void reorderProgramList() {
        // Order programs by name
        ObservableList<Program> list = programList.getItems();
        Collections.sort(list);
        Collections.reverse(list);
        programList.setItems(list);

        User currentUser = SessionManager.getInstance().getCurrentSession().getUser();

        if (currentUser.getCurrentProgram() != null) {
            programList.getSelectionModel().select(currentUser.getCurrentProgram());
            programList.scrollTo(currentUser.getCurrentProgram());
        }
    }

    private Label makeSelectable(Label label) {
        StackPane textStack = new StackPane();
        TextField textField = new TextField(label.getText());
        textField.setEditable(false);
        textField.setStyle(
                "-fx-background-color: transparent; -fx-background-insets: 0; -fx-background-radius: 0; -fx-padding: 0;"
        );

        // The invisible label is a hack to get the textField to size like a label.
        Label invisibleLabel = new Label();
        invisibleLabel.textProperty().bind(label.textProperty());
        invisibleLabel.setVisible(false);
        textStack.getChildren().addAll(invisibleLabel, textField);
        label.textProperty().bindBidirectional(textField.textProperty());
        label.setGraphic(textStack);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        return label;
    }
}