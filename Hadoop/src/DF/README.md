# DF

Ο αλγόριθμος DF είναι ένας αλγόριθμος της Ανάκτησης Πληροφορίας ο οποίος μετράει σε πόσα έγγραφα εμφανίζεται η κάθε λέξη. Όπως και στο παράδειγμα του WCount, έτσι και εδώ το πρόγραμμα του MapReducer χωρίζεται στα τρία. Ο Mapper είναι ίδιος με τον Mapper του TF.

## Reducer

Ο Reducer παρόλο που θυμίζει τον Reducer του WCount, έχουν μία μικρή διαφορά που αλλάζει ριζικά το πως δουλεύει.

```java
public class DFReducer extends MapReduceBase
        implements Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    public void reduce(Text text,
                       Iterator<IntWritable> iterator,
                       OutputCollector<Text, IntWritable> outputCollector,
                       Reporter reporter) throws IOException {
        var count = 0;

        while (iterator.hasNext()) {
            count++; // Εδώ είναι η διαφορά
            iterator.next();
        }

        outputCollector.collect(text, new IntWritable(count));
    }
}
```

Ουσιαστικά στον runner, το combiner class επειδή θα τρέξει δεύτερη φόρα, θα πάρει τα δεδομένα από το reducer class και επιστρέψει στο τελικό αρχείο το σωστό αποτέλεσμα.

## Ενδεικτικά τρεξίματα

Για τα ενδεικτικά τρεξίματα, χρησιμοποιήθηκαν οι στίχοι τριών τραγουδιών, συγκεκριμένα το Roundabout των Yes, το Supper's Ready των Genesis και το Heroes του θρυλικού David Bowie.

```text
'n'	1
A	1
All	1
Along	1
And	2
As	1
Bacon	1
Bang,	1
...
```

Αυτά είναι τα οχτώ πρώτα αποτελέσματα που επέστρεψε το MapReduce. Βλέπουμε ότι όλες οι λέξεις εμφανίζονται σε ένα τραγούδι έκτος από το And που εμφανίζεται σε δύο.