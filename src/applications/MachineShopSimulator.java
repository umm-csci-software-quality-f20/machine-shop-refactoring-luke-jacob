package applications;

public class MachineShopSimulator {

    // error message strings
    public static final String NUMBER_OF_MACHINES_MUST_BE_AT_LEAST_1 = "number of machines must be >= 1";
    public static final String NUMBER_OF_MACHINES_AND_JOBS_MUST_BE_AT_LEAST_1 = "number of machines and jobs must be >= 1";
    public static final String CHANGE_OVER_TIME_MUST_BE_AT_LEAST_0 = "change-over time must be >= 0";
    public static final String EACH_JOB_MUST_HAVE_AT_LEAST_1_TASK = "each job must have >= 1 task";
    public static final String BAD_MACHINE_NUMBER_OR_TASK_TIME = "bad machine number or task time";

    // data members of MachineShopSimulator
    private int timeNow; // current time
    private int numMachines; // number of machines
    private int numJobs; // number of jobs
    private EventList eList; // pointer to event list
    private Machine[] machine; // array of machines
    private int largeTime; // all machines finish before this

    // methods
    /**
     * move theJob to machine for its next task
     * @return false if no next task
     */
    boolean moveToNextMachine(Job theJob, SimulationResults simulationResults) {
        if (theJob.getTaskQ().isEmpty()) {// the job has no next task; return false
            simulationResults.setJobCompletionData(theJob.getId(), timeNow, timeNow - theJob.getLength());
            return false;
        } else {// theJob has a next task
            int p = getMachineForNextTask(theJob);
            theJob.putJobOnMachineQueue(this, p);
            theJob.setArrivalTime(timeNow);
            if (eList.nextEventTime(p) == largeTime) {
                machine[p].changeState(p, eList, timeNow);
            }
            return true;
        }
    }

    private int getMachineForNextTask(Job theJob) {
        int p = ((Task) theJob.getTaskQ().getFrontElement()).getMachine();
        return p;
    }

    /**
     * change the state of theMachine
     * 
     * @return last job run on this machine
     */
    Job changeState(int theMachine) {// Task on theMachine has finished,
                                            // schedule next one.
        Job lastJob;
        if (machine[theMachine].getActiveJob() == null) {// in idle or change-over
                                                    // state
            lastJob = null;
            // wait over, ready for new job
            if (machine[theMachine].getJobQ().isEmpty()) // no waiting job
                eList.setFinishTime(theMachine, largeTime);
            else {// take job off the queue and work on it
                machine[theMachine].setActiveJob((Job) machine[theMachine].getJobQ()
                        .remove());
                machine[theMachine].setTotalWait(machine[theMachine].getTotalWait() + timeNow
                        - machine[theMachine].getActiveJob().getArrivalTime());
                machine[theMachine].setNumTasks(machine[theMachine].getNumTasks() + 1);
                int t = machine[theMachine].getActiveJob().removeNextTask();
                eList.setFinishTime(theMachine, timeNow + t);
            }
        } else {// task has just finished on machine[theMachine]
                // schedule change-over time
            lastJob = machine[theMachine].getActiveJob();
            machine[theMachine].setActiveJob(null);
            eList.setFinishTime(theMachine, timeNow
                    + machine[theMachine].getChangeTime());
        }

        return lastJob;
    }

    private void setMachineChangeOverTimes(SimulationSpecification specification) {
        for (int i = 1; i<=specification.getNumMachines(); ++i) {
            machine[i].setChangeTime(specification.getChangeOverTimes(i));
        }
    }

    /**
     * sets up the jobs for the task queue
     * @param specification
     */
    private void setUpJobs(SimulationSpecification specification) {
        Job theJob;
        for (int i = 1; i <= specification.getNumJobs(); i++) {
            int tasks = Task.getNumTasks(specification, i);
            int firstMachine = 0; // machine for first task

            theJob = new Job(i);
            for (int j = 1; j <= tasks; j++) {
                int theMachine = getMachineNumber(specification, i, j);
                int theTaskTime = getTaskTime(specification, i, j);
                if (j == 1)
                    firstMachine = theMachine; // job's first machine
                theJob.addTask(theMachine, theTaskTime); // add to
            } // task queue
            theJob.putJobOnMachineQueue(this, firstMachine);
        }
    }

    private int getTaskTime(SimulationSpecification specification, int i, int j) {
        return specification.getJobSpecifications(i).getSpecificationsForTasks()[2*(j-1)+2];
    }

    private int getMachineNumber(SimulationSpecification specification, int i, int j) {
        return specification.getJobSpecifications(i).getSpecificationsForTasks()[2*(j-1)+1];
    }

    private void createEventAndMachineQueues(SimulationSpecification specification) {
        eList = new EventList(specification.getNumMachines(), largeTime);
        machine = new Machine[specification.getNumMachines() + 1];
        for (int i = 1; i <= specification.getNumMachines(); i++)
            machine[i] = new Machine();
    }

    /**
     * load first jobs onto each machine
     * @param specification
     */
    void startShop(SimulationSpecification specification) {
        numMachines = specification.getNumMachines();
        numJobs = specification.getNumJobs();
        createEventAndMachineQueues(specification);
        setMachineChangeOverTimes(specification);
        setUpJobs(specification);

        for (int p = 1; p <= numMachines; p++)
            machine[p].changeState(p, eList, timeNow);
    }

    /**
     * process all jobs to completion
     * @param simulationResults
     */
    void simulate(SimulationResults simulationResults) {
        while (numJobs > 0) {
            int nextToFinish = eList.nextEventMachine();
            timeNow = eList.nextEventTime(nextToFinish);
            Job theJob = machine[nextToFinish].changeState(nextToFinish, eList, timeNow);
            if (theJob != null && !moveToNextMachine(theJob, simulationResults))
                numJobs--;
        }
    }

    /**
     * output wait times at machines
     * @param simulationResults
     */
    void outputStatistics(SimulationResults simulationResults) {
        simulationResults.setFinishTime(timeNow);
        simulationResults.setNumMachines(numMachines);
        Machine.setTotalAndNumTasksPerMachine(simulationResults, new int[numMachines + 1], machine);
    }

    /**
     * starts the machine shop then runs the simulation and returns the results
     * @param specification
     * @return the results of running the simulation
     */
    public  SimulationResults runSimulation(SimulationSpecification specification) {
        largeTime = Integer.MAX_VALUE;
        timeNow = 0;
        startShop(specification);
        SimulationResults simulationResults = new SimulationResults(numJobs);
        simulate(simulationResults);
        outputStatistics(simulationResults);
        return simulationResults;
    }

    /** Getter method for the array of machines that is used in MSS */
    public Machine[] getMachineArray() {
        return machine;
    }
    /** Getter method for returning a single machine from the machine array.
     *  This is working to protect encapsulation of the array.
     */
    public Machine getMachine(int p) {
        return machine[p];
    }
    /** entry point for machine shop simulator */
    public static void main(String[] args) {
        final SpecificationReader specificationReader = new SpecificationReader();
        SimulationSpecification specification = specificationReader.readSpecification();
        MachineShopSimulator simulator = new MachineShopSimulator();
        SimulationResults simulationResults = simulator.runSimulation(specification);
        simulationResults.print();
    }
}
