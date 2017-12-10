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