package TFIDF

import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf

object TFIDF {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("TFIDF").setMaster("local")

    val spark = SparkSession.builder.config(conf).getOrCreate()

    val sc = spark.sparkContext

    val input = sc.wholeTextFiles("file://" + System.getProperty("user.dir") + "/data/*")
      .map(file => (file._1.split('/').last, file._2))

    val inputDF = spark.createDataFrame(input).toDF("fileNames", "fileText")

    // TF
    val tokenizer = new Tokenizer().setInputCol("fileText").setOutputCol("words")
    val wordsData = tokenizer.transform(inputDF)
    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures")
    val featurizedData = hashingTF.transform(wordsData)

    // IDF
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)

    // TFIDF
    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.select("fileNames", "features").show()

    sc.stop()
  }
}
