package sde.application.utils.managers;

import sde.application.error.RecordedError;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ErrorManager {
    private ObservableList<RecordedError> errors;
    private static ErrorManager instance;

    public ErrorManager() {
        instance = this;
        errors = FXCollections.observableArrayList();
    }

    public void addError(RecordedError error) {
        if (errors != null && error != null) {
            errors.add(error);
        }
    }

    public ObservableList<RecordedError> getErrors() {
        return errors;
    }

    public static ErrorManager getInstance() {
        if (instance == null) {
            instance = new ErrorManager();
        }
        return instance;
    }
}

