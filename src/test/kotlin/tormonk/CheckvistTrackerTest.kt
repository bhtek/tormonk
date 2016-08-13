package tormonk

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.jdom2.input.SAXBuilder
import org.jonnyzzz.kotlin.xml.bind.jdom.JDOM
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.*
import java.util.*
import kotlin.concurrent.thread

class CheckvistTrackerTest {
    @Test
    fun getLastUpdatedTime_simple() {
        FuelManager.instance.client = object : Client {
            override fun executeRequest(request: Request): Response {
                return Response().apply {
                    url = request.url
                    httpStatusCode = 200
                    data = IOUtils.toByteArray(javaClass.getResourceAsStream("/getLastUpdatedTime_simple.json"))
                }
            }
        }

        Fuel.testMode()

        val tracker = CheckvistTracker()
        val allTasks = tracker.getAllTasks()
        assertThat(allTasks, notNullValue())
        assertThat(tracker.getLastUpdateTime(allTasks!!), equalTo(123L))
    }
}

fun main(args: Array<String>) {
    val jsch = JSch()
    jsch.addIdentity("${System.getProperty("user.home")}/.ssh/id_rsa")
    val session = jsch.getSession("101.100.161.164")
    val config = Properties()
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);
    session.connect()
    val channel = session.openChannel("shell")

    val inputStream = PipedInputStream()
    val pin = PipedOutputStream(inputStream)

    val outputStream = PipedOutputStream()
    val pout = PipedInputStream(outputStream)

    channel.inputStream = inputStream
    channel.outputStream = outputStream
    channel.connect()
    printConsoleResult(pout)

    println("Ready to start executing 1st commands.")

    pin.write("ls -al\n".toByteArray())
    pin.flush()

    println("Ready to start executing 2nd commands.")

    pin.write("date\n".toByteArray())
    pin.flush()

    Thread.sleep(5000)
    channel.disconnect()
    session.disconnect()
    println("All done.")
}

fun printConsoleResult(pout: InputStream) {
    thread {
        var end = false
        val consoleOutput = BufferedReader(InputStreamReader(pout))
        while (!end) {
            consoleOutput.mark(32)
            if (consoleOutput.read() == 0x03) { // End of Text
                end = true
            } else {
                consoleOutput.reset()
                val line = consoleOutput.readLine()
                if (line != null) println(line)
                end = false
            }
        }
        println("Console result ended.")
    }
}


