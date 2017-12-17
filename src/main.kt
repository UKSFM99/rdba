import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

fun main(args: Array<String>) {
    println("Enter mode")
    println("1. Live tracking mode")
    println("2. Historical data mode")
    val input= readLine()
    val is_in_track_mode=if(input=="1") true else if(input=="2") false else throw Exception("Invalid input \"$input\"")
    if(is_in_track_mode){
        println("Real time track mode")
        val now=Date()
        val format=SimpleDateFormat("yyyy-MM-dd").format(now)
        println("Tracking for $format")
        System.err.println("WARNING: Method is incomplete, bugs are present")
        Timer().scheduleAtFixedRate(timerTask{
            val thread = Thread(json_downloader_live())
            thread.start()
        },0,10000)
    }
    else {
        println("Historical data mode")
        println("enter routes, separated by ','")
        val route = readLine().toString()
        println("enter dates in format YYYY-MM-DD, separated by ','")
        val date = readLine().toString()
        val today=SimpleDateFormat("yyyy-MM-dd").format(Date())
        //Check input dates against current date - if match then throw Exception as we cannot run historical mode with current day's data!
        if(date.contains(today)){throw Exception("Cannot enter Today's date in historical mode!, please use Live mode")}
        val current_route = route.split(',')
        val current_date = date.split(',')
        val thread_array = ArrayList<Thread>()
        //Launch each date and route combination as a separate Thread (+50% performance gain)
        //TODO launch AT MAXIMUM the number of system threads at any given time for best performance
        for (k in current_date) {
            for (i in current_route) {
                val url = "http://rtl2.ods-live.co.uk/api/trackingHistory?key=${File("api.key").readText()}&service=$i&date=$k&vehicle=&location="
                val thread = Thread(json_downloader(url, i, k))
                thread.start()
                thread_array.add(thread)
            }
        }
    }
}