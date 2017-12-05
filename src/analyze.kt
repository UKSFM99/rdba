import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/*
    Class in order to take the bus_info array and do various analysis to
 */

class analyze {
    fun analyze_times(array:ArrayList<bus_info>):ArrayList<bus_summeries>{
        val array_of_sequences=ArrayList<bus_summeries>()
        var start_time=""
        var num_of_stops=1
        var array_of_stops=ArrayList<stop_times>()
        /*
            Below is an algorithm that does the following:
            1. step through the entire day's Json entries
            2. find entire route groups. Luckily this is in order so we just see when sequence goes back to 1
            3. Check if the number of stops in the route matches target number of stops
            4. Add the finished route to the analyzed array if the route is valid
         */
        for (i in 0 until array.size) {
            when(array[i].LocationName){
                array[i].StartPoint -> {
                    array_of_stops=ArrayList()
                    array_of_stops.add(stop_times("None","0",
                            array[i].LocationName,array[i].LocationCode,
                            array[i].Location_lat_lng, 0,
                            get_time_delta(array[i].ActualDepart,array[i].ActualArrival),
                            array[i].ActualArrival,array[i].ActualDepart,
                            get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival),
                            get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart)))
                    start_time=array[i].ActualDepart
                    num_of_stops=1
                }
                array[i].EndPoint->{
                    array_of_stops.add(stop_times(array[i-1].LocationName,array[i-1].LocationCode,
                            array[i].LocationName,array[i].LocationCode,
                            array[i].Location_lat_lng,
                            get_time_delta(array[i].ActualArrival,array[i-1].ActualDepart),
                            get_time_delta(array[i].ActualDepart,array[i].ActualArrival),
                            array[i].ActualArrival,array[i].ActualDepart,
                            get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival),
                            get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart)))
                    val end_time=array[i].ActualArrival
                    if(array[i].NumberStops <= num_of_stops) {
                        array_of_sequences.add(bus_summeries(array[i].Direction, get_time_delta(end_time, start_time), array_of_stops, start_time, end_time,array[i].JourneyPattern))
                    }
                }
                else->{
                    array_of_stops.add(stop_times(array[i-1].LocationName,array[i-1].LocationCode,
                                                  array[i].LocationName,array[i].LocationCode,
                                                  array[i].Location_lat_lng,
                                                  get_time_delta(array[i].ActualArrival,array[i-1].ActualDepart),
                                                  get_time_delta(array[i].ActualDepart,array[i].ActualArrival),
                                                  array[i].ActualArrival,array[i].ActualDepart,
                                                  get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival),
                                                  get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart)))
                }
            }
            num_of_stops++
        }
        return array_of_sequences
    }
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private fun get_time_delta(time_now:String,time_before:String):Int = ((format.parse(time_now).time/1000)-(format.parse(time_before).time/1000)).toInt()

    //Work out different routes and each stop location/position in the route
    fun analyze_routes(array:ArrayList<bus_summeries>,route:String):ArrayList<route_stops> {
        val return_array = ArrayList<route_stops>()
        val routes_done = HashMap<String, Int>()//KEY:Pattern type VALUE:occurrences
        //scan each occurrence of Route type
        for (i in 0 until array.size) {
            val count = routes_done.get(array[i].patterntype)
            if (count == null) {
                routes_done.put(array[i].patterntype, 1)
                val arrayofstops = ArrayList<stops>()
                var stop_num = 0
                for (i in array[i].array) {
                    arrayofstops.add(stops(i.position, i.current_location, i.current_location_uuid, stop_num))
                    stop_num++
                }
                return_array.add(route_stops(array[i].patterntype,arrayofstops))
            }

        }
        return_array.forEach { i -> File("output/routes/$route ${i.pattern}.csv").printWriter().use {
            it.append("Stop number,Stop Name,Stop UUID,Stop Location Lat,Stop Location Long\n")
            var stop_id=0
                i.stops_list.forEach { k -> it.append("$stop_id,${k.name},${k.UUID},${k.name},${k.Location.Latitude},${k.Location.Longitude}\n")
                stop_id++
                }
            }
        }
        return return_array
    }
}