
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