import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

abstract class Task implements Serializable {
    protected String title;
    protected boolean isComplete = false;
    
    public Task(String title) {
        this.title = title;
    }
    
    public void markDone() {
        this.isComplete = true;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public abstract String toString(); 
}

class StudyTask extends Task {
    private int minutes;

    public StudyTask(String title, int minutes) {
        super(title);
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return "Study: " + title + " (" + minutes + " mins)";
    }
}

class DeadlineTask extends Task {
    private String date;

    public DeadlineTask(String title, String date) {
        super(title);
        this.date = date;
    }

    @Override
    public String toString() {
        return "Deadline: " + title + " (Due: " + date + ")";
    }
}

interface IPlannerActions {
    void addTask(Task t);
    void markTaskDone(int index);
    ArrayList<Task> getAllTasks();
    String getProductivityReport();
    void saveData();
    void loadData();
}

class TaskManager implements IPlannerActions {
    private ArrayList<Task> taskList;
    private final String FILE_NAME = "data.bin";

    public TaskManager() {
        taskList = new ArrayList<>();
        loadData();
    }

    @Override
    public void addTask(Task t) {
        taskList.add(t);
        saveData();
    }

    @Override
    public void markTaskDone(int index) {
        if (index >= 0 && index < taskList.size()) {
            taskList.get(index).markDone();
            saveData();
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return taskList;
    }

    @Override
    public String getProductivityReport() {
        if (taskList.isEmpty()) return "PRODUCTIVITY SCORE: 0% (No tasks)";

        int completed = 0;
        for (Task t : taskList) {
            if (t.isComplete()) completed++;
        }

        int percentage = (completed * 100) / taskList.size();
        String comment;
        if (percentage == 100) comment = "Excellent! You crushed it.";
        else if (percentage >= 50) comment = "Good job, keep going!";
        else comment = "You are falling behind!";

        return " PRODUCTIVITY SCORE: " + percentage + "% | " + comment;
    }

    @Override
    public void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(taskList);
        } catch (Exception e) {
            System.out.println("Error saving: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            taskList = (ArrayList<Task>) in.readObject();
        } catch (Exception e) {
            taskList = new ArrayList<>();
        }
    }
}

 class SmartStudyPlanner extends JFrame {

    private IPlannerActions manager = new TaskManager(); 
    
    private JTextField titleInput = new JTextField(10);
    private JTextField detailsInput = new JTextField(8); 
    private JTextField minutesInput = new JTextField(4);
    private JTextField idInput = new JTextField(3);
    private JTextArea displayArea = new JTextArea();

    public SmartStudyPlanner() {
        setTitle("Productivity Tracker");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        add(new JLabel("Title:")); add(titleInput);
        add(new JLabel("Due Date (for Deadlines):")); add(detailsInput);
        add(new JLabel("Mins:")); add(minutesInput);

        JButton addStudyBtn = new JButton("Add Study");
        JButton addDeadlineBtn = new JButton("Add Deadline");
        add(addStudyBtn);
        add(addDeadlineBtn);
        
        add(new JLabel("   |   Task # to Finish:"));
        add(idInput);
        JButton doneBtn = new JButton("Mark Done");
        doneBtn.setBackground(Color.GREEN);
        add(doneBtn);

        displayArea.setPreferredSize(new Dimension(550, 300));
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(displayArea));

        addStudyBtn.addActionListener(e -> {
            try {
                String t = titleInput.getText();
                int m = Integer.parseInt(minutesInput.getText());
                manager.addTask(new StudyTask(t, m));
                refreshUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter valid minutes!");
            }
        });

        addDeadlineBtn.addActionListener(e -> {
            String t = titleInput.getText();
            String d = detailsInput.getText();
            manager.addTask(new DeadlineTask(t, d));
            refreshUI();
        });

        doneBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idInput.getText());
                manager.markTaskDone(id - 1);
                refreshUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid ID!");
            }
        });

        refreshUI();
        setVisible(true);
    }

    private void refreshUI() {
        displayArea.setText(""); 
        displayArea.append("=========================================\n");
        displayArea.append("          MY PRODUCTIVITY DASHBOARD      \n");
        displayArea.append("=========================================\n\n");

        ArrayList<Task> tasks = manager.getAllTasks();
        
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String status = t.isComplete() ? "[DONE]   " : "[PENDING]";
            displayArea.append((i + 1) + ". " + status + " " + t.toString() + "\n");
        }
        
        displayArea.append("\n-----------------------------------------\n");
        displayArea.append(manager.getProductivityReport());
        
        titleInput.setText("");
        detailsInput.setText("");
        minutesInput.setText("");
        idInput.setText("");
    }

    public static void main(String[] args) {
        new SmartStudyPlanner();
    }
}