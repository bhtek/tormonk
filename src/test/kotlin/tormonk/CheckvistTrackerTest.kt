package tormonk

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import java.nio.charset.Charset

class CheckvistTrackerTest {
    @Test
    fun getLastUpdatedTime_simple() {
        FuelManager.instance.client = object : Client {
            override fun executeRequest(request: Request): Response {
                return Response().apply {
                    url = request.url
                    httpStatusCode = 200
                    data = """[{"id":23228980,"parent_id":0,"checklist_id":569126,"status":0,"position":1,"tasks":[],"update_line":"note added by btek","updated_at":"2016/07/30 04:15:39 +0000","due":null,"content":"Last Uploaded by tormonk","collapsed":false,"comments_count":1,"assignee_ids":[],"details":{},"tags":{},"tags_as_text":"","notes":[{"note":{"comment":"123","created_at":"2016/07/30 04:15:39 +0000","id":918294,"task_id":23228980,"updated_at":"2016/07/30 04:15:39 +0000","user_id":3573,"username":"btek"}}]},{"id":23228984,"parent_id":0,"checklist_id":569126,"status":0,"position":2,"tasks":[],"update_line":"note added by btek","updated_at":"2016/07/30 04:15:45 +0000","due":null,"content":"Last Downloaded by tordownk","collapsed":false,"comments_count":1,"assignee_ids":[],"details":{},"tags":{},"tags_as_text":"","notes":[{"note":{"comment":"456","created_at":"2016/07/30 04:15:45 +0000","id":918296,"task_id":23228984,"updated_at":"2016/07/30 04:15:45 +0000","user_id":3573,"username":"btek"}}]}]""".toByteArray(Charset.forName("UTF-8"))
                }
            }
        }

        Fuel.testMode()

        val tracker = CheckvistTracker()
        assertThat(tracker.getLastUpdateTime(), equalTo(123L))
    }
}

