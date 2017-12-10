import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

/*
    Class that holds multiple output functions, depending on data type
 */

class output {
    fun output_averages(date:String,array:ArrayList<bus_info>,route:String){
        val resolution:Long=1*60*60
        val recorded=analyze().analyze_times(array)
        val routes=analyze().analyze_routes(recorded,route)
        val routes_details=analyze().analyze_stop_measures(resolution,date,recorded)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val format_output_date = SimpleDateFormat("HH:mm:ss")
        val start:Long = format.parse("$date 00:00:00").time/1000
        val end:Long = 1+format.parse("$date 23:59:59").time/1000
        val route_occurance_threshold=5
        //TODO add configuration to change threshold above
        val array_of_averages = ArrayList<bus_average>()
        //Filter out routes with less than n occurances
        val patterns=HashMap<String,Int>()//KEY:Pattern type VALUE:occurrences
        //scan each occurrence of Route type
        for(i in 0 until recorded.size) {
            val count = patterns.get(recorded[i].patterntype)
            if (count == null) {
                patterns.put(recorded[i].patterntype, 1)
            } else {
                patterns.put(recorded[i].patterntype, count + 1)
            }
        }
        //sort the array by occurrences (value)
        val wanted_routes=ArrayList<String>()
        val sorted_patterns=patterns.toList().sortedBy { (_,value) -> value}.toMap().toList()
        println("Route Pattern occurrences:")
        sorted_patterns.forEach { i -> println(i) }
        sorted_patterns.forEach { i-> if(i.second > route_occurance_threshold){wanted_routes.add(i.first)} }
        println("Allowing routes: $wanted_routes (Above threshold of $route_occurance_threshold)")
        recorded.removeIf { i -> i.patterntype !in wanted_routes }//remove any routes that are not valid from wanted routes
        for(i in (start+resolution)..end step resolution){
            var inbound_count=0
            var outbound_count=0
            var inbound_total=0
            var outbound_total=0
            val timestamp="${format_output_date.format(Date((i-resolution)*1000))} - ${format_output_date.format(Date((i)*1000))}"
            recorded.forEach { k ->
                val bus_time = (format.parse(k.start_time).time/1000)
                if(bus_time >=i && bus_time < (i+resolution)){//found a bus in the timestamp range!
                    when{
                        k.direction.contains("In")->{
                            inbound_count++
                            inbound_total+=k.total_time
                        }
                        k.direction.contains("Out")->{
                            outbound_count++
                            outbound_total+=k.total_time
                        }
                    }
                }
            }
            val out_bound_avg=(outbound_total.toDouble()/outbound_count.toDouble()).toInt()
            val inbound_avg=(inbound_total.toDouble()/inbound_count.toDouble()).toInt()
            val inbound_avg_string=if(inbound_avg==0)"" else inbound_avg.toString()
            val outbound_avg_string=if(out_bound_avg==0)"" else out_bound_avg.toString()
            array_of_averages.add(bus_average(timestamp, inbound_avg_string,inbound_count, outbound_avg_string,outbound_count))
        }
        System.out.format("|%20s|%6s|%8s|%7s|%8s|\n","$route @ $date","In avg","In Node","Out avg","Out Node")
        array_of_averages.forEach { i-> System.out.format("|%20s|%6s|%8s|%7s|%8s|\n",i.timestamp,i.inbound_avg,i.inbound_nodes.toString(),i.outbound_avg,i.outbound_nodes.toString()) }
        File("output/times/$date $route.csv").printWriter().use{
            it.append("Bucket start,Inbound avg,Inbound nodes,Outbound avg,Outbound nodes\n")
            array_of_averages.forEach{ i -> it.append("${i.timestamp},${i.inbound_avg},${i.inbound_nodes},${i.outbound_avg},${i.outbound_nodes}\n") }
        }
    }
}