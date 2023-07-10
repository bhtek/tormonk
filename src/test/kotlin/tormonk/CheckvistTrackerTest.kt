package tormonk

import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.requests.DefaultBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class CheckvistTrackerTest {
    @Test
    @Disabled
    fun getLastUpdatedTime_simple() {
        FuelManager.instance.client = object : Client {
            override fun executeRequest(request: Request): Response {
                return Response(request.url, 200, body = DefaultBody({ javaClass.getResourceAsStream("/getLastUpdatedTime_simple.json") }))
            }
        }

        val tracker = CheckvistTracker()
        tracker.checkvistService = CheckvistService()
        tracker.checkvistService.afterPropertiesSet()
        val allTasks = tracker.getAllTasks()
        assertThat(allTasks, notNullValue())
        assertThat(tracker.getLastUpdateTime(allTasks!!), equalTo(123L))
    }
}
