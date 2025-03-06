import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import com.example.module
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import kotlinx.coroutines.test.TestResult
import kotlinx.serialization.json.Json
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testHelloWorld() = withApp {
        val response = get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, World!", response.bodyAsText())
    }
}

/**
 * Allows easier testing of the application, with the configuration already loaded
 * and the client already configured.
 */
fun withApp(test: suspend HttpClient.() -> Unit): TestResult =
    testApplication {
        environment { config = ApplicationConfig("application.yaml") }
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
        startApplication()
        test(client)
    }
