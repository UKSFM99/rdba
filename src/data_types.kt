//LATLNG
data class LatLng(val Latitude:Double,val Longitude:Double){
    fun get_lat():Double{
        return Latitude
    }
    fun get_long():Double{
        return Longitude
    }
    fun toArrayList():ArrayList<Double>{
        return arrayListOf(Latitude,Longitude)
    }
}

//search array and return index of element
fun ArrayList<*>.search(to_find:String):Int?{
    this.forEach { i ->
        if(i.toString().contains(to_find)){
            return this.indexOf(i)
        }
    }
    return null
}

class color{
    private val os_name=System.getProperty("os.name")
    fun printgreen(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[32m$input\u001B[0m")
    fun printred(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[31m$input\u001B[0m")
    fun printblack(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[30m$input\u001B[0m")
    fun printblue(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[34m$input\u001B[0m")
    fun printpurple(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[35m$input\u001B[0m")
    fun printcyan(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[36m$input\u001B[0m")
    fun printwhite(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[37m$input\u001B[0m")
    fun printyellow(input:String)=if (os_name.contains("win")) println(input) else println("\u001B[33m$input\u001B[0m")
}