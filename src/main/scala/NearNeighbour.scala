import java.util

import scala.io.Source
import scala.collection.JavaConversions._
import java.sql.{DriverManager, Connection}

import scala.util.control.Breaks

/**
 * Created by eshan on 11/29/15.
 */
object NearNeighbour {

  def main(args:Array[String]) : Unit = {

    var dataList = new java.util.ArrayList[DataPoint]()
    var resultList = new java.util.ArrayList[ResultPoint]()
    var resultTreeMap = new util.TreeMap[Double,ResultPoint]()

    //mysql
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://localhost/295b"
    val username = "root"
    val password = "admin"
    var connection: Connection = null

    for (line <- Source.fromFile("/home/ubuntu/targetWrite.txt").getLines) {
      val lineData = line.toString.split(",")
      val temp = new java.util.ArrayList[Double]
      temp.add(lineData(0).toDouble)
      temp.add(lineData(1).toDouble)
      temp.add(lineData(2).toDouble)
      temp.add(lineData(3).toDouble)
      temp.add(lineData(4).toDouble)
      val dp = new DataPoint(temp, lineData(5).toDouble)
      //val dp = new DataPoint(lineData(0).toDouble,lineData(1).toDouble,lineData(2).toDouble,lineData(3).toDouble,lineData(4).toDouble,lineData(5).toDouble)
      dataList.add(dp)
    }
    //println("length:".concat(dataList.length.toString))
    //val dpQuery = new DataPoint(0.0,lineData(0).toDouble,lineData(1).toDouble,lineData(2).toDouble,lineData(3).toDouble,lineData(4).toDouble,lineData(5).toDouble)
    //7,0,5,164,164



    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      // create the statement, and run the select query


      val statement = connection.createStatement()
      val statement1 = connection.createStatement()
      val statement2 = connection.createStatement()

      var query = "select distinct id from predictiontable"
      val rs = statement.executeQuery(query)


      while(rs.next)
        {
          val id = rs.getString("id")
          println(id)
          query = "select * from streamingdata where id = '"+id+"' and timestamp = (select max(timestamp) from streamingdata where id='"+id+"')"
          val resultSet = statement1.executeQuery(query)
          val dpQuery = new java.util.ArrayList[Double]
          while(resultSet.next) {
            dpQuery.add(resultSet.getString("filesystem").toInt)
            dpQuery.add(resultSet.getString("zombieprocess").toInt)
            dpQuery.add(resultSet.getString("inode").toInt)
            dpQuery.add(resultSet.getString("freememory").toInt)
            dpQuery.add(resultSet.getString("loadvalue").toInt)
          }
          //calculate eucledian distance
          for(x <- dataList) {
            // println("in datalist for loop")
            var dist = 0.0
            for (j <- 0 to x.attributes.length - 1) {
              dist += Math.pow(x.attributes.get(j).toInt - dpQuery.get(j).toInt, 2)
            }
            val distance = Math.sqrt(dist)
            val tempRP = new ResultPoint(x.attributes.get(0).toDouble, x.attributes.get(1).toDouble, x.attributes.get(2).toDouble, x.attributes.get(3).toDouble, x.attributes.get(4).toDouble, x.cluster.toDouble)
            //resultList.add(tempRP)
            //Deduplicate and sort
            resultTreeMap.put(distance, tempRP)
          }
          val tempArray = resultTreeMap.toArray
          val resultTreeMapList = tempArray.toList.sortWith(_._1>_._1)

            //val updateQuery = "update predictiontable set filesystem = "+resultTreeMapList.get(0)._2.one+",zombieprocess="+resultTreeMapList.get(0)._2.two+",inode="+resultTreeMapList.get(0)._2.three+",freememory="+resultTreeMapList.get(0)._2.four+",loadvalue="+resultTreeMapList.get(0)._2.five+" where id = '"+id+"' and timetype=10"
            //val t = statement2.executeUpdate(updateQuery)






            //val updateQuery2 = "update predictiontable set filesystem = "+resultTreeMapList.get(5)._2.one+",zombieprocess="+resultTreeMapList.get(5)._2.two+",inode="+resultTreeMapList.get(5)._2.three+",freememory="+resultTreeMapList.get(5)._2.four+",loadvalue="+resultTreeMapList.get(5)._2.five+" where id = '"+id+"' and timetype=30"
            //val s = statement2.executeUpdate(updateQuery2)


          val updateQuery3 = "update predictiontable set filesystem = "+resultTreeMapList.get(15)._2.one+",zombieprocess="+resultTreeMapList.get(15)._2.two+",inode="+resultTreeMapList.get(15)._2.three+",freememory="+resultTreeMapList.get(15)._2.four+",loadvalue="+resultTreeMapList.get(15)._2.five+" where id = '"+id+"' and timetype=180"
          val r = statement2.executeUpdate(updateQuery3)






        }


    } catch {
      case e => e.printStackTrace

    }

    connection.close()

    System.exit(0)


  }


}

class DataPoint ( var attributes : java.util.ArrayList[Double], var cluster : Double){}
class ResultPoint ( val one : Double, val two : Double, val three : Double, val four : Double, val five : Double, val cluster : Double ){}

