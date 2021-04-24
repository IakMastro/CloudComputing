package MaxTFI;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;

public class MaxTFIReducer extends MapReduceBase
        implements Reducer<Text, DoubleWritable, Text, DoubleWritable> {
    @Override
    public void reduce(Text text,
                       Iterator<DoubleWritable> iterator,
                       OutputCollector<Text, DoubleWritable> outputCollector,
                       Reporter reporter) throws IOException {
        var max = Double.MIN_VALUE;

        while (iterator.hasNext()) {
            var tfi = iterator.next().get();

            if (tfi > max)
                max = tfi;
        }

        outputCollector.collect(text, new DoubleWritable(max));
    }
}
