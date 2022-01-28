package dungeonsOffline

import scala.collection.mutable.Map


object dungeon {
  class Generator(rawId:Int, rawname:String,rawrate:Double){
    //Generators, AKA Passive Energy Generations
    val id = rawId
    val name = rawname
    var rate = rawrate
  }

  class Monster(rawid:Int,rawname:String,rawhealth:Int,rawattack:Int, rawdefense:Int, diff:String){
    //the Monster Class, for all your Monster Needs.
    val name = rawname;
    val id = rawid //This is a VAL cause the id of the monster should never change.
    var health = rawhealth
    var attack = rawattack
    var defense = rawdefense
    var cr = diff
  }

  class Dungeon(rawenergy:Double, rawmonsters:Monster, generator:Generator){
    var energy = rawenergy
    //monster mapped to number of monsters
    var monsters = Map[Monster, Int](rawmonsters->1)
    //generators mapped to number of gnerators, this saves me from having huge lists
    var gens = Map[Generator, Int](generator-> 1)
  }
}
