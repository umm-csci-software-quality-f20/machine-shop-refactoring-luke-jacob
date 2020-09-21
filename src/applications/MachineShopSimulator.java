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
        if (theJob.getTaskQ().isEmpty()) {
            simulationResults.setJobCompletionData(theJob.getId(), timeNow, timeNow - theJob.getLength());
            return false;
        }
        else {
            int p = ((Task) theJob.getTaskQ().getFrontElement()).getMachine();
            machine[p].getJobQ().put(theJob);
            theJob.setArrivalTime(timeNow);
            if (eList.nextEventTime(p) == largeTime) {
                machine[p].changeState(p, eList, timeNow);
            }
            return true;
        }
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
            int tasks = specification.getJobSpecifications(i).getNumTasks();
            int firstMachine = 0;

            theJob = new Job(i);
            for (int j = 1; j <= tasks; j++) {
                int theMachine = specification.getJobSpecifications(i).getSpecificationsForTasks()[2*(j-1)+1];
                int theTaskTime = specification.getJobSpecifications(i).getSpecificationsForTasks()[2*(j-1)+2];
                if (j == 1)
                    firstMachine = theMachine;
                theJob.addTask(theMachine, theTaskTime);
            }
            machine[firstMachine].getJobQ().put(theJob);
        }
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
        machine[0].setTotalAndNumTasksPerMachine(simulationResults, new int[numMachines + 1], machine);
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

    /** entry point for machine shop simulator */
    public static void main(String[] args) {
        final SpecificationReader specificationReader = new SpecificationReader();
        SimulationSpecification specification = specificationReader.readSpecification();
        MachineShopSimulator simulator = new MachineShopSimulator();
        SimulationResults simulationResults = simulator.runSimulation(specification);
        simulationResults.print();
    }
}
