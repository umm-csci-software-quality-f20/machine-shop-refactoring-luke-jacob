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
    private Machine[] machines; // array of machines
    private int finishTime; // all machines finish before this

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
            int index = getMachineForNextTask(theJob);
            theJob.putJobOnMachineQueue(this, index);
            theJob.setArrivalTime(timeNow);
            if (eList.nextEventTime(index) == finishTime) {
                machines[index].changeState(index, eList, timeNow);
            }
            return true;
        }
    }

    private int getMachineForNextTask(Job theJob) {
        int machineNum = ((Job) theJob.getTaskQ().getFrontElement()).getMachine();
        return machineNum;
    }

    /**
     * change the state of theMachine
     * 
     * @return last job run on this machine
     */

    private void setMachineChangeOverTimes(SimulationSpecification specification) {
        for (int index = 1; index<=specification.getNumMachines(); ++index) {
            machines[index].setChangeTime(specification.getChangeOverTimes(index));
        }
    }

    /**
     * sets up the jobs for the task queue
     * @param specification
     */
    private void setUpJobs(SimulationSpecification specification) {
        Job theJob;
        for (int currentJob = 1; currentJob <= specification.getNumJobs(); currentJob++) {
            int tasks = Job.getNumTasks(specification, currentJob);
            int firstMachine = 0; // machine for first task

            theJob = new Job(currentJob);
            for (int currentTask = 1; currentTask <= tasks; currentTask++) {
                int theMachine = getMachineNumber(specification, currentJob, currentTask);
                int theTaskTime = getTaskTime(specification, currentJob, currentTask);
                if (currentTask == 1)
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
        eList = new EventList(specification.getNumMachines(), finishTime);
        machines = new Machine[specification.getNumMachines() + 1];
        for (int currentMachine = 1; currentMachine <= specification.getNumMachines(); currentMachine++)
            machines[currentMachine] = new Machine();
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

        for (int theMachine = 1; theMachine <= numMachines; theMachine++)
            machines[theMachine].changeState(theMachine, eList, timeNow);
    }

    /**
     * process all jobs to completion
     * @param simulationResults
     */
    void simulate(SimulationResults simulationResults) {
        while (numJobs > 0) {
            int nextToFinish = eList.nextEventMachine();
            timeNow = eList.nextEventTime(nextToFinish);
            Job theJob = machines[nextToFinish].changeState(nextToFinish, eList, timeNow);
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
        Machine.setTotalAndNumTasksPerMachine(simulationResults, new int[numMachines + 1], machines);
    }

    /**
     * starts the machine shop then runs the simulation and returns the results
     * @param specification
     * @return the results of running the simulation
     */
    public SimulationResults runSimulation(SimulationSpecification specification) {
        finishTime = Integer.MAX_VALUE;
        timeNow = 0;
        startShop(specification);
        SimulationResults simulationResults = new SimulationResults(numJobs);
        simulate(simulationResults);
        outputStatistics(simulationResults);
        return simulationResults;
    }

    /** Getter method for the array of machines that is used in MSS */
    public Machine[] getMachineArray() {
        return machines;
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
