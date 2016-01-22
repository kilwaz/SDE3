package application;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainScapa {

    HashMap<String, Department> departments = new HashMap<>();
    File hierarchyDetails = new File("C:\\Users\\alex\\Downloads\\ScapaHierarchy.txt");

    public MainScapa() {
        // Make all departments
        try (BufferedReader br = new BufferedReader(new FileReader(hierarchyDetails))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");

                departments.put(split[0], new Department(split[0], split[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Match up all department parents
        try (BufferedReader br = new BufferedReader(new FileReader(hierarchyDetails))) {
            String line;
            // Make all departments
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");

                Department department = departments.get(split[0]);
                department.setParent(departments.get(split[2]));
                if (departments.get(split[2]) != null) {
                    departments.get(split[2]).addChild(department);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        printDeptChildren(departments.get("1"));
    }

    public void printDeptChildren(Department department) {
        printDeptChildren(department, 0);
    }

    public void printDeptChildren(Department department, Integer currentLevel) {
        System.out.println(StringUtils.repeat("\t", currentLevel) + " - " + department.getName() + " (" + department.getId() + ")");
        for (Department childDepartment : department.getChildren()) {
            printDeptChildren(childDepartment, currentLevel + 1);
        }
    }

    public static void main(String[] args) {
        System.out.println("Checking departments");

        new MainScapa();
    }

    private class Department {
        private String id = null;
        private Department parent = null;
        private String name = null;
        private List<Department> children = new ArrayList<>();

        public void addChild(Department department) {
            children.add(department);
        }

        public List<Department> getChildren() {
            return children;
        }

        public Department(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Department getParent() {
            return parent;
        }

        public void setParent(Department parent) {
            this.parent = parent;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}


