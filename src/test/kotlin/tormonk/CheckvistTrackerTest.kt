package tormonk

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

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
        assertThat(tracker.getLastUpdateTime(), equalTo(123L))
    }
}

