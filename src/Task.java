public class Task {
    private String taskName;
    private int arrivalTime, priority, cpuBurst;

    public Task(String taskName, int arrivalTime, int priority, int cpuBurst) {
        this.taskName = taskName;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.cpuBurst = cpuBurst;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCpuBurst() {
        return cpuBurst;
    }

    public void setCpuBurst(int cpuBurst) {
        this.cpuBurst = cpuBurst;
    }

}
