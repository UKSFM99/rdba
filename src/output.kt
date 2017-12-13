import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/*
    Class that holds multiple output functions, depending on data type
 */

class output {
    fun output_data(date:String,array:ArrayList<bus_info>,route:String){
        /*
            ROUTE DATA
         */
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
            val count = patterns[recorded[i].patterntype]
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
        sorted_patterns.forEach { (first, second) -> if(second > route_occurance_threshold){wanted_routes.add(first)} }
        println("Allowing routes: $wanted_routes (Above threshold of $route_occurance_threshold)")
        recorded.removeIf { i -> i.patterntype !in wanted_routes }//remove any routes that are not valid from wanted routes
        for(i in (start+resolution)..end step resolution){
            var inbound_count=0
            var outbound_count=0
            var inbound_total=0
            var outbound_total=0
            val timestamp="${format_output_date.format(Date((i-resolution)*1000))} - ${format_output_date.format(Date((i)*1000))}"
            recorded.forEach { (direction, total_time, _, start_time) ->
                val bus_time = (format.parse(start_time).time/1000)
                if(bus_time >=i && bus_time < (i+resolution)){//found a bus in the timestamp range!
                    when{
                        direction.contains("In")->{
                            inbound_count++
                            inbound_total+= total_time
                        }
                        direction.contains("Out")->{
                            outbound_count++
                            outbound_total+= total_time
                        }
                    }
                }
            }
            val out_bound_avg=(outbound_total.toDouble()/outbound_count.toDouble()).toInt()
            val inbound_avg=(inbound_total.toDouble()/inbound_count.toDouble()).toInt()
            val inbound_avg_string=if(inbound_avg==0)"" else inbound_avg.toString()
            val outbound_avg_string=if(out_bound_avg==0)"" else out_bound_avg.toString()
            array_of_averages.removeIf { k -> k.outbound_avg=="" && k.inbound_avg=="" }
            array_of_averages.add(bus_average(timestamp, inbound_avg_string,inbound_count, outbound_avg_string,outbound_count))
        }
        System.out.format("|%20s|%6s|%8s|%7s|%8s|\n","$route @ $date","In avg","In Node","Out avg","Out Node")
        array_of_averages.forEach { (timestamp, inbound_avg, inbound_nodes, outbound_avg, outbound_nodes) -> System.out.format("|%20s|%6s|%8s|%7s|%8s|\n", timestamp, inbound_avg, inbound_nodes.toString(), outbound_avg, outbound_nodes.toString()) }
        File("output/times/$route").mkdirs()
        File("output/times/$route/$date.csv").printWriter().use{
            it.append("Bucket start,Inbound avg,Inbound nodes,Outbound avg,Outbound nodes\n")
            array_of_averages.forEach{ (timestamp, inbound_avg, inbound_nodes, outbound_avg, outbound_nodes) -> it.append("${timestamp},${inbound_avg},${inbound_nodes},${outbound_avg},${outbound_nodes}\n") }
        }

        /*
            STOP DATA
         */
        File("output/stops/$route/$date").mkdirs()
        routes_details.forEach { (from_to, times) ->
            File("output/stops/$route/$date/${from_to.split(',')[0]}-${from_to.split(',')[1]}.csv").printWriter().use {
                it.append("TIMESTAMP,avg time,min time ,max time\n")
                times.forEach { k ->
                    it.append("${k.timestamp},${k.get_avg()},${k.data[2]},${k.data[3]}\n")
                }
            }
        }

        /*
            TIME DATA
         */
        File("output/routes/$route").mkdirs()
        routes.forEach { (pattern, stops_list) ->
            File("output/routes/$route/${pattern}.csv").printWriter().use {
                it.append("Stop number,Stop Name,Stop UUID,Stop Location Lat,Stop Location Long\n")
                var stop_id = 0
                stops_list.forEach { (Location, name, UUID) ->
                    it.append("$stop_id,${name},${UUID},${name},${Location.Latitude},${Location.Longitude}\n")
                    stop_id++
                }
            }
        }
        /*
            Generate a daily report
         */
        //get daily route stop times
        val temp=ArrayList<ArrayList<Any>>()
        routes_details.forEach { (from_to, times) ->
            var count=0
            var min=1000
            var max=0
            var total=0
            times.forEach { k ->
                count+=k.data[0]
                total+=k.data[1]
                max = if(k.data[2] > max) k.data[2] else max
                min = if(k.data[3] < min) k.data[3] else min
            }
            temp.add(arrayListOf(from_to,count,total,min,max))
        }
        //generate route trip data
        val route_arr = ArrayList<ArrayList<Any>>()
        sorted_patterns.forEach { (first) ->
            var dir=""
            var total_time=0
            var hits=0
            var min=1000000
            var max=0
            recorded.forEach { i ->
                dir=i.direction
                if(i.patterntype== first){
                    total_time+=i.total_time
                    hits++
                    min=if(i.total_time < min) i.total_time else min
                    max=if(i.total_time > max) i.total_time else max
                }
                else{
                }
            }
            if(max!=0){ route_arr.add(arrayListOf(first,"($dir)",hits,(total_time.toDouble()/hits.toDouble()).toInt(),min,max)) }
        }
        temp.forEachIndexed { index,_ ->
            try{
                route_arr[index]
            }
            catch(e:Exception){
                route_arr.add(arrayListOf("","","","","",""))
            }
        }
        println("Generating Report")
        File("output/reports/$route").mkdirs()
        File("output/reports/$route/$date.csv").printWriter().use {
            it.append("ALL TIME IN SECONDS - Some data may not be 100% accurate\n\n")
            it.append("Stops,avg travel time,min travel time,max travel time,,,Route code+Direction,Avg completion time,Min completion time,Max completion time,number of occurrences\n")
            temp.forEachIndexed { index,i ->
                it.append("${i[0].toString().split(',')[0]} -> ${i[0].toString().split(',')[1]},${(i[2].toString().toInt().toDouble()/i[1].toString().toInt().toDouble()).toInt()},${i[3]},${i[4]},,,${route_arr[index][0]} ${route_arr[index][1]},${route_arr[index][3]},${route_arr[index][4]},${route_arr[index][5]},${route_arr[index][2]}\n")
            }
        }
    }
}