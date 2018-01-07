import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/*
    Class that holds multiple data classes with various information
 */

//Direct output from reading buses API gets structured and put into bus_info objects here
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
                      val delta_arrival:Int,val delta_departure:Int,
                      val timing_point:Boolean)

//hold location and stop number on route
data class stops(val Location:LatLng,val name:String, val UUID:String,val is_timing:Boolean,var Position:Int)

//hold pattern type and stops array
data class route_stops(val pattern:String,val stops_list:ArrayList<stops>)

//Class that holds information about each route trip
data class bus_summeries(val direction:String,val total_time:Int,
                         val array:ArrayList<stop_times>,val start_time:String,val end_time:String,val patterntype:String)

//Class that holds average entries for inbound and outbound journeys
data class bus_average(val timestamp:String,val inbound_avg:String,val inbound_nodes:Int,val outbound_avg:String,val outbound_nodes:Int)

//class to hold our actual training data for stops
data class stop_time_training(val from_to:String, var times: ArrayList<times_data>)


data class times_data(val timestamp:String,val data:Array<Int>) {//Array contains count,totaltime,min,max
        fun get_avg() = (data[1].toDouble() / data[0].toDouble()).toInt()
        //have an array in data class, override equals() and hashcode() function to prevent bugs! - Auto generated
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as times_data

            if (timestamp != other.timestamp) return false
            if (!Arrays.equals(data, other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = timestamp.hashCode()
            result = 31 * result + Arrays.hashCode(data)
            return result
        }
    }



//class that reads the specs of each bus from fleetlist.dat
class bus_specs{
    private var specs = ArrayList<bus_spec_sheet>()
    fun get_specs(bus_id:String):bus_spec_sheet{
        if(specs.size==0){ read() }//read if array is null, if not just scan
        specs.forEach { i ->
            if(i.ID==bus_id){
                return i
            }
        }
        return bus_spec_sheet("N/A","N/A","N/A","N/A","N/A","N/A","N/A","N/A","N/A","N/A",
                "N/A","N/A","N/A","N/A","N/A","N/A","N/A","N/A","N/A","N/A",
                "N/A","N/A","N/A","N/A")
    }
    private fun read(){
        val lineList = mutableListOf<String>()
        File("fleetdata.dat").inputStream().bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }
        lineList.forEach{
            if(!it.contains("//")||!it.contains("Fleet")) {
                val arr=it.split(',')
                try {
                    specs.add(bus_spec_sheet(
                            if (arr[0] != "") arr[0] else "N/A",
                            if (arr[1] != "") arr[1] else "N/A",
                            if (arr[2] != "") arr[2] else "N/A",
                            if (arr[3] != "") arr[3] else "N/A",
                            if (arr[4] != "") arr[4] else "N/A",
                            if (arr[5] != "") arr[5] else "N/A",
                            if (arr[6] != "") arr[6] else "N/A",
                            if (arr[7] != "") arr[7] else "N/A",
                            if (arr[8] != "") arr[8] else "N/A",
                            if (arr[9] != "") arr[9] else "N/A",
                            if (arr[10] != "") arr[10] else "N/A",
                            if (arr[11] != "") arr[11] else "N/A",
                            if (arr[12] != "") arr[12] else "N/A",
                            if (arr[13] != "") arr[13] else "N/A",
                            if (arr[14] != "") arr[14] else "N/A",
                            if (arr[15] != "") arr[15] else "N/A",
                            if (arr[16] != "") arr[16] else "N/A",
                            if (arr[17] != "") arr[17] else "N/A",
                            if (arr[18] != "") arr[18] else "N/A",
                            if (arr[19] != "") arr[19] else "N/A",
                            if (arr[20] != "") arr[20] else "N/A",
                            if (arr[21] != "") arr[21] else "N/A",
                            if (arr[22] != "") arr[22] else "N/A",
                            if (arr[23] != "") arr[23] else "N/A"))
                }catch (e:Exception){}

            }
        }
        specs.removeAt(0)
    }
    data class bus_spec_sheet(val ID:String,val Registration:String,
        val Operating_division:String,val Livery:String, val Tacho_type:String,
        val wifi:String,val router_type:String,val model:String,val make:String,
        val body:String,val Chassis_type:String,val chassis_no:String, val engine_no:String,
        val euro_cat:String,val CCTV:String, val Dest_type:String, val front_size:String,
        val side_size:String, val rear_size:String, val body_no:String, val Seated:String,
        val Standing:String,val reg_date:String,val license_no:String)
}

//LIVE INFO DATA
//All info from live API gets put into these objects
data class bus_live(val id:String?,val service:String,val heading:Int,val location:LatLng,val timestamp:String)