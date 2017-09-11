package matteo.operations;

import org.apache.flink.api.common.functions.FilterFunction;

public class NotEmptyFilter implements FilterFunction<String> {

    @Override
    public boolean filter(String s) throws Exception {
        return !s.isEmpty();
    }

}
