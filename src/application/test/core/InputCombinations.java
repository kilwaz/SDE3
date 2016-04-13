package application.test.core;

import java.util.ArrayList;
import java.util.List;

public class InputCombinations {
    private List<InputCombination> inputCombinationList = new ArrayList<>();

    public InputCombinations() {

    }

    public InputCombinations apply(TestInputList testInput) {
        if (inputCombinationList.size() == 0) {
            for (String val : testInput.getList()) {
                InputCombination inputCombination = new InputCombination();
                inputCombination.add(testInput.getName(), val);
                inputCombinationList.add(inputCombination);
            }
        } else {
            if (testInput.getList().length > 1) { // If there is more than one new combination we will need to duplicate everything for each new one
                List<InputCombination> combinationsToAdd = new ArrayList<>();
                for (String val : testInput.getList()) {
                    for (InputCombination inputCombination : inputCombinationList) {
                        InputCombination duplicate = inputCombination.duplicate();
                        duplicate.add(testInput.getName(), val);
                        combinationsToAdd.add(duplicate);
                    }
                }
                inputCombinationList = combinationsToAdd;
            } else if (testInput.getList().length == 1) { // If only one go through and add it to all the current combinations
                for (InputCombination inputCombination : inputCombinationList) {
                    inputCombination.add(testInput.getName(), testInput.getList()[0]);
                }
            }
        }

        return this;
    }

    public List<InputCombination> getInputCombinationList() {
        return inputCombinationList;
    }
}

