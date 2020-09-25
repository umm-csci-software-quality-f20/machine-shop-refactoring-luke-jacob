package applications;

import dataStructures.LinkedQueue;

class Job {
    // data members
    private LinkedQueue taskQ; // this job's tasks
    private int length; // sum of scheduled task times
    private int arrivalTime; // arrival time at current queue
    private int id; // job identifier
    private int machine; // the machine doing the task
    private int time; //the time to do the task

    // constructor
    Job(int theId) {
        id = theId;
        taskQ = new LinkedQueue();
        // length and arrivalTime have default value 0
    }

    Job(int theMachine, int theTime){
        machine = theMachine;
        time = theTime;
    }

    // other methods
    public void addTask(int theMachine, int theTime) {
        getTaskQ().put(new Job(theMachine, theTime));
    }

    /**
     * remove next task of job and return its time also update length
     */
    public int removeNextTask() {
        int theTime = ((Job) getTaskQ().remove()).getTime();
        length = getLength() + theTime;
        return theTime;
    }

    public LinkedQueue getTaskQ() {
        return taskQ;
    }

    public int getLength() {
        return length;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getId() {
        return id;
    }

    public int getMachine() {
        return machine;
    }

    public int getTime() {
        return time;
    }

	void putJobOnMachineQueue(MachineShopSimulator machineShopSimulator, int p) {
        Machine[] machine = machineShopSimulator.getMachineArray();
        machine[p].getJobQ().put(this);
	}

    public static int getNumTasks(SimulationSpecification specification, int i) {
        return specification.getJobSpecifications(i).getNumTasks();
    }
}
