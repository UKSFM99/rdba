import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
                            Math.abs(get_time_delta(array[i].ActualDepart,array[i].ActualArrival)),
                            array[i].ActualArrival,array[i].ActualDepart,
                                    Math.abs(get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival)),
                                    Math.abs(get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart))))
                    start_time=array[i].ActualDepart
                    num_of_stops=1
                }
                array[i].EndPoint->{
                    array_of_stops.add(stop_times(array[i-1].LocationName,array[i-1].LocationCode,
                            array[i].LocationName,array[i].LocationCode,
                            array[i].Location_lat_lng,
                            Math.abs(get_time_delta(array[i].ActualArrival,array[i-1].ActualArrival)),
                                    Math.abs(get_time_delta(array[i].ActualDepart,array[i].ActualArrival)),
                            array[i].ActualArrival,array[i].ActualDepart,
                            Math.abs(get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival)),
                            Math.abs(get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart))))
                    val end_time=array[i].ActualArrival
                    if(array[i].NumberStops <= num_of_stops) {
                        array_of_sequences.add(bus_summeries(array[i].Direction, get_time_delta(end_time, start_time), array_of_stops, start_time, end_time,array[i].JourneyPattern))
                    }
                }
                else->{
                    array_of_stops.add(stop_times(array[i-1].LocationName,array[i-1].LocationCode,
                                                  array[i].LocationName,array[i].LocationCode,
                                                  array[i].Location_lat_lng,
                                                  Math.abs(get_time_delta(array[i].ActualArrival,array[i-1].ActualArrival)),
                                                  Math.abs(get_time_delta(array[i].ActualDepart,array[i].ActualArrival)),
                                                  array[i].ActualArrival,array[i].ActualDepart,
                                                  Math.abs(get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival)),
                                                  Math.abs(get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart))))
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
        val routes_done = HashMap<String,Int>()//KEY:Pattern type VALUE:occurrences
        //scan each occurrence of Route type
        for (i in 0 until array.size) {
            val count = routes_done[array[i].patterntype]
            if (count == null) {
                routes_done.put(array[i].patterntype, 1)
                val arrayofstops = ArrayList<stops>()
                array[i].array.mapIndexedTo(arrayofstops) { stop_num, i -> stops(i.position, i.current_location, i.current_location_uuid, stop_num) }
                return_array.add(route_stops(array[i].patterntype,arrayofstops))
            }

        }
        for (i in 0 until array.size) {
            val count = routes_done[array[i].patterntype]
            if (count == null) {
                routes_done.put(array[i].patterntype, 1)
                val arrayofstops = ArrayList<stops>()
                array[i].array.mapIndexedTo(arrayofstops) { stop_num, i -> stops(i.position, i.current_location, i.current_location_uuid, stop_num) }
                return_array.add(route_stops(array[i].patterntype,arrayofstops))
            }

        }
        return return_array
    }
    fun analyze_stop_measures(resolution:Long,date:String,array:ArrayList<bus_summeries>):ArrayList<stop_time_training>{
        val start:Long = format.parse("$date 00:00:00").time/1000
        val end:Long = 1+format.parse("$date 23:59:59").time/1000
        val map = ArrayList<stop_time_training>()
        val format_output_date = SimpleDateFormat("HH:mm:ss")
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        for(t in (start+resolution)..end step resolution) {
            val timestamp="${format_output_date.format(Date((t-resolution)*1000))} - ${format_output_date.format(Date((t)*1000))}"
            array.forEach { (direction, _, array1) ->
                array1.forEach { k ->
                    //if in time range
                    if(format.parse(k.arrive_time).time/1000 >=t && format.parse(k.arrive_time).time/1000 < (t+resolution)) {
                        val data = map.search("${k.previous_location},${k.current_location},${direction}")
                        //stop entry was not found, create on
                        if (data == null) {
                            map.add(stop_time_training("${k.previous_location},${k.current_location},${direction}", arrayListOf(times_data(timestamp, arrayOf(1,k.travel_time,k.travel_time,k.travel_time)))))
                        //stop entry was found, search for timestamp
                        } else {
                            //timestamp was found, so update it
                            val time_id=map[data].times.search(timestamp)
                            if(time_id!=null) {
                                map[data].times[time_id].data[0] = map[data].times[time_id].data[0]+1
                                map[data].times[time_id].data[1] = map[data].times[time_id].data[1]+1 + k.travel_time
                                map[data].times[time_id].data[2] = if (map[data].times[time_id].data[2] < k.travel_time) map[data].times[time_id].data[2] else k.travel_time
                                map[data].times[time_id].data[3] = if (map[data].times[time_id].data[3] > k.travel_time) map[data].times[time_id].data[3] else k.travel_time
                            }
                            //timestamp was not found so add a new one
                            else{
                                map[data].times.add(times_data(timestamp, arrayOf(1,k.travel_time,k.travel_time,k.travel_time,k.travel_time)))
                            }
                        }
                    }
                }
            }
        }
        //remove entries we do not want from data
        map.removeIf { (from_to) -> from_to.split(',')[0] == from_to.split(',')[1] }
        map.removeIf { (from_to) -> from_to.split(',')[0] == "None" }
        return map
    }
}
