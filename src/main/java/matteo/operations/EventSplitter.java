package matteo.operations;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

public final class EventSplitter implements FlatMapFunction<String, String> {

    @Override
    public void flatMap(String value, Collector<String> out) {
        // normalize and split the line
        String[] tokens = value.split("\\.");

        // emit the pairs
        for (String token : tokens) {
            if (token.length() > 0) {
                out.collect(token);
            }
        }
    }
}
