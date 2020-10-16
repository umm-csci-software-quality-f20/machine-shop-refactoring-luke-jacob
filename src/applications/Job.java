package applications;

import dataStructures.LinkedQueue;

class Job {
    // data members
    private LinkedQueue taskQ; // this job's tasks
    private int length; // sum of scheduled task times
    private int arrivalTime; // arrival time at current queue
    private int id; // job identifier

    // constructor
    Job(int theId) {
        id = theId;
        taskQ = new LinkedQueue();
        // length and arrivalTime have default value 0
    }

    // other methods
    public void addTask(int theMachine, int theTime) {
        getTaskQ().put(new Task(theMachine, theTime));
    }

    /**
     * remove next task of job and return its time also update length
     */
    public int removeNextTask() {
        int theTime = ((Task) getTaskQ().remove()).getTime();
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

	void putJobOnMachineQueue(MachineShopSimulator machineShopSimulator, int p) {
        Machine machine = machineShopSimulator.getMachine(p);
        machine.getJobQ().put(this);
	}

	/**
	 * move theJob to machine for its next task
	 * @param machineShopSimulator TODO
	 * @param simulationResults TODO
	 * @return false if no next task, true if has next task.
	 */
	boolean moveToNextMachine(MachineShopSimulator machineShopSimulator, SimulationResults simulationResults) {
	    if (getTaskQ().isEmpty()) {// the job has no next task; return false
	        simulationResults.setJobCompletionData(getId(), machineShopSimulator.timeNow, machineShopSimulator.timeNow - getLength());
	        return false;
	    } else {// theJob has a next task
	        int p = machineShopSimulator.getMachineForNextTask(this);
	        putJobOnMachineQueue(machineShopSimulator, p);
	        setArrivalTime(machineShopSimulator.timeNow);
	        if (machineShopSimulator.eList.nextEventTime(p) == machineShopSimulator.largeTime) {
	            machineShopSimulator.machine[p].changeState(p, machineShopSimulator.eList, machineShopSimulator.timeNow);
	        }
	        return true;
	    }
	}

}
