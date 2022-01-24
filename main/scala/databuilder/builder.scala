package jdbc

import java.io.File
import java.io.PrintWriter
import scala.io.Source
import org.apache.spark.sql.execution.SQLExecution
import java.sql.DriverManager
import java.sql.Connection
import scala.util.Random
import scala.io.Source
import scala.collection.mutable.ArrayBuffer
//PLEASE NOTE
//This shoudl only be runt o make some data up for the databases.
//some of this data is randomly generated by a python script i wrote
object builder {
  
  def main(args: Array[String]):Unit = {
    val driver = "com.mysql.cj.jdbc.Driver"
    val url = "jdbc:mysql://localhost/project0"
    val user= "root"
    val password = "pass1"
    //note this is a password for a test DB that i have. You should NEVER use a password this insecure.
    //you should also never log in using root, and use a different user profile.
    var connection:Connection = null

    //try and make a connection
    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(url,user,password)
      println("Connected to Database")
    } catch {
      case e:Throwable => e.printStackTrace
    }

    val monstersPath = "I:\\try4\\hello-world\\src\\main\\scala\\databuilder\\monsters.csv"
    val rawNameDataPath = "I:\\try4\\hello-world\\src\\main\\scala\\databuilder\\raw-name-data.csv"
      //this file and function is designed to just feed data into the data base from a csv file
      //it makes usernamename and generates a password from a number I make
      //player names taken from https://raw.githubusercontent.com/hadley/data-baby-names/master/baby-names.csv 
      //monster names are generated randomly at: https://www.fantasynamegenerators.com/monster-names.php
      //first things first, open the baby name file and start parsing that data!
      //mosnters
      var buffer = new ArrayBuffer[String]()
      val source = Source.fromFile(monstersPath)
      for (line <- source.getLines()){
        buffer += line;
      }
      source.close()
      //now everything is in a buffer, we need to commit those to the db
      println(buffer.length.toString() + " Items to input into the db")
      for(i <-0 until 200){
        //try to send a request to add items into the database
        try {
          var temp = buffer(i) //to remove the newliong character
          val cleanSplit = temp.split(",")
          println(cleanSplit.length)
          var tempName = "\"" + cleanSplit(0) + "\""
          var tempCR = "\""+cleanSplit(1)+ "\""
          var tempHealth = cleanSplit(2).toString
          var tempAttack = cleanSplit(3).toString
          var tempDefense = cleanSplit(4).toString
          val statement = connection.createStatement()
          val query = s"INSERT INTO monsters(monsterName, monsterHealth, monsterAttack, monsterDefense,cr) VALUES ($tempName,$tempHealth,$tempAttack,$tempDefense,$tempCR)"
          statement.execute(query)
        } catch {
          case e: Throwable => e.printStackTrace()
        }
      }
      println("Items entered into monsters table") 

   //Monsters are done, moving onto names
   var buffer2 = new ArrayBuffer[String]()
   val source2 = Source.fromFile(rawNameDataPath)
   for(line <- source2.getLines()){
      buffer2 += line
   }
   source2.close()
   //got all those names in, time to remove the garbage from them
   for(i<-0 until buffer2.length){
     var temp = buffer2(i).split(",")
     buffer2(i) = temp(1)
   }
   //start putting it into the data bases, player info and dungeon info. all dungeon information 
   for(i<-0 until buffer2.length){
     val statement1 = connection.createStatement()
     val name = buffer2(i)
     val password = "\"password\""
     val emptyJSON = "NULL"
     val query1 = s"INSERT INTO playerData VALUES ($i,$name,$password);"
     val query2 = s"INSERT INTO dungeondata VALUES ($i, $emptyJSON , 0)"
     statement1.execute(query1)
     statement1.execute(query2)
   }
   println("Entered Items into the playerdat aand dungeondata databases")
   println("Closing connection")
   connection.close()
   
  }
}
