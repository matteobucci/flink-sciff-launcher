package matteo.operations;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Scanner;

public class KeyedSciffOperation extends RichFlatMapFunction<Tuple2<String, String>, Tuple2<String, String>> {

    private transient ValueState<PrintWriter> writer;
    private transient ValueState<Scanner> reader;
    private transient ValueState<Scanner> error;
    private transient ValueState<String> result;

    private String[] cmd;
    private boolean o;

    public KeyedSciffOperation(String[] cmd, boolean output){
        this.cmd = cmd;
        this.o = output;
    }

    @Override
    public void open(Configuration config) {
        ValueStateDescriptor<Scanner> readerDescriptor =
                new ValueStateDescriptor<>(
                        "reader", // the state name
                        TypeInformation.of(new TypeHint<Scanner>() {})); // default value of the state, if nothing was set

        ValueStateDescriptor<Scanner> errorDescriptor =
                new ValueStateDescriptor<>(
                        "error", // the state name
                        TypeInformation.of(new TypeHint<Scanner>() {})); // default value of the state, if nothing was set

        ValueStateDescriptor<PrintWriter> writerDescriptor =
                new ValueStateDescriptor<>(
                        "writer", // the state name
                        TypeInformation.of(new TypeHint<PrintWriter>() {})); // default value of the state, if nothing was set

        ValueStateDescriptor<String> resultDescriptor =
                new ValueStateDescriptor<>(
                        "result", // the state name
                        TypeInformation.of(new TypeHint<String>() {})); // default value of the state, if nothing was set



        reader = getRuntimeContext().getState(readerDescriptor);
        writer = getRuntimeContext().getState(writerDescriptor);
        error = getRuntimeContext().getState(errorDescriptor);
        result = getRuntimeContext().getState(resultDescriptor);

    }

    @Override
    public void flatMap(Tuple2<String, String> input, Collector<Tuple2<String, String>> out) throws Exception {

        PrintWriter currentWriter = null;
        Scanner currentReader = null;
        Scanner currentError = null;
        String currentResult = null;

        if(writer != null && reader != null){
            currentReader = reader.value();
            currentWriter = writer.value();
            currentError = error.value();
            currentResult = result.value();
        }

        if (currentWriter == null && currentReader == null) {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            if(o) System.out.println("Nuovo processo creato con chiave" + input.f0);
            currentReader = new Scanner(p.getInputStream());
            currentError = new Scanner(p.getErrorStream());
            currentWriter = new PrintWriter(new BufferedWriter(new PrintWriter(p.getOutputStream())));
            currentReader.nextLine();
            writer.update(currentWriter);
            reader.update(currentReader);
            error.update(currentError);
        }

        if (currentWriter != null && currentReader != null) { //TODO: Cosa succede se l'elaborazione è già terminata?
            if(currentResult != null && (currentResult.equals("Yes") || currentResult.equals("No")) ){
                if(o) System.out.println("Evento per elaborazione già terminata");
                currentError.close();
                currentReader.close();
                currentWriter.close();
                writer.update(currentWriter);
                reader.update(currentReader);
                error.update(currentError);
            }else{
                currentWriter.println(input.f1 + ".");
                currentWriter.flush();
                if(o) System.out.println("Input -> " + input.f1);
                currentResult = currentReader.nextLine();
                if(o) System.out.println("Output -> " + currentResult);
                if (currentResult.equals("Yes") || currentResult.equals("No")) {
                    out.collect(new Tuple2<>(input.f0, currentResult));
                    currentError.close();
                    currentReader.close();
                    currentWriter.close();
                    writer.update(currentWriter);
                    reader.update(currentReader);
                    error.update(currentError);
                }

                result.update(currentResult);
            }

        }else{
            throw new IllegalStateException("Le socket non sono state create con successo");
        }

    }
}
