package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainWhatsApp {

    public MainWhatsApp() {
        File data = new File("C:\\Users\\alex\\Downloads\\chat.txt");
        System.out.println("Exists: " + data.exists());

        List<Message> miliMessage = new ArrayList<>();
        List<Message> alexMessage = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(data))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    if (line.length() > 0) {
                        String date = line.substring(0, 10);
                        String time = line.substring(13, 20);
                        String name = line.substring(22, 32);
                        String message = line.substring(34);

                        if (!message.contains("image omitted") && !message.contains("\uD83D")) {
                            if ("Alex Brown".equals(name)) {
                                alexMessage.add(new Message(date, time, name, message));
                            } else if ("Mili Gudka".equals(name)) {
                                miliMessage.add(new Message(date, time, name, message));
                            }
                        }

                        //System.out.println("Date: '" + date + "' time '" + time + "' name '" + name + "' message '" + message + "'");
                    }
                } catch (Exception ex) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Alex total " + alexMessage.size());
        System.out.println("Mili total " + miliMessage.size());

        for (int i = 0; i < 5; i++) {
            Message alex = getRandomMessage(alexMessage);
            Message mili = getRandomMessage(miliMessage);

            System.out.println("(" + alex.getDate() + " " + alex.getTime() + ") - " + alex.getName() + ": " + alex.getMessage());
            System.out.println("(" + mili.getDate() + " " + mili.getTime() + ") - " + mili.getName() + ": " + mili.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Running");

        new MainWhatsApp();
    }

    public Message getRandomMessage(List<Message> list) {
        int index = (int) (Math.random() * list.size());
        return list.get(index);
    }

    private class Message {
        private String date = "";
        private String time = "";
        private String name = "";
        private String message = "";

        public Message(String date, String time, String name, String message) {
            this.date = date;
            this.time = time;
            this.name = name;
            this.message = message;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
