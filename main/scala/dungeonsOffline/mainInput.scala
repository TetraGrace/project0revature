import scala.io.Source
import scala.io.StdIn.readLine
import org.apache.spark.sql.execution.SQLExecution
import java.sql.DriverManager
import java.sql.Connection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import authenticate._
 
class Monster(rawid:Int,rawname:String,rawhealth:Int,rawattack:Int, rawdefense:Int, diff:String){
    //the Monster Class, for all your Monster Needs.
    val id = rawid //This is a VAL cause the id of the monster should never change.
    var health = rawhealth
    var attack = rawattack
    var defense = rawdefense
    var cr = diff
}

class Generator(rawId:Int, rawname:String,rawrate:Double){
    //Generators, AKA Passive Energy Generations
    val id = rawId
    val name = rawname
    var rate = rawrate
}

class Dungeon(rawenergy:Double, rawmonsters:Monster, generator:Generator){
    var energy = rawenergy
    //monster mapped to number of monsters
    var monsters = Map[Monster, Int](rawmonsters->1)
    //generators mapped to number of gnerators, this saves me from having huge lists
    var gens = Map[Generator, Int](generator-> 1)

    
}

class Player(user:String, uid:Int){
    private var name = user
    private var id = uid
    private var dungeon:Dungeon = null;

    def setUser(temp:String): Unit = {
        name = temp;
    }

    def getUser():String = {
        return name;
    }

    def setId(tempid:Int): Unit = {
        id = tempid
    }
    def getId():Int = {
        return id;
    }
    def createDungeon(mon:Monster, gen:Generator): Unit={
        dungeon = new Dungeon(0, mon,gen)
    }
    def formatMonstersForSaving():String= {
        var tempDun = ""
        var temp = dungeon.monsters.foreach((x:(Monster, Int)) => tempDun += (x._1.id.toString +"," + x._2.toString + "|"))
        return tempDun
    }
    def formatGeneratorForSaving():String={
        var tempDun = ""
        var temp = dungeon.gens.foreach((x:(Generator, Int)) => tempDun += (x._1.id.toString +"," + x._2.toString + "|"))
        return tempDun
    }
    def getEnergyLevel(): Double = {
        return dungeon.energy
    }
}

object mainInput {
    
//===========================================================================================================================================
    //I guess the first thing we need to is establisha  connection to the DB
    def main(args: Array[String]):Unit = {
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
// ==========================================================================================================================================
        println("Welcome to Dungeons Offline! TO log in, type \"login\". To make and account, type \"new\". To quit, type \"quit\"")
        //i dont like using while true loops but i will fix it later
        var res = "";
        var player:Player = null;
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
                    //this is kinda  slopy why to do it but will be fixed later, i need to get this wokring first.
                    //this basically assumes that auth was passed, and will just use what i gave itm FOR NOW
                    //TODO: FIX THIS
                    val playerQuery = "SELECT id FROM playerdata WHERE username = \""+ user + "\""
                    var stat = connection.createStatement()
                    stat.execute(playerQuery)
                    val rest = stat.getResultSet()
                    while(rest.next()){
                        player = new Player(user, rest.getInt("id"))
                    }
                    breaker = false;
                }
            }
            if(currentTries == maxTries) {
                println("You have run out of password attempts. Program quiting.")
                System.exit(0);
            }
        } else {
            //TODO: new account creation
        }
//=======================================================================================================================================================
        println("Welcome! Checking if you have a dungeon...")
        //make a new querey and statement.
        val dungeonCheck = connection.createStatement()
        val dCheckQuery = "SELECT monsterdata FROM dungeondata WHERE dungeonid = "+ player.getId().toString();
        dungeonCheck.execute(dCheckQuery)
        val res2 = dungeonCheck.getResultSet()
        while(res2.next()) {
            var resultString:String = res2.getString("monsterdata")
            if(resultString == null){
                println("No dungeon found, generating dungeon...")
                //for now, since i dont have a DB built for them, im just going to make a generaric generator
                val tempGen = new Generator(0,"Basic Accumulator", 1.5); 
                //run a query to get a random monster.
                val temp = "SELECT * FROM monsters WHERE cr = \"easy\" ORDER BY RAND() LIMIT 1"
                val statement = connection.createStatement()        
                val result = statement.executeQuery(temp)
                //TODO: make this whole thing a function to imporove readablitily
                while(result.next()){
                    val tempID = result.getInt("monsterID")
                    val tempName = result.getString("monsterName")
                    val tempCr = result.getString("cr")
                    val tempHealth = result.getInt("monsterHealth")
                    val tempAtt = result.getInt("monsterAttack")
                    val tempDef = result.getInt("monsterDefense") 
                    val tempMonster = new Monster(tempID,tempName,tempHealth,tempAtt,tempDef, tempCr)
                    //The player automatically starts off with one basic accumilator and one easy monster. 
                    player.createDungeon(tempMonster, tempGen)
                    println("Created dungeon Successfully!")
                    //update the db...
                    val monsterDat = "\"" + player.formatMonstersForSaving() + "\""
                    val genDat = "\"" + player.formatGeneratorForSaving() + "\""
                    val energyLevel = player.getEnergyLevel()
                    val tempIDagain = player.getId()
                    val queryf = s"UPDATE dungeondata SET monsterdata = $monsterDat, generators = $genDat, energy= $energyLevel WHERE dungeonID = $tempIDagain"
                    //create a new statement
                    val updateNew = connection.createStatement()
                    updateNew.execute(queryf)
                    println("Dungeon created and written to Database. Welcome, Dungeon Keeper.")
                }
                //ALRIGHT with that taken care of...
            } else {
                //This is for if the dungeon already exists.
                //TODO: get and reformat that
                println(":)")
            }
        }
        
        //close the connections at the end of the program.
        connection.close()
    }
}
