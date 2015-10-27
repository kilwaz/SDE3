package application.utils.managers;

import application.error.Error;
import application.error.RecordedError;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ErrorManager {
    private ObservableList<RecordedError> errors;
    private static ErrorManager instance;

    public ErrorManager() {
        instance = this;
        errors = FXCollections.observableArrayList();
    }

    public void addError(application.error.RecordedError error) {
        errors.add(error);
    }

    public ObservableList<RecordedError> getErrors() {
        return errors;
    }

    public static ErrorManager getInstance() {
        return instance;
    }
}

