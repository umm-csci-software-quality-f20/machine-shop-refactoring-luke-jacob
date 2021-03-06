package applications;

import dataStructures.LinkedQueue;

class Machine {
    // data members
    private LinkedQueue jobQ; // queue of waiting jobs for this machine
    private int changeTime; // machine change-over time
    private int totalWait; // total delay at this machine
    private int numTasks; // number of tasks processed on this machine
    private Job activeJob; // job currently active on this machine

    // constructor
    Machine() {
        jobQ = new LinkedQueue();
    }

    public LinkedQueue getJobQ() {
        return jobQ;
    }

    public int getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(int changeTime) {
        this.changeTime = changeTime;
    }

    public int getTotalWait() {
        return totalWait;
    }

    public void setTotalWait(int totalWait) {
        this.totalWait = totalWait;
    }

    public int getNumTasks() {
        return numTasks;
    }

    public void setNumTasks(int numTasks) {
        this.numTasks = numTasks;
    }

    public Job getActiveJob() {
        return activeJob;
    }

    public void setActiveJob(Job activeJob) {
        this.activeJob = activeJob;
    }

    /**
     * change the state of theMachine
     * @return last job run on this machine
     */
    public Job changeState(int num, EventList eList, int now) {
        Job lastJob;
        if (getActiveJob() == null) {
            lastJob = null;
            if (getJobQ().isEmpty())
                eList.setFinishTime(num, Integer.MAX_VALUE);
            else {
                setActiveJob((Job) getJobQ().remove());
                setTotalWait(getTotalWait() + now
                        - getActiveJob().getArrivalTime());
                setNumTasks(getNumTasks() + 1);
                int t = getActiveJob().removeNextTask();
                eList.setFinishTime(num,  now + t);
            }
        }
        else {
            lastJob = getActiveJob();
            setActiveJob(null);
            eList.setFinishTime(num, now
                    + getChangeTime());
        }
        return lastJob;
    }

    //Sets the total wait time and number of tasks for the simulation results
    public static void setTotalAndNumTasksPerMachine(SimulationResults simulationResults, int[] total, Machine[] m) {
        int[] numTask = new int[total.length];
        for (int i=1; i<=total.length-1; ++i) {
            total[i] = m[i].getTotalWait();
            numTask[i] = m[i].getNumTasks();
        }
        simulationResults.setTotalWaitTimePerMachine(total);
        simulationResults.setNumTasksPerMachine(numTask);
    }
}
