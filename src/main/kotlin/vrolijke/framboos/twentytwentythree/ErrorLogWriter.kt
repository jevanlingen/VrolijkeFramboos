import java.io.IOException
import java.text.SimpleDateFormat
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.File
import java.util.*

val dateFormat = SimpleDateFormat("HH:mm:ss")

fun appendLog(text: String) {
    try {
        val logFile = File("/home/jacobvanlingen", "VrolijkeFramboosLog.txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        //BufferedWriter for performance, true to set append to file flag
        val buf = BufferedWriter(FileWriter(logFile, true))
        buf.append(dateFormat.format(Date()))
        buf.append(" - ")
        buf.append(text)
        buf.newLine()
        buf.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}