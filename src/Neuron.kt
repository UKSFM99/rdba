
import com.sun.xml.internal.ws.util.StringUtils
import java.io.FileReader
import java.io.BufferedReader
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList


/*
    TBA for actual function
 */

class Neuron(val id:String,val route:String) {
    val metadata:bus_specs.bus_spec_sheet
    var route_data=HashMap<String,ArrayList<stops>>()
    private var updates_without_change=0
    var location = Pair(LatLng(0.toDouble(),0.toDouble()),"")
    private var prev_location = Pair(LatLng(0.toDouble(),0.toDouble()),"")
    var curr_stop=""
    private var prev_stop=""
    init{
        this.metadata = bus_specs().get_specs(id)
        this.route_data = get_all_routes()
    }
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private fun get_time_delta(time_now:String,time_before:String):Int = ((format.parse(time_now).time/1000)-(format.parse(time_before).time/1000)).toInt()

    //Sigmoid function. Take any value an return a number from 0-1 - suitable for Neuron weight when giving a probability of a route
    fun sigmoid(input:Double): Double = (1/(1+Math.pow(Math.E,(-1*input))))

    fun update_location(input:Pair<LatLng,String>){
        if(input.second != location.second) {
            if(updates_without_change >=3){
                println("${text_color.ANSI_GREEN}Bus $id (Route $route) finally spoke to earth! (Therefore its not an abduction)${text_color.ANSI_RESET}")
            }
            updates_without_change=0
            prev_location = location
            location = input
            if (prev_location.first != LatLng(0.toDouble(), 0.toDouble())) {
                if (location.first != prev_location.first && get_speed(get_distance_traveled_m(location.first,prev_location.first),get_time_delta(location.second, prev_location.second)) >=10) {
                    console.println("Bus $id (Route $route) moved ${get_distance_traveled_m(location.first,prev_location.first)} m at ${get_speed(get_distance_traveled_m(location.first,prev_location.first), get_time_delta(location.second, prev_location.second))} mph")
                }
                else{
                    var found_match=false
                    var same_location=true
                    if(route_data.isNotEmpty()) {
                        route_data.forEach {
                            if(found_match){return@forEach}//break out of forEach functional loop
                            it.value.forEach {
                                if(get_distance_traveled_m(it.Location,location.first) <=10){
                                    found_match=true
                                    if(it.name != curr_stop) {
                                        prev_stop = curr_stop
                                        curr_stop = it.name
                                        same_location=false
                                    }
                                }
                            }
                        }
                        //bus is at a stop, see if its the end of the line
                        route_data.forEach {
                            if(it.key.split(',')[1]==curr_stop){
                                route_data.put("${it.key.split(',')[0]},${it.key.split(',')[1]},E",it.value)
                            }
                        }
                        if(!same_location&&found_match) {
                            val route_name_array=ArrayList<String>()
                            route_data.forEach { i ->
                                i.value.forEach {
                                    if(it.name == curr_stop){route_name_array.add(i.key)}
                                }
                            }
                            //print pretty table
                            var max=0 //max length of table entry
                            var header=" Possible routes for bus $id at $curr_stop "
                            val route_entry_format="%1 (%2) -> %3 STATUS:%4"//%1=route %2=route code eg:JPxx %3=destination
                            max=header.length
                            route_name_array.forEach {//get longest string
                                val route_entry_string=route_entry_format.replace("%1",route)
                                        .replace("%2",it.split(',')[0])
                                        .replace("%3", it.split(',')[1])
                                        .replace("%4",if(it.split(',')[2]=="I") "INCLUDED" else "EXCLUDED")
                                max=if(route_entry_string.length > max) route_entry_string.length else max
                            }
                            val line="+${"-".repeat(max)}+"
                            console.println("${text_color.ANSI_PURPLE}$line")
                            var diff=max-header.length
                            console.println("|${" ".repeat((diff.toDouble()/2).toInt())}$header${" ".repeat(((diff.toDouble()/2)+0.5).toInt())}|")
                            console.println(line)
                            route_name_array.forEach {
                                val str=route_entry_format.replace("%1",route)
                                        .replace("%2",it.split(',')[0])
                                        .replace("%3", it.split(',')[1])
                                        .replace("%4", if(it.split(',')[2]=="I") "INCLUDED" else "EXCLUDED")
                                diff=max-str.length
                                console.println("|$str${" ".repeat(diff)}|")
                            }
                            console.println("$line${text_color.ANSI_RESET}")
                        }
                        if(same_location&&found_match){console.println("${text_color.ANSI_BLUE}!!Bus $id is Still at $curr_stop!!${text_color.ANSI_RESET}")}
                    }
                    if(!found_match){console.println("Bus $id might be stuck in traffic at (${location.first.Latitude},${location.first.Longitude})")}
                }
            }
        }
        else{
            updates_without_change++
            if(updates_without_change == 3){
                println("${text_color.ANSI_YELLOW}Bus (Route $route) $id did not update for ${updates_without_change*10} seconds - could be an abduction!${text_color.ANSI_RESET}")
            }
            if(updates_without_change == 6){
                println("${text_color.ANSI_RED}Bus (Route $route) $id did not update for ${updates_without_change*10} seconds - must be an abduction!${text_color.ANSI_RESET}")
            }
        }
    }
    fun print_specs(){
        if(metadata.ID!="N/A") {
            console.println("${text_color.ANSI_BLUE}I found this:")
            console.println("""
                ---BUS INFO---
                FLEET NUMBER: ${metadata.ID}
                REG NUMBER:   ${metadata.Registration}
                DATE REGISTERED:${metadata.reg_date}
                MAX SEATED: ${metadata.Seated}
                MAX STANDING: ${metadata.Standing}${text_color.ANSI_RESET}
        """.trimIndent())
            console.println("\n")
        }
        else{System.err.println("Sorry, I have no fleet data for Bus $id")}
    }
    fun get_distance_traveled_m(loc1:LatLng,loc2:LatLng):Int{
        val Rad = 6371.0 //Earth's Radius In kilometers
        // TODO Auto-generated method stub
        val dLat = Math.toRadians(loc1.Latitude - loc2.Latitude)
        val dLon = Math.toRadians(loc1.Longitude - loc2.Longitude)
        val lat1 = Math.toRadians(loc2.Latitude)
        val lat2 = Math.toRadians(loc1.Latitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
        val c = 2 * Math.asin(Math.sqrt(a))
        val haverdistanceKM = Rad * c
        return (haverdistanceKM*1000).toInt()
    }
    fun get_speed(distance:Int,time:Int):Long = ((distance.toDouble()/time.toDouble())*2.23694).toLong()

    fun get_all_routes():HashMap<String,ArrayList<stops>>{
        val hash_output=HashMap<String,ArrayList<stops>>()
        File("output/routes/$route/").walk().forEach {
            val temp_array=ArrayList<stops>()
            try{
                it.inputStream().bufferedReader().useLines { lines ->
                    lines.forEach { i ->
                        if(!i.contains("Stop")){
                            val arr=i.split(',')
                            temp_array.add(stops(LatLng(arr[3].toDouble(),arr[4].toDouble()),arr[1],arr[2],false,arr[0].toInt()))
                        }
                    }
                }
                if(temp_array[0] != temp_array[temp_array.lastIndex]) {
                    hash_output.put("${it.toString().replace("output/routes/$route/", "").replace(".csv", "")},${temp_array[temp_array.lastIndex].name},I", temp_array) //I=included
                }
                else{
                    //TODO Handle loop route codes
                    println("${text_color.ANSI_RED}Help! Found a loop route \"${it.toString().replace("output/routes/$route/", "").replace(".csv", "")}\" and don't know how to handle it, ignoring${text_color.ANSI_RED}")
                }
            }
            catch (e:Exception){}
        }
        if(hash_output.isEmpty()){System.err.println("Cannot find any data for $route")}
        else {
            val route_list=ArrayList<String>()
            //TODO remove all
            hash_output.keys.forEach { route_list.add(it.split(',')[0]) }
            print("Using routes $route_list for bus $id on route $route\n")
        }
        return hash_output
    }
}

