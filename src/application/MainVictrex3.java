package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainVictrex3 {
    public MainVictrex3() {

        File data = new File("C:\\Users\\alex\\Downloads\\Victrex3data.txt");
        System.out.println(data.exists());

        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String line;
            // Get hierarchy stuff
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\t");
                System.out.println("update fr_emp_salaries set lumpsum_incr = " + split[2] + " where person_seq_id = (select person_seq_id from fr_emp_details where emplid = '" + split[0] +"' and period_seq_id = 315) and period_seq_id = 315;");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Completed");
    }
    public static void main(String[] args) {
        System.out.println("Running");

        new MainVictrex3();
    }
}
