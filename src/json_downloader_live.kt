import java.net.URL
import java.net.UnknownHostException
import kotlin.system.exitProcess

class json_downloader_live :Runnable {
    private var array_split=ArrayList<String>()
    companion object {
        val bus_group=HashMap<String,tracked_bus>()
    }
    override fun run() {
        try {
            var read = URL("http://siri.buscms.com/reading/vehicles.json").readText()
            read = read.substring(2, read.length - 2)//remove [{ and }] from string
            val array = read.split("},{")
            val data = ArrayList<bus_live>()
            data.clear()
            for (i in array) {
                array_split.clear()
                array_split.addAll(i.split(','))
                if (!return_element("\"service\"").isEmpty()) {
                    data.add(bus_live(
                            return_element("\"vehicle\""),
                            return_element("\"service\""),
                            if (return_element("\"bearing\"") == "") -1 else return_element("\"bearing\"").toInt(),
                            LatLng(return_element("\"latitude\"").toDouble(), return_element("\"longitude\"").toDouble()),
                            return_element("\"observed\"")
                    ))
                }
            }
            if (data.isEmpty()) {
                color().printred("There are no Buses currently running!")
                exitProcess(1)
            }
            data.forEach { (id, service, heading, location, timestamp) ->
                val data = bus_group[id!!]
                if (data == null) {
                    System.err.println("Did not find bus $id, adding it")
                    bus_group.put(id, tracked_bus(id, service))
                    bus_group.getValue(id).update_location(Pair(location, timestamp),heading)
                } else {
                    data.update_location(Pair(location, timestamp),heading)
                }
            }
        }
        catch (e: UnknownHostException){println("Cannot reach tracking URL!")}
    }
    //return the value at index in JSON
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