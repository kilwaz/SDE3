package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainVictrex {

    public MainVictrex() {

        HashMap<String, Hierarchy> hierarchyList = new HashMap<>();
        HashMap<String, Employee> employeeList = new HashMap<>();
        HashMap<String, Department> departments = new HashMap<>();

        File hierarchyDetails = new File("C:\\Users\\alex\\Downloads\\VictrexHierarchy.txt");
        File employeeDetails = new File("C:\\Users\\alex\\Downloads\\VictrexEmployeeDetails.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(hierarchyDetails))) {
            String line;
            // Get hierarchy stuff
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\t");

                hierarchyList.put(split[0], new Hierarchy(split[0], split[1], split[2], split[3], split[4], split[5], split[6], split[7], split[8]));

                //System.out.println(line);
            }

            // Get employee stuff
            BufferedReader br2 = new BufferedReader(new FileReader(employeeDetails));
            while ((line = br2.readLine()) != null) {
                String[] split = line.split("\t");

                employeeList.put(split[0], new Employee(split[0], split[1], split[2], split[3], split[4]));

                //System.out.println("EMPLOYEE " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Create the departments
        for (Hierarchy hierarchy : hierarchyList.values()) {
            List<String> departmentChain = new ArrayList<>();
            while (hierarchy != null) {
                departmentChain.add(hierarchy.getSupervisorID());
                hierarchy = hierarchyList.get(hierarchy.getSupervisorID());
            }

            // Make sure all the departments exist
            String finalStr = "";
            Collections.reverse(departmentChain);
            String previousDept = null;
            for (String dept : departmentChain) {
                if (departments.get(dept) == null) {
                    System.out.println("New department " + dept);
                    departments.put(dept, new Department(dept));
                }

                previousDept = dept;

                if (dept.length() > 1) {
                    finalStr += dept + ",";
                }
            }

            // Set all the parents
            previousDept = null;
            for (String dept : departmentChain) {
                if (previousDept != null) {

                    if (departments.get(dept).getParent() != null) {
                        if (departments.get(dept).getParent() != departments.get(previousDept)) {
                            //System.out.println("MISMATCH! " + departments.get(dept).getParent().getId() + " vs " + departments.get(previousDept).getId());
                        }
                    }

                    departments.get(dept).setParent(departments.get(previousDept));
                }

                previousDept = dept;
            }

            // Remove final comma
            if (finalStr.length() > 1) {
                finalStr = finalStr.substring(0, finalStr.length() - 1);
            }

            System.out.println(finalStr);
        }

        // Final output
        System.out.println("FINAL OUTPUT!!");
        int count = 1;
        for (Department department : departments.values()) {
            if (department.getParent() != null) {
                Employee emp = employeeList.get(department.getId());
                Employee empParent = employeeList.get(department.getParent().getId());

                if(emp != null && empParent != null){
                    String departmentName = (emp.getPreferredName() == null ? emp.getFirstName() : emp.getPreferredName()) + ", " + emp.getLastName();
                    String departmentNameParent = (empParent.getPreferredName() == null ? empParent.getFirstName() : empParent.getPreferredName()) + ", " + empParent.getLastName();

                    System.out.println(departmentName + "|" + hierarchyList.get(department.getId()).getDeptFunction() + "|" + departmentNameParent);
                    count++;
                } else {
                    String departmentName = emp.getPreferredName() == null ? emp.getFirstName() : emp.getPreferredName() + ", " + emp.getLastName();

                    System.out.println(departmentName + "|" + hierarchyList.get(department.getId()).getDeptFunction() + "|");
                    count++;
                }
            } else {
                Employee emp = employeeList.get(department.getId());

                if(emp != null){
                    String departmentName = (emp.getPreferredName() == null ? emp.getFirstName() : emp.getPreferredName()) + ", " + emp.getLastName();

                    System.out.println(departmentName + "|" + hierarchyList.get(department.getId()).getDeptFunction() + "|");
                    count++;
                }
            }
        }

        System.out.println("Employees in hierarchy = " + hierarchyList.size());
        System.out.println("Employees Details = " + employeeList.size());
        System.out.println("Departments = " + departments.size());
    }

    public static void main(String[] args) {
        System.out.println("Making departments import");

        new MainVictrex();
    }

    private class Department {
        private String id = null;
        private Department parent = null;

        public Department(String id) {
            this.id = id;
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
    }

    private class Employee {
        private String emplID = null;
        private String lastName = null;
        private String middleName = null;
        private String firstName = null;
        private String preferredName = null;

        public Employee(String emplID, String lastName, String middleName, String firstName, String preferredName) {
            this.emplID = emplID;
            this.lastName = lastName;
            if (!"NULLVALUE".equals(middleName)) {
                this.middleName = middleName;
            }

            this.firstName = firstName;
            if (!"NULLVALUE".equals(preferredName)) {
                this.preferredName = preferredName;
            }
        }

        public String getPreferredName() {
            return preferredName;
        }

        public void setPreferredName(String preferredName) {
            this.preferredName = preferredName;
        }

        public String getEmplID() {
            return emplID;
        }

        public void setEmplID(String emplID) {
            this.emplID = emplID;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }

    private class Hierarchy {
        private String emplID = null;
        private String companyCode = null;
        private String deptID = null;
        private String supervisorID = null;
        private String costCentreCD = null;
        private String countryCdHost = null;
        private String deptFunction = null;
        private String currencyCD = null;
        private String year = null;

        public Hierarchy(String emplID, String companyCode, String deptID, String supervisorID, String costCentreCD, String countryCdHost, String deptFunction, String currencyCD, String year) {
            this.emplID = emplID;
            this.companyCode = companyCode;
            this.deptID = deptID;
            this.supervisorID = supervisorID;
            this.costCentreCD = costCentreCD;
            this.countryCdHost = countryCdHost;
            this.deptFunction = deptFunction;
            this.currencyCD = currencyCD;
            this.year = year;
        }

        public String getEmplID() {
            return emplID;
        }

        public void setEmplID(String emplID) {
            this.emplID = emplID;
        }

        public String getCompanyCode() {
            return companyCode;
        }

        public void setCompanyCode(String companyCode) {
            this.companyCode = companyCode;
        }

        public String getDeptID() {
            return deptID;
        }

        public void setDeptID(String deptID) {
            this.deptID = deptID;
        }

        public String getSupervisorID() {
            return supervisorID;
        }

        public void setSupervisorID(String supervisorID) {
            this.supervisorID = supervisorID;
        }

        public String getCostCentreCD() {
            return costCentreCD;
        }

        public void setCostCentreCD(String costCentreCD) {
            this.costCentreCD = costCentreCD;
        }

        public String getCountryCdHost() {
            return countryCdHost;
        }

        public void setCountryCdHost(String countryCdHost) {
            this.countryCdHost = countryCdHost;
        }

        public String getDeptFunction() {
            return deptFunction;
        }

        public void setDeptFunction(String deptFunction) {
            this.deptFunction = deptFunction;
        }

        public String getCurrencyCD() {
            return currencyCD;
        }

        public void setCurrencyCD(String currencyCD) {
            this.currencyCD = currencyCD;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }
}
