import org.apache.commons.cli.*;

public class ArgumentParser {
    private static final int DEFAULT_SKIER_NUM = 10000;
    private static final int DEFAULT_THREADS_NUM = 200;
    private static final int DEFAULT_NUM_LIFT = 40;
    private static final int DEFAULT_MENA_RIDES = 10;
    private int numThreads;
    private int numSkiers;
    private int numPerLift;
    private int meanRides;
    private String serverIp;

    public ArgumentParser() {
    }

    public void parse(String[] args) throws ParseException {
        //options
        Options options = new Options();
        options.addOption("t","maxThreadNum", true, "the maximum number of client threads");
        options.addOption("n","numberOfSkiers", true, "the number of skiers to be lifted, identical to skier id");
        options.addOption("p","numberPerLift", true, "the number of skiers lifted per run, default 40");
        options.addOption("m","liftsPerSkier", true, "the mean number of lifts per skier");
        options.addOption("i","ipAddress", true, "the ip and port of server");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options,args);

        numThreads = DEFAULT_THREADS_NUM;
        numSkiers = DEFAULT_SKIER_NUM;
        numPerLift = DEFAULT_NUM_LIFT;
        meanRides = DEFAULT_MENA_RIDES;
        if(!cmd.hasOption("i"))throw new ParseException("Server IP not found");
        else this.serverIp = cmd.getOptionValue("i");
        if(cmd.hasOption("t"))this.numThreads=Integer.parseInt(cmd.getOptionValue("t"));
        if(cmd.hasOption("n"))this.numSkiers=Integer.parseInt(cmd.getOptionValue("n"));
        if(cmd.hasOption("p"))this.numPerLift=Integer.parseInt(cmd.getOptionValue("p"));
        if(cmd.hasOption("m"))this.meanRides=Integer.parseInt(cmd.getOptionValue("m"));
    }
    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getNumSkiers() {
        return numSkiers;
    }

    public void setNumSkiers(int numSkiers) {
        this.numSkiers = numSkiers;
    }

    public int getNumPerLift() {
        return numPerLift;
    }

    public void setNumPerLift(int numPerLift) {
        this.numPerLift = numPerLift;
    }

    public int getMeanRides() {
        return meanRides;
    }

    public void setMeanRides(int meanRides) {
        this.meanRides = meanRides;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
