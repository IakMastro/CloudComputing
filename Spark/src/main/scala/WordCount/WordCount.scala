package WordCount

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("WordCount").setMaster("local")

    val sc = new SparkContext(conf)

    val input = sc.textFile("file://" + System.getProperty("user.dir") + "/data/simple_text.txt")

    val wordcount = input.flatMap(text => text.split(" "))
      .map(word => (word.toLowerCase, 1))
      .reduceByKey(_ + _)
      .foreach(println)

    sc.stop()
  }
}
