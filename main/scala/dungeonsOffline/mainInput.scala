package dungeonsOffline
import scala.io.Source
import scala.io.StdIn.readLine
import dungeon._
import org.apache.spark.sql.execution.SQLExecution
import java.sql.DriverManager
import java.sql.Connection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import authenticate._
import player.Player

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
                case "quit" => {connection.close();System.exit(0)}
                case "login" => res = "login"
                case "new" => {println("Welcome, you are making a new character. Please enter a username (case-sensitive)")
                                 val newUser ="\"" + readLine("Username:") + "\""
                                println("Please enter a password (case-sensitive)")
                                val pass ="\"" + readLine("Password:") + "\""
                                println("Creating account...")
                                val statement1 = connection.createStatement()
                                val emptyJSON = "NULL"
                                val query1 = "SELECT id FROM playerdata ORDER BY id DESC LIMIT 1"
                                statement1.execute(query1)
                                var resulting= statement1.getResultSet()
                                var i = 0;
                                while(resulting.next()){
                                i = resulting.getInt("id") + 1
                                }
                                val query2 = s"INSERT INTO playerData VALUES ($i,$newUser,$pass);"
                                val query3 = s"INSERT INTO dungeondata VALUES ($i, $emptyJSON , 0, $emptyJSON);"
                                statement1.execute(query2)
                                statement1.execute(query3)
                                println("New player created. Please login.")}
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
                val pass = stdIn.readLine("Password:").toString()
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
        } else if (res == "new"){
            
        } else {
            println(" :) ")
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
                //pull down the data about the player's dungeon
                val getData = connection.createStatement()
                val playerID= player.getId()
                val dungeonQuery = s"SELECT * FROM dungeondata WHERE dungeonid = $playerID"
                getData.execute(dungeonQuery)
                val res = getData.getResultSet()
                var monsterString = ""
                var genString = ""
                var energy = 0.0
                while(res.next()){
                    monsterString = res.getString("monsterdata")
                    genString = res.getString("generators")
                    energy = res.getDouble("energy")
                }
                //not that we have it from the database, we need to parse it
                var splitMonsterData = monsterString.split("&")
                var splitGenData= genString.split("&")
                var monstersToGet = new ArrayBuffer[Int]();
                var monstersAmounts = new ArrayBuffer[Int]();
                var tempMonsterMap = Map[Monster,Int]();
                //usually, I would need to do the same thing twice for the genrators, but since im using a temp, i dont need too >:)
                //now we have two arrays of strings 
                for(i<-0 until splitMonsterData.length){
                    var tempbuffer = splitMonsterData(i).split(",")
                    monstersToGet += tempbuffer(0).toInt
                    monstersAmounts += tempbuffer(1).toInt
                    //ok so this should do it.
                }
                println(monstersToGet.length)
                //now loop though those new arrays
                for(i<-0 until monstersToGet.length){
                    val currMonId = monstersToGet(i)
                    val monsterConn = connection.createStatement()
                    val monQuery = s"SELECT * FROM monsters WHERE monsterid = $currMonId"
                    monsterConn.execute(monQuery)
                    val res = monsterConn.getResultSet()
                    while(res.next()){
                        val tempID = res.getInt("monsterID")
                        val tempName = res.getString("monsterName")
                        val tempCr = res.getString("cr")
                        val tempHealth = res.getInt("monsterHealth")
                        val tempAtt = res.getInt("monsterAttack")
                        val tempDef = res.getInt("monsterDefense") 
                        val tempMonster = new Monster(tempID,tempName,tempHealth,tempAtt,tempDef, tempCr)
                        tempMonsterMap += (tempMonster -> monstersAmounts(i))
                    }
                }
                val generatorsNumber = splitGenData(0).split(",")
                var finNum = generatorsNumber(1).toInt
                val tempGen = new Generator(0,"Basic Accumulator", 1.5);
                //this is lazy. I know its lazy. I dont have time to fix it.
                val genMap = Map[Generator,Int](tempGen -> finNum)
                player.createDungeon(tempMonsterMap, genMap, energy)
                println("Dungeon loaded. Welcome back, Dungeon Keeper.")
            }
        }
//================================================================================================================================
        //this is where the actual game starts lol
        var userInput = ""
        do{
            //ok look. This is rough. I'm not goikng to be doing much input sanatization, if any.
            //im just going to do the bare minimun to get the requirment and to satisfy me.
            println("Please input a command. Availible commands: \"tick\", \"view\", \"save\", \"delete\", and \"quit\".")
            userInput = readLine("> ")
            userInput.toLowerCase match {
                case "view" => println(player.getDungeonStatus())
                case "save" => {
                                val monsterDat = "\"" + player.formatMonstersForSaving() + "\""
                                val genDat = "\"" + player.formatGeneratorForSaving() + "\""
                                val energyLevel = player.getEnergyLevel()
                                val tempIDagain = player.getId()
                                val queryf = s"UPDATE dungeondata SET monsterdata = $monsterDat, generators = $genDat, energy= $energyLevel WHERE dungeonID = $tempIDagain"
                                //create a new statement
                                val updateNew = connection.createStatement()
                                updateNew.execute(queryf)}
                case "delete" =>{
                                    println("WARNING: this will PERMANTENTLY DELETE your save and account. Proceed? ")
                                    val check = readLine("[Y/n]:")
                                    check.toLowerCase() match {
                                        case "y"=> {
                                            println("deleteing account. Program will close after deletion.")
                                            val playerid = player.getId()
                                            val deleteQuery = s"DELETE FROM dungeondata WHERE dungeonid= $playerid"
                                            val deleteQuery2 = s"DELETE FROM playerdata WHERE id = $playerid"
                                            val deletion = connection.createStatement()
                                            deletion.execute(deleteQuery)
                                            deletion.execute(deleteQuery2)
                                            connection.close()
                                            sys.exit(0)
                                        }
                                        case _ => println("Not deleting account.")
                                    }
                                }
                case "quit" => {println("Quiting...");connection.close(); sys.exit(0)}
                case "tick" => {
                                player.tick()
                                }
            }

        }while(userInput != "quit")
        
        //close the connections at the end of the program.
        connection.close()
    }
}
