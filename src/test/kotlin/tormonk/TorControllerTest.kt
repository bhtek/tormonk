package tormonk

import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class TorControllerTest {
    @MockitoBean
    private lateinit var checkvistService: CheckvistService

    @MockitoBean
    private lateinit var torrentJobs: TorrentJobContainer

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun trackRunsTorrentJob() {
        mockMvc.perform(post("/track")).andExpect(status().isOk)

        verify(torrentJobs).checkForTorrents()
    }
}
