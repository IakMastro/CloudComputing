package TFI;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

public class TFIRunner {
    public static void main(String[] args) {
        var conf = new JobConf(TFIRunner.class);
        conf.setJobName("tfi");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(DoubleWritable.class);

        conf.setMapperClass(TFIMapper.class);
        conf.setReducerClass(TFIReducer.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        conf.setNumReduceTasks(1);

        var paths = new Path[args.length - 1];
        for (int i = 0; i < paths.length; i++)
            paths[i] = new Path(args[i]);

        FileInputFormat.setInputPaths(conf, paths);
        FileOutputFormat.setOutputPath(conf, new Path(args[paths.length]));

        try {
            JobClient.runJob(conf);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Wrong input/output");
        }
    }
}
