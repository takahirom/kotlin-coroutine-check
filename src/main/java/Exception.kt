package exception

import kotlin.concurrent.thread
import kotlin.coroutines.experimental.*
import kotlinx.coroutines.experimental.*

fun main(args: Array<String>) {
    println("***simple error")
    Action().simpleLoadError()
    Thread.sleep(300)

    println("***two async error")
    Action().zipLoadError()
    Thread.sleep(300)

    println("***two async if one success , do it")
    Action().compileLatestLoadError()
    Thread.sleep(300)
}

class Action : CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default + job
    fun simpleLoadError() {
        launch {
            try {
                println("LOADING")
                fetchStringWithException()
                println("EpisodeLoadedEvent")
            } catch (e: Exception) {
                println("ErrorEvent")
            } finally {
                println("LOADABLE")
            }
        }
    }

    fun zipLoadError() {
        launch {
            try {
                println("LOADING")
                val result1Deferred = async {
                    fetchString()
                }
                val result2Deferred = async {
                    fetchStringWithException()
                }
                println("EpisodeLoadedEvent ${result1Deferred.await()} ${result2Deferred.await()}")
            } catch (e: Exception) {
                println("ErrorEvent")
            } finally {
                println("LOADABLE")
            }
        }
    }

    fun compileLatestLoadError() {
        launch {
            try {
                println("LOADING")
                val result1Deferred = async {
                    fetchString()
                }
                val result2Deferred = async {
                    fetchStringWithException()
                }
                val result1 = try {
                    result1Deferred.await()
                } catch (e: Exception) {
                    null
                }
                val result2 = try {
                    result2Deferred.await()
                } catch (e: Exception) {
                    null
                }
                println("EpisodeLoadedEvent $result1 $result2")
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

