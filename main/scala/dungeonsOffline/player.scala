package dungeonsOffline

import dungeonsOffline.dungeon.{Dungeon, Generator, Monster}

import scala.collection.mutable.Map

object player {
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

    def createDungeon(mon:Map[Monster, Int], gen:Map[Generator, Int], en:Double):Unit = {
      createDungeon(null,null)
      dungeon.monsters = mon
      dungeon.gens = gen
      dungeon.energy = en
    }

    def formatMonstersForSaving():String= {
      var tempDun = ""
      var temp = dungeon.monsters.foreach((x:(Monster, Int)) => tempDun += (x._1.id.toString +"," + x._2.toString + "&"))
      return tempDun
    }
    def formatGeneratorForSaving():String={
      var tempDun = ""
      var temp = dungeon.gens.foreach((x:(Generator, Int)) => tempDun += (x._1.id.toString +"," + x._2.toString + "&"))
      return tempDun
    }
    def getEnergyLevel(): Double = {
      return dungeon.energy
    }
    def getDungeonStatus():String = {
      //returns a preformatted String toprint out a stus of the dungeon in the console.
      var finalOut = "+-----------------------------------+\n"
      finalOut +=    "| Monsters:                         |\n"

      var mName = "| "
      dungeon.monsters.foreach(t => (mName += t._1.name + ": " + t._2.toString()))
      while(mName.length < 36) {
        mName += " "
      }
      mName +="|\n"
      finalOut += mName;
      finalOut +=    "| Generators:                       |\n"
      mName = "| "
      dungeon.gens.foreach(t => (mName += t._1.name + ": " + t._2.toString()))
      while(mName.length < 36) {
        mName += " "
      }
      mName +="|\n"
      finalOut += mName
      mName = "| Energy: " + getEnergyLevel().toString()
      while(mName.length < 36) {
        mName += " "
      }
      mName +="|\n"
      finalOut += mName
      finalOut += "+-----------------------------------+\n"
      return finalOut
    }
    def tick(){
      dungeon.energy += 1.5;
    }
  }
}
