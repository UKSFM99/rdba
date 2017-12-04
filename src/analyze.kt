import java.text.SimpleDateFormat
import java.util.*

/*
    Class in order to take the bus_info array and do various analysis to
 */

class analyze {
    fun analyze_time_deltas(array:ArrayList<bus_info>):ArrayList<bus_summeries>{
        var array_of_sequences=ArrayList<bus_summeries>()
        var start_time=""
        var num_of_stops=0
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
                    array_of_stops=ArrayList<stop_times>()
                    array_of_stops.add(stop_times("None","0",
                            array[i].LocationName,array[i].LocationCode,
                            0,
                            get_time_delta(array[i].ActualDepart,array[i].ActualArrival),
                            array[i].ActualArrival,array[i].ActualDepart,
                            get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival),
                            get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart)))
                    start_time=array[i].ActualDepart
                    num_of_stops=0
                }
                array[i].EndPoint->{
                    array_of_stops.add(stop_times(array[i-1].LocationName,array[i-1].LocationCode,
                            array[i].LocationName,array[i].LocationCode,
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
                                                  get_time_delta(array[i].ActualArrival,array[i-1].ActualDepart),
                                                  get_time_delta(array[i].ActualDepart,array[i].ActualArrival),
                                                  array[i].ActualArrival,array[i].ActualDepart,
                                                  get_time_delta(array[i].ActualArrival,array[i].ScheduledArrival),
                                                  get_time_delta(array[i].ActualDepart,array[i].ScheduledDepart)))

                }
            }
            num_of_stops++
        }
        var patterns=HashMap<String,Int>()//KEY:Pattern type VALUE:occurrences
        //scan each occurrence of Route type
        for(i in 0 until array_of_sequences.size) {
            val count = patterns.get(array_of_sequences[i].patterntype)
            if (count == null) {
                patterns.put(array_of_sequences[i].patterntype, 1)
            } else {
                patterns.put(array_of_sequences[i].patterntype, count + 1)
            }
        }
        //sort the array by occurances (value)
        val sorted_patterns=patterns.toList().sortedBy { (key,value) -> value}.toMap().toList()
        println("Route Pattern occurrences:")
        for(i in sorted_patterns){
            println(i)
        }
        val route_occurance_threashold=5
        //TODO add configuration to change threshold above
        val wanted_routes=ArrayList<String>()
        for(i in sorted_patterns){
            if(i.second > route_occurance_threashold){wanted_routes.add(i.first)}//add routes that have more than 5 occurances
        }
        println("Allowing routes: ${wanted_routes} (Above threshold of ${route_occurance_threashold})")
        array_of_sequences.removeIf { i -> i.patterntype !in wanted_routes }//remove any routes that are not valif from wanted routes
        return array_of_sequences
    }
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private fun get_time_delta(time_now:String,time_before:String):Int = ((format.parse(time_now).time/1000)-(format.parse(time_before).time/1000)).toInt()
}