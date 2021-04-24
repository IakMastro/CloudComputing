package DF;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

public class DFReducer extends MapReduceBase
        implements Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    public void reduce(Text text,
                       Iterator<IntWritable> iterator,
                       OutputCollector<Text, IntWritable> outputCollector,
                       Reporter reporter) throws IOException {
        var count = 0;

        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }

        outputCollector.collect(text, new IntWritable(count));
    }
}
