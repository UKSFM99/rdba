import java.text.SimpleDateFormat

/*
    Class in order to take the bus_info array and do various analysis to
 */

class analyze {
    fun analyze_time_deltas(array:ArrayList<bus_info>):ArrayList<bus_summeries>{
        val array_of_stops=ArrayList<stop_times>()
        val array_of_sequences=ArrayList<bus_summeries>()
        var sequence_prev=1
        var total=0
        var valid=0
        var route_time=0
        var route_start_time:String
        var direction:String
        var total_stops=1
        var num_based_scheduled=0
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        /*
            Below is an algorithm that does the following:
            1. step through the entire day's Json entries
            2. find entire route groups. Luckily this is in order so we just see when sequence goes back to 1
            3. Check if the number of stops in the route matches target number of stops
            4. Add the finished route to the analyzed array if the route is valid
         */
        for (i in 0 until array.size) {

            var loadingtime=array[i].get_stop_time(false)
            if(loadingtime<=1){loadingtime=array[i].get_stop_time(true)}
            if(array[i].Sequence.toInt() < sequence_prev) {
                route_start_time=if(array[i].ActualDepart!="") array[i].ActualDepart else array[i].ScheduledDepart
                direction=array[i].Direction
                if(sequence_prev >= array[i-1].NumberStops){
                    array_of_sequences.add(bus_summeries(direction,route_time,array_of_stops,route_start_time,num_based_scheduled))
                    valid++

                }
                total++
                array_of_stops.clear()
                total_stops+=sequence_prev
                sequence_prev=0
                route_time=0
                num_based_scheduled=0
            }
            else{
                sequence_prev = array[i].Sequence.toInt()
                if(i!=0){//make sure we don't go into negative array indexes!
                    var delta_time=0
                    try {
                        delta_time=(format.parse(array[i].ActualArrival).time / 1000 - format.parse(array[i - 1].ActualDepart).time / 1000).toInt()
                    }
                    catch(e:Exception){
                        num_based_scheduled++
                        delta_time=(format.parse(array[i].ScheduledArrival).time / 1000 - format.parse(array[i - 1].ScheduledDepart).time / 1000).toInt()
                        System.err.println("Time is invalid, falling back to scheduled time. Bus ID is ${array[i].VehicleCode} at ${array[i].LocationName} at estimated time of ${array[i].ScheduledArrival}")
                    }
                    //add index
                    route_time+=delta_time
                    array_of_stops.add(stop_times(array[i-1].LocationName,
                            array[i-1].LocationCode,
                            array[i].LocationName,
                            array[i].LocationCode,
                            delta_time,
                            loadingtime,
                            array[i].ActualArrival,
                            array[i].ScheduledDepart,
                            array[i].get_time_late(false),
                            array[i].get_time_late(true)
                    ))
                }
                route_time+=loadingtime
            }
        }
        //finished adding - now work out how valid the data is
        println("~${((valid.toDouble()/total.toDouble())*100).toInt()}% Data correct ($valid/$total entries)")
        return array_of_sequences
    }
}