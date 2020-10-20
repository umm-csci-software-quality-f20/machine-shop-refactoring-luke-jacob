package applications;

import dataStructures.LinkedQueue;

class Job {
    // data members
    private LinkedQueue taskMachineQ; // this job's machines
    private LinkedQueue taskTimeQ; // this job's times
    private int length; // sum of scheduled task times
    private int arrivalTime; // arrival time at current queue
    private int id; // job identifier
    private int machine; // the machine doing the task
    private int time; //the time to do the task

    // constructor
    Job(int theId) {
        id = theId;
        taskMachineQ = new LinkedQueue();
        taskTimeQ = new LinkedQueue();
    }

    // other methods
    public void addTask(int theMachine, int theTime) {
        taskMachineQ.put(theMachine);
        taskTimeQ.put(theTime);
    }

    /**
     * remove next task of job and return its time also update length
     */
    public int removeNextTask() {
        int theTime = (int)taskTimeQ.remove();
        taskMachineQ.remove();
        length = getLength() + theTime;
        return theTime;
    }

    public LinkedQueue getTaskMachineQ() { return taskMachineQ; }

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
    /**
     * put the next job on the machine queue.
     * @param machineShopSimulator
     * @param p // the machine to get
     * @return
     */
	void putJobOnMachineQueue(MachineShopSimulator machineShopSimulator, int p) {
        Machine machine = machineShopSimulator.getMachine(p);
        machine.getJobQ().put(this);
    }

	/**
	 * move theJob to machine for its next task
	 * @param machineShopSimulator
	 * @param simulationResults
     * @param eList
	 * @return false if no next task, true if has next task.
	 */
	boolean moveToNextMachine(MachineShopSimulator machineShopSimulator, SimulationResults simulationResults, EventList eList) {
	    if (taskMachineQ.isEmpty()) {// the job has no next task; return false
            simulationResults.setJobCompletionData(getId(), machineShopSimulator.getTimeNow(),
             machineShopSimulator.getTimeNow() - getLength());
	        return false;
	    } else {// theJob has a next task
	        int p = machineShopSimulator.getMachineForNextTask(this);
	        putJobOnMachineQueue(machineShopSimulator, p);
	        setArrivalTime(machineShopSimulator.getTimeNow());
	        if (eList.nextEventTime(p) == Integer.MAX_VALUE) {
	            machineShopSimulator.getMachine(p).changeState(p, eList, machineShopSimulator.getTimeNow());
	        }
	        return true;
	    }
	}

    public static int getNumTasks(SimulationSpecification specification, int i) {
        return specification.getJobSpecifications(i).getNumTasks();
    }
}
