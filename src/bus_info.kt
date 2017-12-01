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
                    val RunningBoard:String,val Duty:Int,
                    val Direction:String, val JourneyCode:Int,
                    val VehicleCode:String, val DriverCode:String,
                    val TimingPoint:Boolean, val JourneyPattern:String,
                    val NumberStops:Int, val StartPoint:String,
                    val EndPoint:String, val Location_lat_lng:Array<Double>,
                    val ScheduledArrival:String,val ActualArrival:String,
                    val ScheduledDepart:String,val ActualDepart:String
                    ){
    fun get_stop_time(is_based_on_schedule:Boolean):Int {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val arrivaltime = if (is_based_on_schedule) format.parse(this.ScheduledArrival) else (format.parse(this.ActualArrival))
            val departuretime = if (is_based_on_schedule) format.parse(this.ScheduledDepart) else (format.parse(this.ActualDepart))
            val arrival_unix = (arrivaltime.time / 1000)
            val departure_unix = (departuretime.time / 1000)
            return (departure_unix - arrival_unix).toInt()
        }
        catch (e:Exception){return 0}
    }
    fun get_time_late(is_depart:Boolean):Int {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val schedule = if (is_depart) format.parse(this.ScheduledDepart) else (format.parse(this.ActualDepart))
            val actual = if (is_depart) format.parse(this.ScheduledArrival) else (format.parse(this.ActualArrival))
            val schedule_unix = (schedule.time / 1000)
            val actual_unix = (actual.time / 1000)
            return (actual_unix - schedule_unix).toInt()
        }
        catch (e:Exception){return 0}
    }
}

//Class that holds information about each route details
data class stop_times(val previous_location:String,val previous_location_uuid:String,
                      val current_location:String,val current_location_uuid:String,
                      val travel_time:Int,val waiting_time:Int,
                      val arrive_time:String,val depart_time:String,
                      val time_late_arrive:Int,val time_late_depart:Int)

//Class that holds information about each route trip
data class bus_summeries(val direction:String,val total_time:Int,
                         val array:ArrayList<stop_times>,val start_time:String)

//Class that holds average enteries for inbound and outbound journeys
data class bus_average(val timestamp:String,val inbound_avg:String,val inbound_nodes:Int,val outbound_avg:String,val outbound_nodes:Int)

//class that reads the specs of each bus from fleetlist.csv
class bus_specs{
    private val specs = ArrayList<ArrayList<String>>()
    fun get_specs(bus_id:String):ArrayList<String>?{
        if(specs.size==0){
            read()
        }
        return specs.firstOrNull { it[0] == bus_id }
    }
    private fun read(){
        val inputStream: InputStream = File("fleetdata.dat").inputStream()
        val lineList = mutableListOf<String>()
        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }
        lineList.forEach{
            specs.add(ArrayList(it.split(',')))
        }
        specs.removeAt(0)

    }
}