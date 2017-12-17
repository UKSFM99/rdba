import java.net.URL

class json_downloader_live():Runnable {
    private var array_split=ArrayList<String>()
    companion object {
        val bus_group=HashMap<String,Neuron>()
    }
    override fun run() {
        var read = URL("http://siri.buscms.com/reading/vehicles.json").readText()
        read = read.substring(2, read.length - 2)//remove [{ and }] from string
        val array = read.split("},{")
        val data=ArrayList<bus_live>()
        data.clear()
        for (i in array) {
            array_split.clear()
            array_split.addAll(i.split(','))
            if (!return_element("\"service\"").isEmpty()) {
                data.add(bus_live(
                        return_element("\"vehicle\""),
                        return_element("\"service\""),
                        if (return_element("\"bearing\"") == "") null else return_element("\"bearing\"").toInt(),
                        LatLng(return_element("\"latitude\"").toDouble(), return_element("\"longitude\"").toDouble()),
                        return_element("\"observed\"")
                ))
            }
        }
        data.forEach { i ->
            val data= bus_group.get(i.id!!)
            if(data == null){
                System.err.println("Did not find ${i.id}, adding it")
                bus_group.put(i.id,Neuron(i.id,i.service))
            }
            else{
                data.update_location(Pair(i.location,i.timestamp))
            }
        }
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