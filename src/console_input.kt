import java.util.*

class console_input : Thread(){
    val sc= Scanner(System.`in`)
    override fun run(){
        while(true) {
            val input = sc.nextLine()
            if (input == "help") {
                TODO("I dont have a help function yet")
            }
            else {
                val cmd_qry = input.split(' ').toTypedArray()
                if (cmd_qry[0] == "query") {
                    if (cmd_qry[1] == "specs") {
                        if (cmd_qry[2] in json_downloader_live.bus_group.keys) {
                            json_downloader_live.bus_group.getValue(cmd_qry[2]).print_specs()
                        }
                    } else if (cmd_qry[1] == "location")
                        if (cmd_qry[2] in json_downloader_live.bus_group.keys) {
                            if (json_downloader_live.bus_group.getValue(cmd_qry[2]).location.first != LatLng(0.0, 0.0)) {
                                val loc = json_downloader_live.bus_group.getValue(cmd_qry[2]).location.first
                                println("Bus ${cmd_qry[2]} is at ${loc.Longitude},${loc.Latitude}")
                            } else {
                                println("I haven't got a location lock on Bus ${cmd_qry[2]}, sorry")
                            }
                        }
                } else if (cmd_qry[1] == "list") {
                    TODO("needs to work out how to generate list")
                } else {
                    println("${text_color.ANSI_RED}Sorry, I don't know how to handle your query \"$input\"")
                    println("Type \"help\" to see what I can do${text_color.ANSI_RESET}")
                }
            }
        }
    }
}