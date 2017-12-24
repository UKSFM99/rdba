
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
    private var location = Pair(LatLng(0.toDouble(),0.toDouble()),"")
    private var prev_location = Pair(LatLng(0.toDouble(),0.toDouble()),"")
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
                    println("Bus $id (Route $route) moved ${get_distance_traveled_m(location.first,prev_location.first)} m at ${get_speed(get_distance_traveled_m(location.first,prev_location.first), get_time_delta(location.second, prev_location.second))} mph")
                }
                else{
                    if(route_data.isNotEmpty()) {
                        route_data.forEach { i ->
                            val route_start=i.value[0]
                            val route_end=i.value[i.value.lastIndex]
                            i.value.forEach {
                                if (get_distance_traveled_m(location.first, it.Location) <= 10) {
                                    if(it.name == route_start.name){println("${text_color.ANSI_CYAN}Bus $id is at ${it.name} which is the start stop of route ${i.key.split(',')[0]}${text_color.ANSI_RESET}")}
                                    if(it.name == route_end.name){println("${text_color.ANSI_CYAN}Bus $id is at ${it.name} which is the end stop of route ${i.key.split(',')[0]}${text_color.ANSI_RESET}")}
                                    else {
                                        println("${text_color.ANSI_BLUE}Bus $id (Route $route(${i.key.split(',')[0]})) is at ${it.name} heading towards ${i.key.split(',')[1]}${text_color.ANSI_RESET}")
                                    }
                                }
                            }
                        }
                    }
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
            println("\n")
            System.out.println("""
                ---BUS INFO---
                FLEET NUMBER: ${metadata.ID}
                REG NUMBER:   ${metadata.Registration}
                DATE REGISTERED:${metadata.reg_date}
                MAX SEATED: ${metadata.Seated}
                MAX STANDING: ${metadata.Standing}
        """.trimIndent())
        }
        else{System.err.println("Warning. No Fleet data for ID $id")}
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
                            temp_array.add(stops(LatLng(arr[3].toDouble(),arr[4].toDouble()),arr[1],arr[2],arr[0].toInt()))
                        }
                    }
                }
                hash_output.put("${it.toString().replace("output/routes/$route/","").replace(".csv","")},${temp_array[temp_array.lastIndex].name}",temp_array)
            }
            catch (e:Exception){}
        }
        if(hash_output.isEmpty()){System.err.println("Cannot find any data for $route")}
        else {
            val route_list=ArrayList<String>()
            hash_output.keys.forEach { route_list.add(it.split(',')[0]) }
            print("Using routes $route_list for bus $id on route $route\n")
        }
        return hash_output
    }
}

