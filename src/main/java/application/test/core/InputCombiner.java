package application.test.core;

import java.util.ArrayList;
import java.util.List;

public class InputCombiner {
    List<TestInputList> testInputList = new ArrayList<>();

    public InputCombiner() {

    }

    public InputCombiner addTestInput(TestInputList testInput) {
        testInputList.add(testInput);
        return this;
    }

    public InputCombinations combine() {
        InputCombinations inputCombinations = new InputCombinations();

        testInputList.forEach(inputCombinations::apply);

        return inputCombinations;
    }
}
