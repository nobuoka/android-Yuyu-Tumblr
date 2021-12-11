package info.vividcode.android.app.yuyutumblr.usecase

import info.vividcode.android.app.yuyutumblr.usecase.tools.UseCaseDriver
import info.vividcode.android.app.yuyutumblr.usecase.tools.UseCaseJob
import info.vividcode.android.app.yuyutumblr.usecase.tools.UseCaseReactor
import kotlinx.coroutines.*

fun main() {
    runBlocking {
        val mainCoroutineScope = CoroutineScope(newSingleThreadContext("main2"))
        val ioCoroutineContext = newSingleThreadContext("io")

        val showMainTimelineUseCaseDriver = UseCaseDriver(
                ShowMainTimelineUseCase, GlobalScope, Dispatchers.Default, ioCoroutineContext,
                object : ShowMainTimelineUseCase.BackgroundEnvironment {
                    override val httpClient: String = "Http Client"
                }
        )

        val foregroundEnvironment = object : ShowMainTimelineUseCase.ForegroundEnvironment {
            override val mainTimelineView = object : MainTimelineView {
                override fun showProgressBar() {
                    println("Show progress bar")
                }
            }
        }
        showMainTimelineUseCaseDriver.activate(foregroundEnvironment)

        launch (mainCoroutineScope.coroutineContext) {
            showMainTimelineUseCaseDriver.handleCommand(ShowMainTimelineUseCase.Command.Start)
        }

        launch(start = CoroutineStart.UNDISPATCHED) {
            launch(start = CoroutineStart.UNDISPATCHED) {
                println("1 (${Thread.currentThread()})")
            }
            println("2 (${Thread.currentThread()})")
        }

        delay(10_000)
    }
}

interface ShowMainTimelineUseCase {

    interface ForegroundEnvironment {
        val mainTimelineView: MainTimelineView
    }

    interface BackgroundEnvironment {
        val httpClient: String
    }

    sealed class Command {
        object Start : Command()
    }

    sealed class Step {
        class ShowProgressBar : Step()
        class Fetch(val targetTag: String) : Step()
        class ShowMainTimeline(val tumblrPosts: String) : Step()
    }

    companion object : UseCaseReactor<Command, Step, ForegroundEnvironment, BackgroundEnvironment> {
        override fun createFirstStepOfCommand(command: Command): Step = when (command) {
            Command.Start -> Step.ShowProgressBar()
        }

        override fun createJob(
                step: Step
        ): UseCaseJob<Step, ForegroundEnvironment, BackgroundEnvironment> = when (step) {
            is Step.ShowProgressBar -> UseCaseJob.Foreground {
                it.mainTimelineView.showProgressBar()
                Step.Fetch("由々しき")
            }
            is Step.Fetch -> UseCaseJob.Background {
                println("取得処理開始！ [${step.targetTag}] (${Thread.currentThread()})")
                println("request by ${it.httpClient}")
                println("取得処理終了！")
                Step.ShowMainTimeline("とれたやつ")
            }
            is Step.ShowMainTimeline -> UseCaseJob.Foreground {
                println("タイムライン表示！ [${step.tumblrPosts}] (${Thread.currentThread()})")
                null
            }
        }
    }

}

interface MainTimelineView {
    fun showProgressBar()
}
