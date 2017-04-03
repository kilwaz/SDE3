package application.test.core;

import application.node.objects.Input;

import java.util.ArrayList;
import java.util.List;

public class InputCombination {
    List<Input> inputs = new ArrayList<>();

    public InputCombination() {

    }

    public InputCombination add(String name, String value) {
        Input testInput = Input.create(Input.class);
        testInput.setVariableName(name);
        testInput.setVariableValue(value);
        inputs.add(testInput);
        return this;
    }

    public InputCombination duplicate() {
        InputCombination duplicate = new InputCombination();

        for (Input input : inputs) {
            duplicate.add(input.getVariableName(), input.getVariableValue());
        }

        return duplicate;
    }

    public List<Input> getInputs() {
        return inputs;
    }
}
