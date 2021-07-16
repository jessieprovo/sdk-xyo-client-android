package network.xyo.client

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.launch
import network.xyo.client.address.XyoAddress
import network.xyo.client.archivist.api.XyoArchivistApiClient
import network.xyo.client.archivist.api.XyoArchivistApiConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

class TestPayload1SubObject {
    var number_value = 2
    var string_value = "yo"
}

class TestPayload1: XyoPayload("network.xyo.test") {
    var timestamp = 1618603439107
    var number_field = 1
    var object_field = TestPayload1SubObject()
    var string_field = "there"
}

class TestPayload2SubObject {
    var string_value = "yo"
    var number_value = 2
}

class TestPayload2: XyoPayload("network.xyo.test") {
    var string_field = "there"
    var object_field = TestPayload1SubObject()
    var timestamp = 1618603439107
    var number_field = 1
}

val knownHash = "af0ba507a43ce9cde64afd7bca25b901745b8aa8aa99cac1d53f732638bf967d"

class XyoBoundWitnessTest {

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.INTERNET)


    lateinit var appContext: Context

    @Before
    fun useAppContext() {
        // Context of the app under test.
        this.appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testNotAuthenticated() {
        val config = XyoArchivistApiConfig("test", "http://localhost:3030/dev")
        val api = XyoArchivistApiClient.get(config)
        assertFalse(api.authenticated)
    }

    @Test
    fun testPayload1() {
        val payload = TestPayload1()
        val hash = BoundWitnessBuilder.hash(payload)
        assertEquals("13898b1fc7ef16c6eb8917b4bdd1aabbc1981069f035c51d4166a171273bfe3d", hash )
        val address = XyoAddress("test")
        val bw = BoundWitnessBuilder().witness(address).payload("network.xyo.test", TestPayload1())
        val bwJson = bw.build()
        assertEquals(knownHash, bwJson._hash)
    }

    @Test
    fun testPayload1WithSend() {
        xyoScope.launch {
            val address = XyoAddress("test")
            val config = XyoArchivistApiConfig("test", "https://beta.archivist.xyo.network")
            val api = XyoArchivistApiClient.get(config)
            val bw =
                BoundWitnessBuilder().witness(address).payload("network.xyo.test", TestPayload1())
            val bwJson = bw.build()
            assertEquals(knownHash, bwJson._hash)
            val postResult = api.postBoundWitnessAsync(bwJson)
            assertEquals(0, postResult.errors?.size)
        }
    }

    @Test
    fun testPayload2() {
        val address = XyoAddress("test")
        val bw = BoundWitnessBuilder().witness(address).payload("network.xyo.test", TestPayload2())
        val bwJson = bw.build()
        assertEquals(knownHash, bwJson._hash)
    }

    @Test
    fun testPayload2WithSend() {
        xyoScope.launch {
            val address = XyoAddress("test")
            val config = XyoArchivistApiConfig("test", "https://beta.archivist.xyo.network")
            val api = XyoArchivistApiClient.get(config)
            val bw = BoundWitnessBuilder().witness(address).payload("network.xyo.test", TestPayload2())
            val bwJson = bw.build()
            assertEquals(knownHash, bwJson._hash)
            val postResult = api.postBoundWitnessAsync(bwJson)
            assertEquals(0, postResult.errors?.size)
        }
    }
}