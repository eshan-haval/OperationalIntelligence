/**
 * Created by eshan on 12/2/15.
 */


import java.io.StringWriter
import java.sql.{DriverManager, Connection}

import java.util.{Date, Properties}

import _root_.kafka.serializer.StringDecoder
import org.apache.commons.io.IOUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.kafka._
import org.apache.spark.{sql, SparkContext, SparkConf}
import org.apache.spark.streaming._
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors
import scala.io.Source


object StreamNCluster {

  def main(args:Array[String]): Unit = {


      val conf = new SparkConf().setAppName("OIStreamCluster")
      val sc = new SparkContext(conf)

      val ssc = new StreamingContext(sc, Seconds(4))

      val sqlContext = new SQLContext(sc)
      val kafkaParams = Map("metadata.broker.list" -> "localhost:9092")
      val topics = Set("logdata")
      val streamKafka = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
      var offsetRanges = Array[OffsetRange]()
      val steamKafkatemp = streamKafka
      //var streamTopicName = ""
      var timestamp = ""
    val data = sc.textFile("file:///home/ubuntu/write.txt")

    streamKafka.foreachRDD { rdd =>

      //offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd.collect().map { x => {

    val test = x.toString()

      val test1 = test.replace("(","")
      val test2 = test1.replace(")","")

      val test3 = test2.substring(5)

      val removeNullNew = test3.replace("|", "@")
      val tempString = new StringBuilder()
      val splitData = removeNullNew.split("@")
      var machineName = splitData(0).toString

      val fileSystem = splitData(1).split(" ")
      val fileSystemvalue = fileSystem(1)
      val temp = fileSystemvalue.replace("%", "")
      tempString.append(temp)
      tempString.append(",")
      val zombieProcess = splitData(2)
      tempString.append(zombieProcess)
      tempString.append(",")
      val iNode = splitData(3).split(" ")
      val iNodevalue = iNode(1).toString.replace("%", "")
      tempString.append(iNodevalue)
      tempString.append(",")
      val freeMemoryvalue = splitData(5)
      tempString.append(freeMemoryvalue)
      tempString.append(",")
      val load = splitData(8).split(",")
      val loadvalue = load(0).toString.split(" ")
      tempString.append(loadvalue(1))

        timestamp = splitData(11).toString

      val cs1: CharSequence = "/"
      val cs2: CharSequence = "%"
      val cs3: CharSequence = "dev"
      val cs4: CharSequence = "null"
      if (!(tempString.contains(cs1) || tempString.contains(cs2) || tempString.contains(cs3) || tempString.contains(cs4))) {

        val numClusters = 12
        val numIterations = 30
        val parsedData = data.map(s => Vectors.dense(s.split(",").map(_.toDouble)))

        val clusters = KMeans.train(parsedData, numClusters, numIterations)
        //println("eshan shriya123"+tempString)

        val dataToPredict = tempString
        //println("eshan pooja"+Vectors.dense(dataToPredict.toString.split(',').map(_.toDouble)))
        val clusterPredict = clusters.predict(Vectors.dense(dataToPredict.toString.split(',').map(_.toDouble)))
        //println("raju:"+clusterPredict)
        val tempStringArray = tempString.toString.split(",")
        try {
        val c = new ScalaJDBC()
          c.insertdata(tempStringArray,timestamp.toString, machineName,clusterPredict)

        }
        catch {
          case e => {
            println(e.getStackTrace)
          }
        }

      }
    } } }


    //Start the streaming
    ssc.start()
    ssc.awaitTermination()
  }

}

class ScalaJDBC {

  def insertdata(z:Array[String], ts:String, tn:String, cluster:Int): Unit =
  {
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://localhost/295b"
    val username = "root"
    val password = "admin"

    var connection: Connection = null
    val dt = new java.util.Date();
    val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    val osdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      // create the statement, and run the select query

      val datets :Date  = sdf.parse(ts);
      val outputts = osdf.format(datets)
      val statement = connection.createStatement()
      val rs = statement.executeQuery("select uptime from clustertable where id = '"+tn.toString+"'")
      var uptimesec : Double = 0.0
      while(rs.next)
        {
          uptimesec = rs.getString("uptime").toInt
        }
      uptimesec = uptimesec + 10
      statement.executeUpdate("insert into streamingdata (id, filesystem, zombieprocess, inode, freememory, loadvalue, timestamp) " + "values ('"+tn.toString+"',"+z(0).toFloat+","+z(1).toFloat+","+z(2).toFloat+","+z(3).toFloat+","+z(4).toFloat+",'"+outputts+"' )")
      statement.executeUpdate("update clustertable set cluster = "+cluster.toInt+", uptime = "+uptimesec+" where id = '"+tn.toString+"'")
    } catch {
      case e => e.printStackTrace
    }
    connection.close()
  }

}

