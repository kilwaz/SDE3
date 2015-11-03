package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainVictrex4 {
    public MainVictrex4() {

        File data = new File("C:\\Users\\alex\\Downloads\\Victrex4data.txt");
        System.out.println(data.exists());

        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String line;
            // Get hierarchy stuff
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\t");
                if(split.length == 2 && !"-1".equals(split[1]) && !"".equals(split[1])){
                    System.out.println("update fr_emp_salaries set recommended_increase = " + split[1] + " where person_seq_id = (select person_seq_id from fr_emp_details where emplid = '" + split[0] +"' and period_seq_id = 315) and period_seq_id = 315;");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Completed");
    }
    public static void main(String[] args) {
        System.out.println("Running");

        new MainVictrex4();
    }
}
