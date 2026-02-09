import java.io.*;
import java.util.*;

// Interface for grade calculation
interface Calculable {
    double calculateAverage();
}

// Base class Person
class Person {
    protected String id;
    protected String name;

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void displayInfo() {
        System.out.println("ID: " + id + ", Name: " + name);
    }
}

// Student class inherits Person and implements Calculable
class Student extends Person implements Calculable {
    private Map<String, Integer> grades; // subject -> marks

    public Student(String id, String name) {
        super(id, name);
        grades = new HashMap<>();
    }

    // Add grade (method overloading: with or without subject)
    public void addGrade(String subject, int marks) throws InvalidGradeException {
        if (marks < 0 || marks > 100) {
            throw new InvalidGradeException("Marks should be between 0 and 100!");
        }
        grades.put(subject, marks);
    }

    public void addGrade(String subject, int marks, boolean notifyTeacher) throws InvalidGradeException {
        addGrade(subject, marks);
        if (notifyTeacher) {
            new NotificationThread("Grade added for " + name + " in " + subject).start();
        }
    }

    // Display all grades
    public void displayGrades() {
        System.out.println("\nGrades for " + name + ":");
        if (grades.isEmpty()) {
            System.out.println("No grades recorded.");
            return;
        }
        for (Map.Entry<String, Integer> entry : grades.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Average: " + calculateAverage());
    }

    // Implement calculateAverage()
    @Override
    public double calculateAverage() {
        if (grades.isEmpty()) return 0;
        double sum = 0;
        for (int marks : grades.values()) {
            sum += marks;
        }
        return sum / grades.size();
    }

    // Save student grades to file
    public void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("students.txt", true))) {
            for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                bw.write(id + "," + name + "," + entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving student grades: " + e.getMessage());
        }
    }

    // Load student grades from file (static method)
    public static List<Student> loadFromFile() {
        List<Student> students = new ArrayList<>();
        Map<String, Student> studentMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("students.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String id = parts[0];
                    String name = parts[1];
                    String subject = parts[2];
                    int marks = Integer.parseInt(parts[3]);

                    Student student;
                    if (studentMap.containsKey(id)) {
                        student = studentMap.get(id);
                    } else {
                        student = new Student(id, name);
                        studentMap.put(id, student);
                    }
                    student.grades.put(subject, marks);
                }
            }
            students.addAll(studentMap.values());
        } catch (IOException e) {
            System.out.println("No saved student data found. Starting fresh.");
        }
        return students;
    }
}

// Custom Exception for invalid grades
class InvalidGradeException extends Exception {
    public InvalidGradeException(String message) {
        super(message);
    }
}

// Optional: Notification Thread
class NotificationThread extends Thread {
    private String message;
    public NotificationThread(String message) { this.message = message; }

    @Override
    public void run() {
        System.out.println("Notification: " + message);
    }
}

// Main class
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Student> students = Student.loadFromFile();

        while (true) {
            System.out.println("\n=== Student Grade Management System ===");
            System.out.println("1. Add Student Grade");
            System.out.println("2. Display All Students");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter Student ID: ");
                    String id = sc.nextLine();
                    System.out.print("Enter Student Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Subject: ");
                    String subject = sc.nextLine();
                    System.out.print("Enter Marks: ");
                    int marks = sc.nextInt();
                    sc.nextLine();

                    Student student = null;
                    for (Student s : students) {
                        if (s.id.equals(id)) {
                            student = s;
                            break;
                        }
                    }
                    if (student == null) {
                        student = new Student(id, name);
                        students.add(student);
                    }

                    try {
                        student.addGrade(subject, marks, true);
                        student.saveToFile();
                    } catch (InvalidGradeException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 2 -> {
                    if (students.isEmpty()) {
                        System.out.println("No students recorded yet.");
                    } else {
                        for (Student s : students) {
                            s.displayInfo();
                            s.displayGrades();
                        }
                    }
                }
                case 3 -> {
                    System.out.println("Exiting...");
                    sc.close();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
}
k