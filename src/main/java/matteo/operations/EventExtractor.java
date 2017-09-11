package matteo.operations;

import matteo.utils.SubmodelVariables;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

public class EventExtractor implements MapFunction<String, Tuple2<String, String>> {

    @Override
    public Tuple2<String, String> map(String s) throws Exception {
        return new Tuple2<>(getId(s), getH(s));
    }

    private String getId(String event) {
        return event.split("t\\(")[1].split(",")[0];
    }

    private String getH(String event){
        if(event.contains(SubmodelVariables.lastEvent)){
            return SubmodelVariables.lastEvent;
        }else{
            return removeLastChar("h(" + event.split("h\\(")[1]);
        }
    }

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

}
