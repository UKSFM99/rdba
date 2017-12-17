
import java.io.FileReader
import java.io.BufferedReader
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList


/*
    TBA for actual function
 */

class Neuron(val id:String,val route:String) {
    val metadata:Any
    private var location = Pair(LatLng(0.toDouble(),0.toDouble()),"")
    private var prev_location = Pair(LatLng(0.toDouble(),0.toDouble()),"")
    init{
        this.metadata = bus_specs().get_specs(id)
    }
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private fun get_time_delta(time_now:String,time_before:String):Int = ((format.parse(time_now).time/1000)-(format.parse(time_before).time/1000)).toInt()
    //Sigmoid function. Take any value an return a number from 0-1
    //Let this be our neuron Weight
    fun sigmoid(input:Double): Double = (1/(1+Math.pow(Math.E,(-1*input))))

    fun update_location(input:Pair<LatLng,String>){
        if(input.second != location.second) {
            prev_location = location
            location = input
            if (prev_location.first != LatLng(0.toDouble(), 0.toDouble())) {
                try {
                    if (location.first != prev_location.first) {
                        println("Bus $id moved ${get_distance_traveled_m()} m at ${get_speed(get_distance_traveled_m(), get_time_delta(location.second, prev_location.second))} mph")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        else{println("${text_color().ANSI_YELLOW}Bus $id got no update this round ${text_color().ANSI_RESET}")}
    }
    fun print_specs(){
        if(metadata!="") {
            metadata as ArrayList<String>
            println("\n")
            System.out.println("""
                ---BUS INFO---
                FLEET NUMBER: ${metadata[0]}
                REG NUMBER:   ${metadata[1]}
                DATE REGISTERED:${metadata[22]}
                MAX SEATED: ${metadata[20]}
                MAX STANDING: ${metadata[21]}
        """.trimIndent())
        }
        else{System.err.println("Warning. No Fleet data for ID $id")}
    }
    fun get_distance_traveled_m():Int{
        val Rad = 6371.0 //Earth's Radius In kilometers
        // TODO Auto-generated method stub
        val dLat = Math.toRadians(location.first.Latitude - prev_location.first.Latitude)
        val dLon = Math.toRadians(location.first.Longitude - prev_location.first.Longitude)
        val lat1 = Math.toRadians(prev_location.first.Latitude)
        val lat2 = Math.toRadians(location.first.Latitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
        val c = 2 * Math.asin(Math.sqrt(a))
        val haverdistanceKM = Rad * c
        return (haverdistanceKM*1000).toInt()
    }
    fun get_speed(distance:Int,time:Int):Long = ((distance.toDouble()/time.toDouble())*2.23694).toLong()
}

