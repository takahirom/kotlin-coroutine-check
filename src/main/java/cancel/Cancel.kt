package cancel
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.*
import kotlinx.coroutines.experimental.*

fun main(args: Array<String>) {
    val action2 = Action2()
    action2.simpleLoad()
    action2.simpleLoad()
    action2.simpleLoad()
    action2.simpleLoad()
    Thread.sleep(300)
}

fun Job?.isCompleted(): Boolean = (this == null || isCompleted)

class Action2 : CoroutineScope {
    val job = Job()
    var jobForExclusion: Job? = null
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default + job
    fun simpleLoad() {
        if (!jobForExclusion.isCompleted()) {
            return
        }
        jobForExclusion = launch {
            try {
                println("LOADING")
                fetchString()
                println("EpisodeLoadedEvent")
            } catch (e: Exception) {
                println("ErrorEvent")
            } finally {
                println("LOADABLE")
            }
        }
    }

    private suspend fun fetchStringWithException(): String = suspendCancellableCoroutine {
        it.invokeOnCancellation { println("cancel fetchStringWithException") }
        thread {
            it.resumeWithException(RuntimeException())
        }
    }

    private suspend fun fetchString(): String = suspendCancellableCoroutine {
        it.invokeOnCancellation { println("cancel fetchString") }
        thread {
            Thread.sleep(100)
            it.resume("result1")
        }
    }
}

