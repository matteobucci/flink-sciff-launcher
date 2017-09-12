package matteo;


import matteo.operations.EventExtractor;
import matteo.operations.EventSplitter;
import matteo.operations.KeyedSciffOperation;
import matteo.operations.NotEmptyFilter;
import matteo.utils.SubmodelVariables;
import org.apache.commons.cli.*;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.File;

public class StreamingJob {



	public static void main(String[] args) throws Exception {

	    int port = 0;
	    String host = null;
	    String path = null;
        String[] sciffCmd;

		boolean o = false;
		boolean printToConsole = false;

		/* Command line arguments */

		Options options = new Options();

		Option debugOption = new Option("o", "output", false, "enable debug output");
		debugOption.setRequired(false);
		options.addOption(debugOption);

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

		Option outputOption = new Option("c", "console", false, "output to console");
		outputOption.setRequired(false);
		options.addOption(outputOption);

		Option fileOption = new Option("f", "file", true, "output to file");
		fileOption.setRequired(false);
		options.addOption(fileOption);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {

			cmd = parser.parse(options, args);

			//Check debug mode
            if(cmd.hasOption(debugOption.getOpt())){
                o = true;
				System.out.println("Debug mode enabled");
			}

			//Check output to console
            if(cmd.hasOption(outputOption.getOpt())){
				printToConsole = true;
				System.out.println("Print to console forced");
			}

			//Check port and host
            port = Integer.parseInt(cmd.getOptionValue(portOption.getOpt()));
            host = cmd.getOptionValue(hostOption.getOpt());

            if(port < 0 || port > 65535){
                System.err.println("Port number must be between 0 and 65535");
                formatter.printHelp("SciffStream", options);
                System.exit(1);
                return;
            }

            //Check output to file
            if(cmd.hasOption(fileOption.getOpt())){
				path = cmd.getOptionValue(fileOption.getOpt());
				File file = new File(path);
				if(file.isDirectory()){
					System.err.println("File cannot be a directory");
					System.exit(1);
					return;
				}
				System.out.println("Results are going to be in " + file.getPath());
			}

			//Check Sciff dirs
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
		env.getConfig().setLatencyTrackingInterval(1000);

        DataStream<String> data = env.socketTextStream(host, port);

		/* Stream flow */

		SingleOutputStreamOperator<Tuple2<String, Integer>> output = data
				.filter(new NotEmptyFilter())															//Remove empty lines
				.flatMap(new EventSplitter())															//Extract single elements
				.map(new EventExtractor())																//Map key and event
				.keyBy(0)																		//Group by key
				.flatMap(new KeyedSciffOperation(sciffCmd, o))											//Run process
				.map(new MapFunction<Tuple2<String,String>, Tuple2<String, Integer>>() {				//Extract results

					@Override
					public Tuple2<String, Integer> map(Tuple2<String, String> stringStringTuple2) throws Exception {
						return new Tuple2<>(stringStringTuple2.f1, 1);
					}
				})
				.keyBy(0)																		//Group by results
				.sum(1);																	//Count results

		/* Output logic */
		if(path != null){
			output.writeAsText(path); 	//Write to file
		}

		if(path == null || printToConsole){
			output.print();				//Write to console if file is not specified or if printToConsole is forced (args c)
		}

		/* Execute program */
		env.execute("Sciff launcher");
	}

}
