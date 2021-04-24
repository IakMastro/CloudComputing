package TFI;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;

public class TFIReducer extends MapReduceBase
        implements Reducer<Text, DoubleWritable, Text, DoubleWritable> {
    @Override
    public void reduce(Text text,
                       Iterator<DoubleWritable> iterator,
                       OutputCollector<Text, DoubleWritable> outputCollector,
                       Reporter reporter) throws IOException {
        var sum = 0.0f;

        while (iterator.hasNext())
            sum += iterator.next().get();

        // Get the words counted in Mapper for the word in question.
        outputCollector.collect(text, new DoubleWritable(sum / TFIMapper.wordsCounted.get(
                text.toString().split(" ")[1])));
    }
}
