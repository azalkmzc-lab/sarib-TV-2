package com.example

import androidx.media3.common.MimeTypes
import com.example.util.StreamUrlParser
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testProxyWorkerStreamUrlParsing() {
        val testUrl = "https://ostora.ravynerinnn.workers.dev/proxy?url=https%3A%2F%2Fwww.maziikaaaaaa.shop%2Fx1%2F225587619216738.php&ua=Mozilla%2F5.0%20%28Windows%20NT%2010.0%3B%20Win64%3B%20x64%29%20AppleWebKit%2F537.36%20%28KHTML%2C%20like%20Gecko%29%20Chrome%2F151.0.0.0%20Safari%2F537.36&iv=0x1d8bb95bf5e7dbf21bddf5269096d9b8"
        val config = StreamUrlParser.parse(testUrl)

        assertEquals(testUrl, config.cleanUrl)
        assertNotNull(config.userAgent)
        assertTrue(config.userAgent!!.contains("Chrome/151.0.0.0"))
        assertEquals("0x1d8bb95bf5e7dbf21bddf5269096d9b8", config.headers["iv"])
        assertEquals(MimeTypes.APPLICATION_M3U8, config.mimeType)
    }
}



