# TFI

Το TFI είναι ένας αλγόριθμος της ανάκτησης πληροφορίας, όπου υπολογίζει τη συχνότητα εμφάνισης ενός όρου μέσα στο έγγραφο.

$$
tf("this", d_1) = \frac{1}{5}
$$

Αυτό σημαίνει ότι μέσα στο έγγραφο d1, το "this" εμφανίζεται μια φόρα στις 5 λέξεις και έχει συχνότητα 0.2.

## Mapper

Στην κλάση TFIMapper, αντιθέτως με τις προηγούμενες Mapper κλάσεις που αναλυθήκαν, είναι αρκετά διαφορετική. Καταρχάς, αυτή τη φόρα αντιστοιχεί Text με DoubleWritable, για τον λόγο ότι γίνεται διαίρεση στον Reducer.
Για να υπολογίζει το tfi θα πρέπει κάπως να κρατάει πόσες λέξεις διάβασε συνολικά ανά αρχείο. Αυτό γίνεται με τον εξής τρόπο:

```java
public static HashMap<String, Integer> wordsCounted = new HashMap<>();
```

Δημιουργείται, δηλαδή, μία HashMap η οποία αντιστοιχεί το όνομα του αρχείου με έναν αριθμό. Αυτό γίνεται σε αυτές τις γραμμές κωδικά.

```java
int count = wordsCounted.getOrDefault(fileName, 0);
wordsCounted.put(fileName, count + 1);
```

Αρχικά, με την εντολή getOrDefault(fileName, 0) ουσιαστικά κοιτάει το key του fileName, το οποίο είναι το αρχείο εισόδου που το παίρνει από αυτή τη γραμμή.

```java
var fileName = ((FileSplit)reporter.getInputSplit()).getPath().getName();
```

Εάν, το key δεν έχει βρεθεί, επειδή είναι η πρώτη φορά που περνάει το αρχείο λογικά, τότε παίρνει την τιμή 0, διαφορετικά παίρνει το value του key στο map. Τέλος, φορτώνει το key στο Map και του αντιστοιχεί την τιμή count + 1. Αυτό, θεωρητικά θα του δώσει την τελική τιμή για το πόσες λέξεις έχει το έγγραφο μέσα του. Επιπλέον, αφού είναι public, σημαίνει ότι μπορεί να το δει και το Reducer αργότερα, με σκοπό να γίνουν σωστά οι υπολογισμοί.
Εφόσον, αναλυθήκαν τα νέα δύσκολα τμήματα του κωδικά, από κάτω ακολουθείται ολόκληρος ο κώδικας.

```java
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
```

## Reducer

Ο TFIReducer είναι παρόμοιος με το TFReducer. Υπάρχει μία διαφορά όμως.

```java
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
                text.toString().split(" ")[1]))); // Εδώ είναι η διαφορά
    }
}
```

Επειδή, δεν είναι ακριβώς ξεκάθαρο το πως δουλεύει, ας αναλυθεί τι κάνει η συγκεκριμένη εντολή:

```java
TFIMapper.wordsCounted.get(text.toString().split(" ")[1]);
```

Όπως αναλύθηκε στο Mapper, το wordsCounted.get επιστρέφει την αντιστοιχημένη τιμή του key πίσω για επιπλέον πληροφορία. Επειδή, στο text, το Mapper προσθέτει και το όνομα του αρχείου στην αρχή, με το text.toString().split(" ")[1], αφαιρείται το όνομα του αρχείου, αφού χωρίζει το text αναλόγως του κενού και παίρνει το δεύτερο μέρος του που βρίσκεται η λέξη.

## Ενδεικτικά τρεξίματα

Αρχικά, για να επιβεβαιωθεί ότι δουλεύει σωστά ο αλγόριθμος, θα χρησιμοποιηθεί το αρχείο που είχε χρησιμοποιηθεί στο TF.

```text
hi bye hi three three three
```

Ο θεωρητικός υπολογισμός τους είναι ο εξής:

$$
tf("hi", d) = \frac{2}{6} \simeq 0.33
$$
$$
tf("bye", d) = \frac{1}{6} \simeq 0.16
$$
$$
tf("three", d) = \frac{3}{6} \simeq 0.5
$$

Αφού εκτελεσθεί το αρχείο, για να θεωρηθεί σωστό, θα πρέπει να έχει αυτές τις τιμές.
Στο αρχείο εξόδου, επιστρέφει τις εξής τιμές:

```text
bye simple_text.txt	0.1666666716337204
hi simple_text.txt	0.3333333432674408
three simple_text.txt	0.5
```

Οπότε, μπορεί να σημειωθεί ότι ο αλγόριθμος είναι ορθός.

### Ενδεικτικό τρέξιμο σε μεγάλη κλίμακα δεδομένων

Μεγάλο ενδιαφέρον του αντικείμενου της ανάλυσης δεδομένων είναι να μπορεί ο αλγόριθμός να χρησιμοποιηθεί σε μία μεγάλου όγκου δεδομένων και όχι σε εκείνο το μικρό, ο οποίος αναλύθηκε μόνο για την επιβεβαίωση ορθότητας του αλγορίθμου.
Για άλλη μια φόρα, θα αναλυθούν τα ίδια τρία τραγούδια που αναλύθηκαν και στο DF. Το αρχείο στην έξοδο επέστρεψε τα εξής δεδομένα:

```text
'n' roundabout.txt	0.010695187374949455
A suppers_ready.txt	0.0021030493080615997
All suppers_ready.txt	0.0010515246540307999
Along roundabout.txt	0.002673796843737364
And heroes.txt	0.02631578966975212
And suppers_ready.txt	0.015772869810461998
As suppers_ready.txt	0.0021030493080615997
Bacon suppers_ready.txt	0.0010515246540307999
Bang, suppers_ready.txt	0.0010515246540307999
Better suppers_ready.txt	0.0010515246540307999
British suppers_ready.txt	0.0010515246540307999
But heroes.txt	0.003759398590773344
But suppers_ready.txt	0.0010515246540307999
Call roundabout.txt	0.010695187374949455
...
```

Παρότι οι πληροφορίες που επέστρεψε είναι ορθές, όμως θα μπορούσε να παρατηρήσει κανείς, επιστρέφονται σημεία στίξης, κεφαλαία κτλ που ίσως δε θα έπρεπε να υπήρχαν. Για να λυθεί αυτό το πρόβλημα στον πραγματικό κόσμο, θα πρέπει να καθαριστούν αυτά (με τη χρήση των κατάλληλων βιβλιοθηκών). Έτσι με αποτέλεσμα θα έχουμε μία πιο πραγματική προσέγγιση στην πραγματική τιμή της συχνότητας της λέξεως. Βεβαία, για τη συγκεκριμένη εργασία δεν υπάρχει λόγος να αναλυθεί πολύ, απλώς καλό είναι να αναφέρονται και να σημειώνονται αυτά.
