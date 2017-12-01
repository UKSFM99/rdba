import java.io.File
import java.text.SimpleDateFormat
import java.util.*
/*
    Class that holds multiple output functions, depending on data type
 */

class output {
    fun output_averages(date:String,array:ArrayList<bus_info>,route:String){
        val recorded=analyze().analyze_time_deltas(array)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val start:Long = format.parse("$date 00:00:00").time/1000
        val end:Long = 1+format.parse("$date 23:59:59").time/1000
        val resolution:Long=1*60*60//hour
        val array_of_averages = ArrayList<bus_average>()
        for(i in (start+resolution)..end step resolution){
            var inbound_count=0
            var outbound_count=0
            var inbound_total=0
            var outbound_total=0
            var num_of_failures=0
            var total_stop_count=0
            var timestamp="${format.format(Date((i-resolution)*1000))} - ${format.format(Date((i)*1000))}"
            for(k in recorded){
                val bus_time = (format.parse(k.start_time).time/1000)
                if(bus_time >=i && bus_time < (i+resolution)){//found a bus in the timestamp range!
                    if(k.direction.contains("In")){
                        inbound_count++
                        inbound_total+=k.total_time
                        num_of_failures+=k.number_based_schedules
                        total_stop_count+=k.array.size
                    }
                    if(k.direction.contains("Out")){
                        outbound_count++
                        outbound_total+=k.total_time
                        num_of_failures+=k.number_based_schedules
                        total_stop_count+=k.array.size
                    }
                }
            }
            val out_bound_avg=(outbound_total.toDouble()/outbound_count.toDouble()).toInt()
            val inbound_avg=(inbound_total.toDouble()/inbound_count.toDouble()).toInt()
            val inbound_avg_string=if(inbound_avg==0) "" else inbound_avg.toString()
            val outbound_avg_string=if(out_bound_avg==0)"" else out_bound_avg.toString()
            val accuracy=(100-(num_of_failures.toDouble()/total_stop_count.toDouble())).toInt()
            array_of_averages.add(bus_average(timestamp, inbound_avg_string,inbound_count, outbound_avg_string,outbound_count,accuracy))
        }
        System.out.format("|%41s|%6s|%8s|%7s|%8s|%8s|\n","Time frame for route $route","In avg","In Node","Out avg","Out Node","Accuracy")
        for(i in array_of_averages){
            System.out.format("|%41s|%6s|%8s|%7s|%8s|%8s|\n",i.timestamp,i.inbound_avg,i.inbound_nodes.toString(),i.outbound_avg,i.outbound_nodes.toString(),"${i.accuracy}%")
        }
        File("$date $route.csv").printWriter().use{
            it.append("Bucket start,Inbound avg,Inbound nodes,Outbound avg,Outbound nodes\n")
            for(i in array_of_averages){
                it.append("${i.timestamp},${i.inbound_avg},${i.inbound_nodes},${i.outbound_avg},${i.outbound_nodes}\n")
            }
        }
    }
}