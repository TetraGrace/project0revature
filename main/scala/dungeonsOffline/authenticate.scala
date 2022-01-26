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
