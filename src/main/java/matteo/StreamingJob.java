package matteo;


import matteo.operations.EventExtractor;
import matteo.operations.EventSplitter;
import matteo.operations.KeyedSciffOperation;
import matteo.operations.NotEmptyFilter;
import matteo.utils.SubmodelVariables;
import org.apache.commons.cli.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class StreamingJob {

	static  boolean o = false; //Output of debug informations

	public static void main(String[] args) throws Exception {

	    int port = 0;
	    String host = null;
        String[] sciffCmd;

		/* Command line arguments */

		Options options = new Options();

		Option outputOption = new Option("o", "output", false, "enable debug output");
        outputOption.setRequired(false);
		options.addOption(outputOption);

		Option portOption = new Option("p", "port", true, "port");
        portOption.setRequired(true);
        options.addOption(portOption);

        Option hostOption = new Option("h", "host", true, "host");
        hostOption.setRequired(true);
        options.addOption(hostOption);

        Option swiplOption = new Option("s", "swipl", true, "swipl path");
        swiplOption.setRequired(true);
        options.addOption(swiplOption);

        Option dirOption = new Option("d", "dir", true, "working directory");
        dirOption.setRequired(true);
        options.addOption(dirOption);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);

            if(cmd.hasOption(outputOption.getOpt())){
                o = true;
            }

            port = Integer.parseInt(cmd.getOptionValue(portOption.getOpt()));
            host = cmd.getOptionValue(hostOption.getOpt());

            if(port < 0 || port > 65535){
                System.err.println("Port number must be between 0 and 65535");
                formatter.printHelp("SciffStream", options);
                System.exit(1);
                return;
            }

            String path_to_swipl = cmd.getOptionValue(swiplOption.getOpt());
            String path_to_AlpBPM = cmd.getOptionValue(dirOption.getOpt());

            sciffCmd = new String[]{
                    path_to_swipl+"/swipl",
                    "-g",
                    "working_directory(_,'"+ path_to_AlpBPM +"'),spark_trace_verification("+ SubmodelVariables.model+ ","
                            +SubmodelVariables.observability+","+SubmodelVariables.durationConstr+","+SubmodelVariables.interTaskConstr+","+SubmodelVariables.options+")",
                    "-t",
                    "halt",
                    path_to_AlpBPM+"/AlpBPM.pl"
            };


		} catch (ParseException e) {
			System.err.println(e.getMessage());
			formatter.printHelp("SciffStream", options);
			System.exit(1);
			return;
		} catch (NumberFormatException e) {
            System.err.println("Port number must be an integer");
            formatter.printHelp("SciffStream", options);
            System.exit(1);
            return;
        }

        if(o) System.out.println("Program started in debug mode");

		/* Setup stream enviroment */

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> data = env.socketTextStream(host, port);

		/* Stream flow */

		data.filter(new NotEmptyFilter())
				.flatMap(new EventSplitter())
				.map(new EventExtractor())
				.keyBy(0)
				.flatMap(new KeyedSciffOperation(sciffCmd, o))
				.map(new MapFunction<Tuple2<String,String>, Tuple2<String, Integer>>() {

					@Override
					public Tuple2<String, Integer> map(Tuple2<String, String> stringStringTuple2) throws Exception {
						return new Tuple2<>(stringStringTuple2.f1, 1);
					}
				})
				.keyBy(0)
				.sum(1)
				.print();

		// Execute program
		env.execute("Sciff launcher");
	}


}
