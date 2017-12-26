import java.awt.FlowLayout
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextArea

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
        while(true){
            val query=JOptionPane.showInputDialog("Enter query")
            if(query=="help"){
                JOptionPane.showMessageDialog(null,"No help information yet")
            }
            else if(query=="exit"||query=="stop"){
                break
            }
            else{
                JOptionPane.showMessageDialog(null,"Cannot recognise query \"$query\"")
            }
        }
    }
}