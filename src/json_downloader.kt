import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
/*
    Class that holds JSON downloader for the analytics part of the program
 */
class json_downloader(url:String,route:String,date:String):Runnable {
    private val route=route
    private val url=url
    private val date=date
    private var array_split=ArrayList<String>()
    override fun run(){
        val array_finished=ArrayList<bus_info>()
        try {
            println("Begin download for route: $route for date: $date")
            var read = URL(url).readText()
            read = read.substring(1, read.length - 1)//remove [ and ] from string
            val array = read.split("},{")
            for (i in array) {
                array_split.clear()
                array_split.addAll(i.split(','))
                try {
                    //If actual dep/arr time is blank then use scheduled
                    var actual_arrival_time = ""
                    var actual_departure_time = ""
                    actual_arrival_time = try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(return_element("\"ArrivalTime\""))
                        return_element("\"ArrivalTime\"")
                    } catch (e: Exception) {
                        return_element("\"ScheduledArrivalTime\"")
                    }
                    actual_departure_time = try {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(return_element("\"DepartureTime\""))
                        return_element("\"DepartureTime\"")
                    } catch (e: Exception) {
                        return_element("\"ScheduledDepartureTime\"")
                    }
                    //Add JSON information to each variable as a data class array
                    val info = bus_info(return_element("\"LineRef\""),
                            return_element("\"LocationCode\""),
                            return_element("\"LocationName\""),
                            return_element("\"JourneyType\""),
                            return_element("\"LiveJourneyId\""),
                            return_element("\"Sequence\""),
                            return_element("\"RunningBoard\""),
                            return_element("\"Duty\""),
                            return_element("\"Direction\""),
                            return_element("\"JourneyCode\""),
                            return_element("\"VehicleCode\""),
                            return_element("\"DriverCode\""),
                            !return_element("\"TimingPoint\"").contains("Non"),
                            return_element("\"JourneyPattern\""),
                            return_element("\"NumberStops\"").toInt(),
                            return_element("\"StartPoint\""),
                            return_element("\"EndPoint\""),
                            LatLng(return_element("\"Latitude\"").toDouble(), return_element("\"Longitude\"").toDouble()),
                            return_element("\"ScheduledArrivalTime\""),
                            actual_arrival_time,
                            return_element("\"ScheduledDepartureTime\""),
                            actual_departure_time
                    )
                    array_finished.add(info)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            println("Finished downloading $route at $date")
            output().output_data(date, array_finished, route)
        }
        finally {
            System.out.println("Finished thread for route $route at $date!")
        }
    }
    //return the value at index in JSON
    private fun return_element(query:String):String{
        try {
            val pos: Int? = search_for_index(query)
            val temp = array_split[pos!!].split(':')
            var index = temp[1].replace("\"", "")
            try {
                index += ":${temp[2]}:${temp[3].replace("\"", "")}"
            } catch (e: Exception) {
            }//try to re-join date string
            return index
        }
        catch (e:KotlinNullPointerException){ throw Exception("Bus route invalid! - Halting download of $route") }
    }
    //get index of an array of query sting
    private fun search_for_index(term:String):Int?{
        var n=0
        for(i in array_split){
            if(i.contains(term)){
                return n
            }
            else{
                n++
            }
        }
        return null
    }
}