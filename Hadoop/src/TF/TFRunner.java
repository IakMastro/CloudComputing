package TF;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

public class TFRunner {
    public static void main(String[] args) {
        var conf = new JobConf(TFRunner.class);
        conf.setJobName("tf");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(TFMapper.class);
        conf.setCombinerClass(TFReducer.class);
        conf.setReducerClass(TFReducer.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

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
