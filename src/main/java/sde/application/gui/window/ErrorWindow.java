package sde.application.gui.window;

import sde.application.error.Error;
import sde.application.error.RecordedError;
import sde.application.gui.UI;
import sde.application.utils.managers.ErrorManager;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

import java.net.URL;

public class ErrorWindow extends SDEWindow {
    private static Logger log = Logger.getLogger(ErrorWindow.class);

    public ErrorWindow() {
        super();
        init();
    }

    private void init() {
        try {
            TableView<RecordedError> errorTableView = new TableView<>();

            TableColumn errorOccurredAtColumn = new TableColumn("Occurred At");
            errorOccurredAtColumn.setCellValueFactory(new PropertyValueFactory<Error, String>("OccurredAt"));

            TableColumn errorCodeColumn = new TableColumn("Error Code");
            errorCodeColumn.setCellValueFactory(new PropertyValueFactory<Error, Integer>("Code"));

            TableColumn errorReferenceColumn = new TableColumn("Error Reference");
            errorReferenceColumn.setCellValueFactory(new PropertyValueFactory<Error, Integer>("Reference"));

            TableColumn errorDescriptionColumn = new TableColumn("Description");
            errorDescriptionColumn.setCellValueFactory(new PropertyValueFactory<Error, String>("Description"));

            TableColumn errorAdditionalInformationColumn = new TableColumn("Additional Information");
            errorAdditionalInformationColumn.setCellValueFactory(new PropertyValueFactory<Error, Exception>("AdditionalInformation"));

            TableColumn errorClassNameColumn = new TableColumn("Class Name");
            errorClassNameColumn.setCellValueFactory(new PropertyValueFactory<Error, Exception>("ClassName"));

            TableColumn errorLineNumberColumn = new TableColumn("Line Number");
            errorLineNumberColumn.setCellValueFactory(new PropertyValueFactory<Error, Exception>("LineNumber"));

            TableColumn errorExceptionColumn = new TableColumn("Exception");
            errorExceptionColumn.setCellValueFactory(new PropertyValueFactory<Error, Exception>("Exception"));

            errorTableView.getColumns().addAll(errorOccurredAtColumn, errorCodeColumn, errorReferenceColumn, errorDescriptionColumn, errorAdditionalInformationColumn, errorClassNameColumn, errorLineNumberColumn, errorExceptionColumn);
            errorTableView.setItems(ErrorManager.getInstance().getErrors());

            AnchorPane errorAnchor = new AnchorPane();

            errorAnchor.setPadding(new Insets(7, 11, 7, 11));
            errorAnchor.getChildren().add(errorTableView);

            UI.setAnchorMargins(errorAnchor, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(errorTableView, 0.0, 0.0, 0.0, 0.0);

            createScene(errorAnchor, 900, 800);

            this.setTitle("Error Report (" + ErrorManager.getInstance().getErrors().size() + " errors)");

            URL url = getClass().getResource("/icon.png");
            this.getIcons().add(new Image(url.toExternalForm()));

            this.show();
        } catch (Exception ex) {
            Error.CREATE_ERROR_WINDOW.record().create(ex);
        }
    }
}