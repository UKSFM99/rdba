import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

// Each bus being tracked is stored here - As well as all its related information
class tracked_bus(val id:String, val route_id:String) {
    private val time_format=SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    //Store bus specifications
    private val specs=bus_specs().get_specs(id)
    //return specs
    fun get_specs():bus_specs.bus_spec_sheet=specs

    //Store bus location in Pair(Latlng(),Timestamp)
    private var location_now = Pair(LatLng(0.0,0.0),"") //Current location
    private var location_prev = Pair(LatLng(0.0,0.0),"")//Previous location
    private var curr_heading=0

    //Route information
    private val routes=route(route_id)
    //Store current and last known stop the bus was at
    private var current_stop=HashMap<String,Int>()
    private var last_known_stop=HashMap<String,Int>()
    private var arrival_time=""
    private var departure_time=""
    //TODO work on what we do if bus is stationary
    fun stationary(location:LatLng){
        val possible_stops=routes.findstops(location)
        var is_still_at_stop=false
        //need to check both previous and current stop. This is because sometimes the bus is on a boundary and fluctuates between in and out of a stops range
        possible_stops.forEach { if(current_stop[it.key] !=null ||last_known_stop[it.key]!=null) is_still_at_stop=true }//see if we could be at a previous stop
        if(!is_still_at_stop) {
            if (!possible_stops.isEmpty()) {
                arrival_time=location_now.second
                if(!last_known_stop.isEmpty()){
                    val calculaed_travel_time=calc_timedelta(time_format.parse(arrival_time),time_format.parse(departure_time))
                    color().printgreen("Bus $id traveled from ${last_known_stop.keys} to ${possible_stops.keys}, took ~$calculaed_travel_time seconds")
                    val missed_stops=routes.get_missed_stops(last_known_stop.keys.first(),possible_stops.keys.first())
                    if(missed_stops.isEmpty()){color().printgreen("Bus did not miss any stops")}
                    else{
                        color().printred("Bus missed stops:")
                        missed_stops.forEach {
                            color().printred("=> Route ${it.key} ==> missed ${it.value}")
                        }
                    }

                }
                color().printblue("Bus $id could be at stop(s) ${possible_stops.keys} with distances of ${possible_stops.values} meters")
                possible_stops.forEach {
                    val routes = routes.get_routes_with_stop(it.key)
                    if(routes.size == 1){
                        //HORRAY! FOUND A DEFINATE ROUTE
                        color().printcyan("HORRAY! Bus $id is 100% on route ${routes.keys}")
                        //TODO Handle timetable serving
                    }
                }
                current_stop = possible_stops
            }
            else{
                if(!current_stop.isEmpty()) {
                    last_known_stop.clear()
                    color().printgreen("Bus $id left ${current_stop.keys} at ~${location_now.second}")
                    departure_time=location_now.second
                    color().printgreen("Bus $id was at ${current_stop.keys} for ~${calc_timedelta(time_format.parse(departure_time),time_format.parse(arrival_time))} seconds")
                    last_known_stop.putAll(current_stop)
                    current_stop.clear()
                }
            }
        }
        else if(!current_stop.isEmpty()){
            color().printgreen("Bus $id is still at ${current_stop.keys}")
        }
    }

    //whenever we refresh all buses this is called to check if the bus has updated or moved
    fun update_location(new_location:Pair<LatLng,String>,heading:Int){
        //update bus direction (Heading) - ignore invalid heading (AKA -1)
        if(heading!=-1){ curr_heading=heading }
        //check if we got no update
        if(new_location==location_now){
        }
        //check if we got update but location did not change
        else if(new_location!=location_now && new_location.first == location_now.first){
            stationary(new_location.first)
        }
        //assume bus moved
        else{
            location_prev=location_now
            location_now=new_location
            //see if location_prev is still at its initial valid.
            //if it is then we don't do any analysis on location_now and location_prev
            if(location_prev.first==LatLng(0.0,0.0)){}
            else{
                val dist_traveled=calc_distance(location_now.first,location_prev.first)
                if(dist_traveled<=5){stationary(location_now.first)}
                else{
                    val time_delta=calc_timedelta(time_format.parse(location_now.second),time_format.parse(location_prev.second))
                    val speed=calc_speed(time_delta,dist_traveled)
                    val est_heading=calc_heading(location_now.first,location_prev.first)
                    if(speed <=5){
                        stationary(location_now.first)
                    }
                    else{println("Bus $id traveled at $speed mph for $time_delta seconds")}
                }
            }
        }
    }

    //Calculate distance between 2 LatLngs
    fun calc_distance(loc1:LatLng,loc2:LatLng):Int {
        val dLat = Math.toRadians(loc1.Latitude - loc2.Latitude)
        val dLon = Math.toRadians(loc1.Longitude - loc2.Longitude)
        val lat1 = Math.toRadians(loc2.Latitude)
        val lat2 = Math.toRadians(loc1.Latitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2)
        val c = 2 * Math.asin(Math.sqrt(a))
        val haverdistanceKM = 6371.0 * c
        return (haverdistanceKM * 1000).toInt()
    }
    //take 2 dates and calculate delta in seconds
    fun calc_timedelta(time_now:Date,time_prev:Date):Int=((time_now.time-time_prev.time)/1000).toInt()

    //take time interval and distance and get avg speed
    fun calc_speed(time_delta:Int,distance_m:Int):Int=(2.23694*(distance_m.toDouble()/time_delta.toDouble())).toInt()

    //calculate initial heading from loc_prev to loc_now
    fun calc_heading(loc_now:LatLng,loc_prev:LatLng):Int{
        val c = 40075016.6856
        val diff_lat=c*(loc_now.Latitude-loc_prev.Latitude)/360
        val diff_lon=c*(loc_now.Longitude-loc_prev.Longitude)/360 * Math.cos(loc_now.Latitude-loc_prev.Latitude)
        val a=diff_lon/diff_lat
        var heading = Math.toDegrees(Math.atan(a)).toInt()
        if(heading < 0){
            heading=360-heading
        }
        if(heading > 360){
            heading=0+(heading-360)
        }
        return heading
    }
}