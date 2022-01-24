import scala.io.Source
import scala.io.StdIn.readLine
import org.apache.spark.sql.execution.SQLExecution
import java.sql.DriverManager
import java.sql.Connection

class DBUtils {
    


    def runQuery(query:String):java.sql.ResultSet = {
        val driver = "com.mysql.cj.jdbc.Driver"
        val url = "jdbc:mysql://localhost/project0"
        val user= "root"
        val password = "pass1"
    //note this is a password for a test DB that i have. You should NEVER use a password this insecure.
    //you should also never log in using root, and use a different user profile.
        var connection:Connection = null
        try {
            Class.forName(driver)
            connection = DriverManager.getConnection(url,user,password)
            println("Connected to Database")
        } catch {
            case e:Throwable => e.printStackTrace
        }
        val statement = connection.createStatement()        
        val result = statement.executeQuery(query)
        connection.close()
        return result
        
    }
}


object dungeons {
    
}

object authenticate {
    def stringBuilder(buf: Array[Char]):String = {
        var result = ""
        buf.foreach(x => result += x.toString())
        return result;
    }

    def authUser(user:String, pass:Array[Char]): Boolean = {
        // TODO: do this
        return true;
  }
}

object mainInput {
    val temp = "SELECT * FROM monsters WHERE cr = \"easy\" ORDER BY RAND() LIMIT 1";
    //I guess the first thing we need to start with is having th main loop. 
    //the loop will wait for input from the user then calldo its stuff.
    def main(args: Array[String]):Unit = {
    println("Welcome to Dungeons Offline! TO log in, type \"login\". To make and account, type \"new\". To quit, type \"quit\"")
    //i dont like using while true loops but i will fix it later
    var res = "";
        do { 
            val input = readLine()
            input.toLowerCase() match {
                case "quit" => System.exit(0)
                case "login" => res = "login"
                case "new" => println("Account creation in not availible at this time") //TODO: account creation
                case _ => println("Please enter an acceptable input.")
            }
        }
        while(res == "");
        if(res == "login"){
            val stdIn = System.console();
            val maxTries = 3;
            var currentTries = 0;
            //this varible is used to determine if we need to quick the login try loop
            var breaker = true;
            while(currentTries < maxTries && breaker){
                val user = readLine("Username:")
                val pass = stdIn.readPassword("Password")
                if(!authenticate.authUser(user,pass)){
                    currentTries+=1;
                    println("Incorrect Username or Password, please try again. Remainging tries: " + (maxTries-currentTries).toString())
                } else {
                    breaker = false;
                }
            }
            if(currentTries == maxTries) {
                println("You have run out of password attempts.")
                System.exit(0);
            }


        } else {
            //TODO: new account creation
        }
        //If the program has run to to this point, everythign was authorized correctly, probably. Now we just need to play the game
        val queryObj = new DBUtils()
        println(queryObj.runQuery(temp))
    }
}
