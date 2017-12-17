import org.junit.Test
import kotlin.test.assertEquals

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

class text_color(){
    val ANSI_RESET = "\u001B[0m"
    val ANSI_BLACK = "\u001B[30m"
    val ANSI_RED = "\u001B[31m"
    val ANSI_GREEN = "\u001B[32m"
    val ANSI_YELLOW = "\u001B[33m"
    val ANSI_BLUE = "\u001B[34m"
    val ANSI_PURPLE = "\u001B[35m"
    val ANSI_CYAN = "\u001B[36m"
    val ANSI_WHITE = "\u001B[37m"
}