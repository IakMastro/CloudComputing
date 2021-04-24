package TFI;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;


import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class TFIMapper extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, DoubleWritable> {
    private final static DoubleWritable one = new DoubleWritable(1.0f);
    private Text word = new Text();

    // HashMap used to keep track counted words of each file.
    public static HashMap<String, Integer> wordsCounted = new HashMap<>();

    @Override
    public void map(LongWritable longWritable,
                    Text text,
                    OutputCollector<Text, DoubleWritable> outputCollector,
                    Reporter reporter) throws IOException {
        var fileName = ((FileSplit)reporter.getInputSplit()).getPath().getName();
        var line = text.toString();
        var tokenizer = new StringTokenizer(line);

        while (tokenizer.hasMoreTokens()) {
            word.set(tokenizer.nextToken() + " " + fileName );
            outputCollector.collect(word, one);

            // If fileName is not on the map, then get a default value of 0.
            int count = wordsCounted.getOrDefault(fileName, 0);
            wordsCounted.put(fileName, count + 1);
        }
    }
}
