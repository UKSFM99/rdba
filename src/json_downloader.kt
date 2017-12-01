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
    private var array= arrayListOf<String>()
    private var array_split= arrayListOf<String>()
    override fun run(){
        val array_finished=ArrayList<bus_info>()
        try {
            println("Begin download for route: $route for date: $date")
            var read = URL(url).readText()
            read = read.substring(1, read.length - 1)//remove [ and ] from sting
            array.addAll(read.split("},{"))
            for (i in array) {
                array_split.clear()
                array_split.addAll(i.split(','))
                try {
                    if(return_element("\"ArrivalTime\"")=="" && return_element("\"DepartureTime\"")==""){continue}
                   val info=bus_info(return_element("\"LineRef\""),
                            return_element("\"LocationCode\""),
                            return_element("\"LocationName\""),
                            return_element("\"JourneyType\""),
                            return_element("\"LiveJourneyId\""),
                            return_element("\"Sequence\""),
                            return_element("\"RunningBoard\""),
                            return_element("\"Duty\"").toInt(),
                            return_element("\"Direction\""),
                            return_element("\"JourneyCode\"").toInt(),
                            return_element("\"VehicleCode\""),
                            return_element("\"DriverCode\""),
                            !return_element("\"TimingPoint\"").contains("Non"),
                            return_element("\"JourneyPattern\""),
                            return_element("\"NumberStops\"").toInt(),
                            return_element("\"StartPoint\""),
                            return_element("\"EndPoint\""),
                            Array<Double>(2) { return_element("\"Latitude\"").toDouble();return_element("\"Longitude\"").toDouble() },
                            return_element("\"ScheduledArrivalTime\""),
                            return_element("\"ArrivalTime\""),
                            return_element("\"ScheduledDepartureTime\""),
                            return_element("\"DepartureTime\"")
                    )
                    //println(info)
                    array_finished.add(info)
                }catch(e:Exception){e.printStackTrace()}
            }
            output().output_averages(date,array_finished,route)
        }finally {
            System.out.println("Finished thread for route $route at $date!")
        }
    }
    private fun return_element(query:String):String{
        val pos:Int?=search_for_index(query)
        val temp = array_split[pos!!].split(':')
        var index = temp[1].replace("\"", "")
        try {
            index += ":${temp[2]}:${temp[3].replace("\"", "")}"
        } catch (e: Exception) {
        }//try to re-join date string
        return index
    }
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