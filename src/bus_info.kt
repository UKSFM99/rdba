import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

/*
    Class that holds multiple data classes with various information
 */

//Most verbose class -> Direct output from JSON goes here
data class bus_info(val Lineref:String,val LocationCode:String,
                    val LocationName:String,val JourneyType:String,
                    val LiveJourneyId:String,val Sequence:String,
                    val RunningBoard:String,val Duty:String,
                    val Direction:String, val JourneyCode:String,
                    val VehicleCode:String, val DriverCode:String,
                    val TimingPoint:Boolean, val JourneyPattern:String,
                    val NumberStops:Int, val StartPoint:String,
                    val EndPoint:String, val Location_lat_lng:LatLng,
                    val ScheduledArrival:String,val ActualArrival:String,
                    val ScheduledDepart:String,val ActualDepart:String
                    )

//Class that holds information about each route details
data class stop_times(val previous_location:String,val previous_location_uuid:String,
                      val current_location:String,val current_location_uuid:String,
                      val position:LatLng,
                      val travel_time:Int,val waiting_time:Int,
                      val arrive_time:String,val depart_time:String,
                      val delta_arrival:Int,val delta_departure:Int)

//hold location and stop number on route
data class stops(val Location:LatLng,val name:String, val UUID:String,val Position:Int)

//hold pattern type and stops array
data class route_stops(val pattern:String,val stops_list:ArrayList<stops>)

//Class that holds information about each route trip
data class bus_summeries(val direction:String,val total_time:Int,
                         val array:ArrayList<stop_times>,val start_time:String,val end_time:String,val patterntype:String)

//Class that holds average entries for inbound and outbound journeys
data class bus_average(val timestamp:String,val inbound_avg:String,val inbound_nodes:Int,val outbound_avg:String,val outbound_nodes:Int)


//class that reads the specs of each bus from fleetlist.csv
class bus_specs{
    private val specs = ArrayList<ArrayList<String>>()//2D array
    fun get_specs(bus_id:String):ArrayList<String>?{
        if(specs.size==0){ read() }//read if array is null, if not just scan
        return specs.firstOrNull { it[0] == bus_id }
    }
    private fun read(){
        val lineList = mutableListOf<String>()
        File("fleetdata.dat").inputStream().bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }
        lineList.forEach{
            specs.add(ArrayList(it.split(',')))
        }
        specs.removeAt(0)
    }
}