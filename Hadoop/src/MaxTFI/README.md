# MaxTFI

Το MaxTFI είναι μία προέκταση του TFI προγράμματος. Σκοπός του είναι να βρίσκει το μέγιστο TFI ένα όρου μέσα στα έγγραφα. Για παράδειγμα, άμα η λέξη "vision" έχει TFI 0.5 στο έγγραφο 1 και 0.6 στο έγγραφο 2, θα πρέπει να κρατάει το TFI του εγγράφου 2.

## Mapper

Ο Mapper του MaxTFI είναι αρκετά ενδιαφέρον επειδή σχεδιάστηκε με το σκεπτικό ότι θα χρησιμοποιήσει το έγγραφο εξόδου του TFI ως το έγγραφο εισόδου.

```java
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
```

Ουσιαστικά, σπάει τη γραμμή στα δύο και κρατάει τον όρο και τη συχνότητα TFI του. Δεν υπάρχει ενδιαφέρον στο να κρατηθεί η πληροφορία για το ποιο έγγραφο άνηκε κάποτε ο όρος.

## Reducer

Στην αρχή του reducer έχουμε αυτή την τιμή:

```java
var max = Double.MIN_VALUE;
```

Όπου παίρνει αυτόματα την ελάχιστη τιμή που μπορεί να πάρει ένας αριθμός διπλής ακρίβειας.
Ο υπόλοιπος reducer είναι αρκετά απλός. Κρατάει και καταγράφει μόνο τη μέγιστη συχνότητα TFI που έχει ο όρος σε οποιεσδήποτε έγγραφο.

```java
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
```

## Ενδεικτικά τρεξίματα

Για τα ενδεικτικά τρεξίματα του MaxTFI, θα χρησιμοποιηθούν τα αποτελέσματα του TFI. Για να υπάρχει σημείο σύγκρισης με τα αποτελέσματα του MaxTFI, σημειώνονται από κάτω.

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

Να σημειωθεί ότι στον όρο And στο τραγούδι heroes έχει συχνότητα TFI 0.02, ενώ στο supper's ready έχει 0.01, όποτε για να είναι ορθό η υλοποίηση του MaxTFI, θα πρέπει να κρατάει το 0.02 ως τιμή της συχνότητας.

```text
'n'	0.010695187374949455
A	0.0021030493080615997
All	0.0010515246540307999
Along	0.002673796843737364
And	0.02631578966975212
As	0.0021030493080615997
Bacon	0.0010515246540307999
Bang,	0.0010515246540307999
Better	0.0010515246540307999
British	0.0010515246540307999
But	0.003759398590773344
Call	0.010695187374949455
Can't	0.0021030493080615997
Catching	0.002673796843737364
Cause	0.003759398590773344
Churchill	0.0010515246540307999
Coming	0.0010515246540307999
...
```

Όντως, το And έχει συχνότητα 0.02, όποτε ο αλγόριθμος έχει υλοποιηθεί σωστά.
