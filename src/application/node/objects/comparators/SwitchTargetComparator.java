package application.node.objects.comparators;

import application.node.objects.Switch;

import java.util.Comparator;

public class SwitchTargetComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        Switch switch1 = (Switch) o1;
        Switch switch2 = (Switch) o2;

        String target1 = switch1.getTarget();
        String target2 = switch2.getTarget();

        if (target1 == null && target2 == null) {
            return 0;
        } else if (target1 == null) {
            return 1;
        } else if (target2 == null) {
            return -1;
        } else {
            return target1.compareTo(target2);
        }
    }
}
