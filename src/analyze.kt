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
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        for (i in 0 until array.size) {
            var loadingtime=array[i].get_stop_time(false)
            if(loadingtime<=1){loadingtime=array[i].get_stop_time(true)}
            if(array[i].Sequence.toInt() < sequence_prev) {
                route_start_time=array[i].ActualDepart
                direction=array[i].Direction
                if(sequence_prev < array[i-1].NumberStops){
                }
                else{
                    val data=bus_specs().get_specs(array[i].VehicleCode)
                    if(data==null){println("\nBus not found with ID ${array[i].VehicleCode}")}
                    else {
                        println(data)
                        println("\n")
                        println("""
                        ---Bus Info for ID ${data[0]}---
                        License     : ${data[1]}
                        Type        : ${data[3]}
                        Livery      : ${data[9]}
                        Registered  : ${data[22]}
                        Max Seated  : ${if((data[20])=="") "Unknown" else data[20]}
                        Max Standing: ${if((data[21])=="") "Unknown" else data[21]}
                        Has WIFI    : ${if((data[5])=="") "No" else "Yes"}
                    """.trimIndent())
                    }
                    array_of_sequences.add(bus_summeries(direction,route_time,array_of_stops,route_start_time))
                    valid++

                }
                //println("\n")
                total++
                array_of_stops.clear()
                total_stops+=sequence_prev
                sequence_prev=0
                route_time=0
            }
            else{
                sequence_prev = array[i].Sequence.toInt()
                if(i!=0){
                    route_time+=(format.parse(array[i].ActualArrival).time/1000 - format.parse(array[i-1].ActualDepart).time/1000).toInt()
                    array_of_stops.add(stop_times(array[i-1].LocationName,array[i-1].LocationCode,
                            array[i].LocationName,array[i].LocationCode,
                            (format.parse(array[i].ActualArrival).time/1000 - format.parse(array[i-1].ActualDepart).time/1000).toInt(),
                            loadingtime,array[i].ActualArrival,array[i].ScheduledDepart,array[i].get_time_late(false),array[i].get_time_late(true)))
                }
                route_time+=loadingtime
            }
        }
        println("${((valid.toDouble()/total.toDouble())*100).toInt()}% Data correct ($valid/$total entries)")
        println("computer says: ${Neuron().how_bad_is_the_error(total,valid)}")
        return array_of_sequences
    }
}