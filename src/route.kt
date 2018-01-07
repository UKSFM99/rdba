import java.io.File

class route(name:String){
    //store name of route here
    val name=name
    var array_of_sub_routes = HashMap<String,ArrayList<stops>>()
    val array_of_sub_routes_nom_mod = get_subroutes()

    //Function to get all sub routes under route name ie: X4 as JP56,JP58,JP59,JP60
    //Of which each route code has a different set of stops
    fun get_subroutes():HashMap<String,ArrayList<stops>>{
        val return_hash=HashMap<String,ArrayList<stops>>()
        //walk down the directory
        val all_files=File("output/routes/$name/").walkTopDown()
        all_files.forEach {
            //Found a file, scan it now
            if(it.isFile){
                val filler_array=ArrayList<stops>()
                var pos=0
                it.bufferedReader().forEachLine {
                    if(!it.contains("Stop")){//Do not use Header Line!
                        val data=it.split(',')
                        //add line entry from CSV to stop data class
                        filler_array.add(stops(LatLng(data[3].toDouble(),data[4].toDouble()),data[1],data[2],false,pos))
                        pos++
                    }
                }
                //check for Loop routes
                if(filler_array[0].name==filler_array[filler_array.lastIndex].name){
                    //TODO handle loop routes
                    color().printyellow("Warning, Found a loop route, this WILL cause issues")
                }
                //place sub route ie JP56 into the hashmap
                return_hash.put(it.name.replace(".csv",""),filler_array)
            }
        }
        //No routes found
        if(return_hash.isEmpty()) {
            color().printred("No route data for $name, please Download")
        }
        else{
            println("Found routes ${return_hash.keys} for $name")
        }
        array_of_sub_routes=return_hash
        return return_hash
    }

    fun findstops(location:LatLng):HashMap<String,Int>{
        if(!array_of_sub_routes_nom_mod.isEmpty()){
            val distance_stops=HashMap<String,Int>()
            array_of_sub_routes_nom_mod.forEach {
                it.value.forEach {
                    val data=distance_stops.get(it.name)
                    if(data == null) {
                        val dist = calc_distance(location, it.Location)
                        if(dist <=10) {
                            distance_stops.put(it.name, dist)
                        }
                    }
                }
            }
            if(distance_stops.isEmpty()){
                return hashMapOf()
            }
            return distance_stops
        }
        return hashMapOf()
    }
    //Calculate distance between 2 LatLngs - COPIED FROM TRACKED_BUS.KT
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

    //get list of routes containing stop and probability of that route
    fun get_routes_with_stop(stop_name:String):HashMap<String,Int>{//TODO return probability, not 100
        val return_hash=HashMap<String,Int>()
        array_of_sub_routes.forEach { name ->
            name.value.forEach {
                if(it.name==stop_name){return_hash.put(name.key,0)}
            }
        }
        val perc="%NAN%"//TODO proper percentage readout and calculation
        val to_delete_array=ArrayList<String>()
        array_of_sub_routes.forEach { base_id ->
            val data=return_hash[base_id.key]
            if(data == null){
                color().printpurple("=> Bus is not on route ${base_id.key}! - removing from list")
                to_delete_array.add(base_id.key)
            }
            else{
                color().printpurple("=> Bus can be on route ${base_id.key} - probability is $perc%")
            }
        }
        to_delete_array.forEach { array_of_sub_routes.remove(it) }//delete entries here - avoids concurrent modification exceptions
        return return_hash
    }
}