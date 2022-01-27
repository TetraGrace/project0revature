import org.apache.spark.sql.execution.SQLExecution
import java.sql.DriverManager
import java.sql.Connection

object authenticate {
    def stringBuilder(buf: Array[Char]):String = {
        
        var result = ""
        buf.foreach(x => result += x.toString())
        return result;
    }

    def authUser(user:String, pass:String): Boolean = {
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
            
            case e:Throwable => {println("Failed tp connect to the database. Program is quiting"); e.printStackTrace}
        }
        
        val username2 = "\"" + user + "\""
        val passString = "\"" + pass+ "\""
        val querys = s"SELECT * FROM playerdata WHERE username = $username2 AND password = $passString"
        val action = connection.createStatement()
        action.execute(querys)
        val result = action.getResultSet()
        var back = ""
        while(result.next()){
            back = result.getString("id")
        }
        //didnt find a match
        println(back)
        if (back!= null) {
        return true;
        }
        return false        
  }
}
