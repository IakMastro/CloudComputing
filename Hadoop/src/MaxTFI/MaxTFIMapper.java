package MaxTFI;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

public class MaxTFIMapper extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, DoubleWritable> {
    @Override
    public void map(LongWritable longWritable,
                    Text text,
                    OutputCollector<Text, DoubleWritable> outputCollector,
                    Reporter reporter) throws IOException {
        var line = text.toString().split("\t");
        outputCollector.collect(new Text(line[0].split(" ")[0]),
                new DoubleWritable(Double.parseDouble(line[1])));
    }
}
