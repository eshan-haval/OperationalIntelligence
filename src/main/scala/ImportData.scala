/**
  * Created by ehaval on 11/3/15.
  */
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka._
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

object ImportData {

  def main(args:Array[String]) = {
/*    val conf = new SparkConf().setMaster("local[2]").setAppName("OperationalIntelligence").set("spark.executor.memory", "1g").set("spark.rdd.compress", "true").set("spark.storage.memoryFraction", "1")
    val ssc = new StreamingContext(conf, Seconds(4))
    val kafkaParams = Map("zookeeper.connect" -> "localhost:2181", "zookeeper.connection.timeout.ms" -> "1000")
    val inputTopic = "machine01"
    val group = "none"
    val kafkaStream = KafkaUtils.createStream(ssc, kafkaParams, Map(inputTopic -> 1), StorageLevel.MEMORY_ONLY_SER)
    println(kafkaStream)
    */
// initialise spark contexts
val conf = new SparkConf().setAppName("OperationalIntelligence")
    //val sc = new SparkContext(conf)
    //val rdd = sc.textFile("/home/ubuntu/eshan.txt")
    //rdd.coalesce(1).saveAsTextFile("/home/ubuntu/eshan")
    //rdd.coalesce(1,false)
    //rdd.saveAsTextFile("hdfs://localhost:50070/home/ubuntu/")
    // do stuff
    //println("Hello, world!")

    // terminate spark context
    //sc.stop()

    val ssc = new StreamingContext(conf,Seconds(4))
    //val kafkaParams = Map("zookeeper.connect" -> "localhost:2181","zookeeper.connection.timeout.ms" -> "1000")
    val inputTopic = "machine01"
    val group = "none"
    val kafkaStream = KafkaUtils.createStream(ssc,"localhost:2181","spark-streaming-consumer-group", Map(inputTopic -> 1))
    kafkaStream.saveAsTextFiles("/home/ubuntu/shriya")
    ssc.start()
    ssc.awaitTerminationOrTimeout(15000)

    //kafkaStream.saveAsTextFiles("/home/ubuntu/shriya")


  }
}