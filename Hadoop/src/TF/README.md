# WCount (TF)

Ένα κλασσικό παράδειγμα είναι το WCount, το όποιο μετράει πόσες φόρες εμφανίζεται κάθε όρος μέσα στα έγγραφα. Στην ανάκτηση πληροφορίας, αυτό ονομάζεται tf. Το MapReduce είναι χωρισμένο σε τρία τμήματα κώδικα. Στην κλάση του mapper, στην κλάση του reducer και τέλος σε έναν runner. Αυτό το μοντέλο ονομάζεται MapReducer.

## Mapper

Το πρώτο στάδιο του hadoop είναι πάντα το Mapper.

```java
public class TFMapper extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    @Override
    public void map(LongWritable longWritable,
                    Text text,
                    OutputCollector<Text, IntWritable> outputCollector,
                    Reporter reporter) throws IOException {
        var line = text.toString();
        var tokenizer = new StringTokenizer(line);

        while (tokenizer.hasMoreTokens()) {
            word.set(tokenizer.nextToken());
            outputCollector.collect(word, one);
        }
    }
}
```

Το Mapper ουσιαστικά αντιστοιχεί keywords με values, όπως και άλλες υλοποίησης της δομής Map (για παράδειγμα το HashMap της Java, το Dictionary της Python και το JSON της Javascript). Σε αυτή την περίπτωση, αντιστοιχούμενες τιμές είναι ένα Text με ένα IntWritable. Και τα δύο είναι classes του Hadoop.

## Reducer

Το δεύτερο στάδιο του Hadoop είναι το Reducer.

```java
public class TFReducer extends MapReduceBase
        implements Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    public void reduce(Text text,
                       Iterator<IntWritable> iterator,
                       OutputCollector<Text, IntWritable> outputCollector,
                       Reporter reporter) throws IOException {
        var sum = 0;

        while (iterator.hasNext()) {
            sum += iterator.next().get();
        }

        outputCollector.collect(text, new IntWritable(sum));
    }
}
```

To Reducer διαβάζει κάθε key αυτόματα και παίρνει τις τιμές του, κάνει κάποιους υπολογίσιμους και γράφει στο output αρχείο το τελικό αποτέλεσμα. Στο παράδειγμα του wcount, μετράει τις λέξεις που έγιναν map. Δηλαδή εάν η λέξη \<\<up>> εμφανίζεται τρεις φόρες στο κείμενο, θα γράψει στο output \<\<up 3>>.

## Runner

Ο runner δεν είναι ακριβώς στάδιο του Hadoop, άλλα πιο πολύ μία configuration κλάση, που δηλώνει ο προγραμματιστής στο MapReduce ποιες κλάσεις να χρησιμοποιηθούν, ποιος ο τρόπος εγγραφής στο τελικό έγγραφο, κτλ.
Στην αρχή, γίνεται αρχικοποίηση του configuration. Δηλώνεται ότι το configuration βρίσκεται στην κλάση του TFRunner και το όνομα του job είναι tf.

```java
var conf = new JobConf(TFRunner.class);
conf.setJobName("tf");
```

Στη συνέχεια, δηλώνεται ότι τα δεδομένα που θα γραφτούν στο αρχείο εξόδου θα είναι Text και IntWritable. Αυτά, αντιστοιχίζονται με τον Reducer, αφού αυτός είναι υπεύθυνος για την εγγραφή στο αρχείο εξόδου.

```java
conf.setOutputKeyClass(Text.class);
conf.setOutputValueClass(IntWritable.class);
```

Επιπρόσθετα, δηλώνονται ποιες είναι οι κλάσεις του Mapper και του Reducer, όπως και του Combiner. Στην περίπτωση του WCount ενδέχεται να μη χρησιμεύει το Combiner.

```java
conf.setMapperClass(TFMapper.class);
conf.setCombinerClass(TFReducer.class);
conf.setReducerClass(TFReducer.class);
```

Τέλος, δηλώνονται τα formats των αρχείων εισόδου και εξόδου. Και στις δύο περιπτώσεις, είναι Text, εφόσον φορτώνουν απλά εγγράφου (.txt για παράδειγμα, ή από το hdfs://localhost:9870. Περισσότερα στα ενδεικτικά τρεξίματα).

```java
conf.setInputFormat(TextInputFormat.class);
conf.setOutputFormat(TextOutputFormat.class);
```

Εφόσον, το configuration του Runner είναι έτοιμο, το μόνο που λείπει να γίνει είναι να του δωθεί το path των αρχείων είσοδο και το path του αρχείου εξόδου. Ο ολοκληρωμένος κώδικας του Runner είναι ο εξής:

```java
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
```

## Ενδεικτικά τρεξίματα

Για τα ενδεικτικά τρεξίματα, θα χρησιμοποιήσει το εξής απλό αρχείο 6 λέξεων:

```text
hi bye hi three three three
```

Σαφέστατα, στο αρχείο εξόδου, πρέπει να εμφανίζει το hi 2, bye 1 και three 3.
Εκτελώντας το αρχείο, επιστρέφονται τα εξής αποτελέσματα.

```text
bye	1
hi	2
three	3
```

Τα αποτελέσματα του αρχείου είναι σωστά και λογικά.
