import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Schedule implements Closeable {
    private final int QUANTUM = 10;
    private FileWriter output;

    public static void main(String[] args){
        try (Schedule schedule = new Schedule(openFileAndCreateTaskList(args[1]))) {
            schedule.runFunction(args[0]);
            System.out.println("Done! Check output.txt for the result");
        } catch (IOException e) {
            System.out.println("Unable to access files. Exiting...");;
        } catch (Exception e) {
            System.out.println("Unknown error has occurred. Exiting...");
        }
    }

    private static ArrayList<Task> openFileAndCreateTaskList(String fileName) throws Exception {
        ArrayList<Task> taskList = new ArrayList<>();
        System.out.println("Reading " + fileName);
        File file = new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] arr = line.replace(", ", ",").replace(" ,", ",").split(",");
            // Skip incorrect lines
            if (arr.length < 4)
                continue;

            taskList.add(new Task(arr[0], Integer.parseInt(arr[1]), Integer.parseInt(arr[2]), Integer.parseInt(arr[3])));
        }
        return taskList;
    }


    Schedule(ArrayList<Task> taskList) throws IOException {
        this.taskList = taskList;
        this.tasksNum = taskList.size();
        output = new FileWriter("output.txt");
    }

    public void runFunction(String function) {
        switch (function) {
            case "fcfs":
                firstComeFirstServed();
                break;
            case "sjf":
                shortestJobFirst();
                break;
            case "pri":
                priorityScheduling();
                break;
            case "rr":
                roundRobinScheduling(Comparator.comparingInt(Task::getArrivalTime));
                break;
            case "pri-rr":
                roundRobinScheduling(Comparator.comparingInt(Task::getPriority).reversed());
                break;
        }
    }

    ArrayList<Task> taskList;
    int time = 0;
    final int tasksNum;
    int totalTurnAround;
    int totalWaitTime;

    private void println() {
        println("");
    }

    private void println(String message) {
        try {
            output.write(message + "\n");
        } catch (Exception e) {
            System.out.println("ERROR: Couldn't write to file but program will continue.");
        }
    }

    private void printEnding() {
        println("---Clock: " + time + "---");
        println("Program done\n");
        println(String.format("Average turnaround time: %.1f", (float) totalTurnAround / tasksNum));
        println(String.format("Average wait time time: %.1f", (float) totalWaitTime / tasksNum));
    }

    private void doTask(Task task) {
        doTask(task, task.getCpuBurst());
    }

    private void doTask(Task task, int timeToAdd) {
        println("---Clock: " + time + "---");
        println("Will run task: " + task.getTaskName());
        println("Priority: " + task.getPriority());
        println("Burst: " + task.getCpuBurst());

        // Wait time = start clock - arrival time
        int waitTime = Math.max(0, time - task.getArrivalTime());
        totalWaitTime += waitTime;
        // Turnaround time = wait time + cpu burst
        int turnAroundTime = waitTime + timeToAdd;
        totalTurnAround += turnAroundTime;
        time += timeToAdd;
        println("Turnaround Time: " + turnAroundTime);
        println("Waiting Time: " + waitTime);
        if (task.getCpuBurst() - timeToAdd == 0)
            println("Task " + task.getTaskName() + " finished\n");
        else println();

    }

    /**
     * Gets tasks arrived within the time variable
     * If no tasks were found, adds enough time to reach arrival of earliest task
     * And then returns arrived tasks
     * Also removes the tasks from the main list
     *
     * @return list of tasks that are within the time limit
     */
    private ArrayList<Task> getArrivedTasksOrIncreaseTime() {
        ArrayList<Task> arrivedTasks = getArrivedTasks();
        if (taskList.isEmpty())
            return arrivedTasks;
        if (arrivedTasks.isEmpty()) {
            int newTime = getTaskWithMinArrivalTime().getArrivalTime();
            println("---Clock: " + time + "---\n---Idle time: " + (newTime - time) + "---\n");
            time = newTime;
            return getArrivedTasksOrIncreaseTime();
        }
        return arrivedTasks;
    }

    private ArrayList<Task> getArrivedTasks() {
        ArrayList<Task> arrivedTasks = new ArrayList<>();
        if (taskList.isEmpty())
            return arrivedTasks;
        if (time == 0) {
            time = getTaskWithMinArrivalTime().getArrivalTime();
            if (time > 0)
                println("---Clock: 0---\n---Idle time: " + (time) + "---\n");
        }
        for (Task task : taskList)
            if (task.getArrivalTime() <= time)
                arrivedTasks.add(task);
        taskList.removeAll(arrivedTasks);
        return arrivedTasks;
    }

    private Task getTaskWithMinArrivalTime() {
        Task task = taskList.get(0);
        for (int i = 1; i < taskList.size(); i++)
            if (taskList.get(i).getArrivalTime() < task.getArrivalTime())
                task = taskList.get(i);
        return task;
    }

    private void firstComeFirstServed() {
        ArrayList<Task> arrivedTasks = getArrivedTasksOrIncreaseTime();
        while (!arrivedTasks.isEmpty() || !(arrivedTasks = getArrivedTasksOrIncreaseTime()).isEmpty()) {
            // Find task with min arrival time or if same arrival time, the one with higher priority
            Task minTask = arrivedTasks.get(0);
            for (int i = 1; i < arrivedTasks.size(); i++) {
                Task task = arrivedTasks.get(i);
                if (task.getArrivalTime() < minTask.getArrivalTime())
                    minTask = task;
            }
            arrivedTasks.remove(minTask);
            doTask(minTask);
        }
        printEnding();
    }

    private void shortestJobFirst() {
        ArrayList<Task> arrivedTasks = getArrivedTasksOrIncreaseTime();
        while (!arrivedTasks.isEmpty() || !(arrivedTasks = getArrivedTasksOrIncreaseTime()).isEmpty()) {
            // Find task with min burst time or min arrival time if 2 tasks have same burst time
            Task minTask = arrivedTasks.get(0);
            for (int i = 1; i < arrivedTasks.size(); i++) {
                Task task = arrivedTasks.get(i);
                if (task.getCpuBurst() < minTask.getCpuBurst())
                    minTask = task;
                else if (task.getCpuBurst() == minTask.getCpuBurst() && task.getArrivalTime() < minTask.getArrivalTime())
                    minTask = task;
            }
            arrivedTasks.remove(minTask);

            doTask(minTask);
        }
        printEnding();
    }

    private void priorityScheduling() {
        ArrayList<Task> arrivedTasks = getArrivedTasksOrIncreaseTime();
        while (!arrivedTasks.isEmpty() || !(arrivedTasks = getArrivedTasksOrIncreaseTime()).isEmpty()) {
            // Find task with min burst time or min arrival time if 2 tasks have same burst time
            Task maxTask = arrivedTasks.get(0);
            for (int i = 1; i < arrivedTasks.size(); i++) {
                Task task = arrivedTasks.get(i);
                if (task.getPriority() > maxTask.getPriority())
                    maxTask = task;
            }
            arrivedTasks.remove(maxTask);

            doTask(maxTask);
        }
        printEnding();
    }

    private void roundRobinScheduling(Comparator<Task> comparator) {
        ArrayList<Task> arrivedTasks = getArrivedTasks();
        arrivedTasks.sort(comparator);
        while (!arrivedTasks.isEmpty()) {
            // Find task with min burst time or min arrival time if 2 tasks have same burst time
            for (int i = 0; i < arrivedTasks.size(); i++) {
                Task task = arrivedTasks.get(i);
                int burstTime = Math.min(task.getCpuBurst(), QUANTUM);
                doTask(task, burstTime);
                task.setCpuBurst(task.getCpuBurst() - burstTime);
                if (task.getCpuBurst() == 0) {
                    arrivedTasks.remove(task);
                    i--;
                }
                ArrayList<Task> newArrival = getArrivedTasks();
                newArrival.sort(comparator);
                arrivedTasks.addAll(newArrival);
            }
        }
        printEnding();
    }


    @Override
    public void close() throws IOException {
        output.flush();
        output.close();
    }
}
