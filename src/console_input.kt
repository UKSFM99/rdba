
import java.awt.FlowLayout
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.*

class term_window(title:String) : Runnable{
    private val title=title
    private val frame=JFrame(title)
    private val outputArea=JTextArea(30,100)
    init {
        outputArea.isEditable = false
    }
    fun println(msg:String){
        outputArea.append("$msg\n")
    }
    fun print(msg:String){
        outputArea.append(msg)
    }
    fun println(t:Throwable){
        this.println(t.toString())
    }
    fun printStackTrace(t:Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        this.println(sw.toString())
    }
    override fun run(){
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        }catch (e:Exception){}
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val outputpanel=JPanel(FlowLayout())
        outputpanel.add(outputArea)
        frame.add(outputpanel)
        frame.pack()
        frame.isVisible = true
    }
}

class query_window :Runnable{
    override fun run(){
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        }catch (e:Exception){}
        while(true){
            val query=JOptionPane.showInputDialog("Enter query")
            if(query!=null) {
                if (query == "help") {
                    //JOptionPane.showMessageDialog(null,"No help information yet")
                    JOptionPane.showMessageDialog(null, """
                    USAGE:
                    query specs (ID):queries specification of bus ID
                    list buses: lists all Bus ID's currently running
                    list routes: lists all routes currently active, with number of buses on each route
                """.trimIndent())
                } else if (query == "exit" || query == "stop") {
                    break
                } else if (query.contains("query")) {
                    val query_cmd = query.split(' ')
                    if (query_cmd[1] == "specs") {
                        val bus_id = query_cmd[2]
                        if (bus_id in json_downloader_live.bus_group.keys) {
                            val specs = json_downloader_live.bus_group[bus_id]!!.get_specs()
                            if (specs.ID != "N/A") {
                                val specs_array = specs.toString().replace(',', '\n').replace(")", "").replace("(", "").replace("bus_spec_sheet", "")
                                JOptionPane.showMessageDialog(null, specs_array)
                            } else {
                                JOptionPane.showMessageDialog(null, "Sorry, Bus \"$bus_id\" does exist but I have no fleet data for it")
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Sorry, Bus \"$bus_id\" not found at the moment")
                        }
                    }
                } else if (query.contains("list")) {
                    val list_cmd = query.split(' ')
                    if (list_cmd[1] == "buses") {
                        val bus_list = json_downloader_live.bus_group.keys.toString()
                        val str = bus_list.replace(",", "\n").replace(")", "").replace("(", "")
                        JOptionPane.showMessageDialog(null, str)
                    }
                    if (list_cmd[1] == "routes") {
                        val route_count = HashMap<String, Int>()
                        json_downloader_live.bus_group.forEach {
                            val data = route_count.get(it.value.route_id)
                            if (data != null) {
                                route_count.put(it.value.route_id, data + 1)
                            } else {
                                route_count.put(it.value.route_id, 1)
                            }
                        }
                        JOptionPane.showMessageDialog(null, route_count.toString().replace(',', '\n').replace("{", "").replace("}", "").replace("=", " "))
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Cannot recognise query \"$query\"")
                }
            }
        }
    }
}