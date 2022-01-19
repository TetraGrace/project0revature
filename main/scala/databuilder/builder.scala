package jbdc

import java.io.File
import java.io.PrintWriter
import scala.io.Source
import org.apache.spark.sql.execution.SQLExecution
import java.sql.DriverManager
import java.sql.Connection
import scala.util.Random
import scala.io.Source
import scala.collection.mutable.ArrayBuffer

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
    } catch {
      case e:Throwable => e.printStackTrace
    }

    println("Got a connection!")
    connection.close()

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
      // for(i <- 0 until buffer.length){
      //   //try to send a request to add items into the database
      //   try {
      //     var temp = buffer(i).substring(0,buffer(i).length-1) //to remove the newliong character
      //     var tempName = temp(0)
      //     var tempCR = temp(1)
      //     var tempHealth = temp(2).toString
      //     var tempAttack = temp(3).toString
      //     var tempDefense = temp(4).toString
      //     val statement = connection.createStatement()
      //     val query = s"INSERT INTO monsters(monsterName, monsterHealth, monsterAttack, monsterDefense,cr) VALUES ($tempName,$tempCR,$tempHealth,$tempAttack,$tempDefense)"
      //     statement.execute(query)
      //   } catch {
      //     case e: Throwable => e.printStackTrace()
      //   }
      // }
      // println("Items entered") 

   //Monsters are done, moving onto names
   var buffer2 = new ArrayBuffer[String]()
   val source2 = Source.fromFile(rawNameDataPath)
   for(line <- source2.getLines()){
      buffer2 += line
   }
   source.close()
   //got all those names in, time to remove the garbage from them
   for(i<-0 until buffer2.length){
     var temp = buffer2(i).split(",")
     buffer2(i) = temp(1)
   }
   //start putting it into the data bases, player info and dungeon info. all dungeon information 
   for(i<-0 until buffer2.length){
     val statement1 = connection.createStatement()
     val name = buffer2(i)
     val query1 = s"INSERT INTO playerData VALUES ($i,$name,password)"
     
   }
  }
}
