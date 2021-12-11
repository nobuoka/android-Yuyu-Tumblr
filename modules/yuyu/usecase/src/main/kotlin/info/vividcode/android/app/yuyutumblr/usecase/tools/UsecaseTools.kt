package info.vividcode.android.app.yuyutumblr.usecase.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

sealed class UseCaseJob<S, EF, EB> {
    class Foreground<S, EF, EB>(val process: (EF) -> S?) : UseCaseJob<S, EF, EB>()
    class Background<S, EF, EB>(val process: (EB) -> S?) : UseCaseJob<S, EF, EB>()
}

interface UseCaseReactor<C, S, EF, EB> {
    fun createFirstStepOfCommand(command: C): S
    fun createJob(step: S): UseCaseJob<S, EF, EB>
}

class UseCaseDriver<C, S, EF, EB>(
        private val useCaseReactor: UseCaseReactor<C, S, EF, EB>,
        private val coroutineScope: CoroutineScope,
        private val foregroundCoroutineContext: CoroutineContext,
        private val backgroundCoroutineContext: CoroutineContext,
        private val backgroundEnvironment: EB
) {

    private val currentExecutions: MutableSet<Execution> = HashSet()

    fun activate(foregroundEnvironment: EF) {
        this.foregroundEnvironment = foregroundEnvironment
        paused = false

        currentExecutions.forEach {
            if (it.currentStep != null && it.currentJob == null) {
                it.executeSteps()
            }
        }
    }

    fun deactivate() {
        this.foregroundEnvironment = null
        paused = true
    }

    var foregroundEnvironment: EF? = null
    var paused: Boolean = true

    fun handleCommand(command: C) {
        val execution = Execution()
        execution.currentStep = useCaseReactor.createFirstStepOfCommand(command)
        execution.executeSteps()
        currentExecutions.add(execution)
    }

    private inner class Execution() {
        var currentStep: S? = null
        var currentJob: UseCaseJob<S, EF, EB>? = null

        fun executeSteps() {
            coroutineScope.launch(foregroundCoroutineContext, CoroutineStart.UNDISPATCHED) {
                executeStepsAsync()
            }
        }

        private suspend fun executeStepsAsync() {
            while (true) {
                val step = currentStep ?: break
                if (paused) break

                println("Begin step : $step (${Thread.currentThread()})")
                val job = useCaseReactor.createJob(step)
                currentJob = job
                val nextStep = when (job) {
                    is UseCaseJob.Foreground<S, EF, EB> -> job.process(foregroundEnvironment!!)
                    is UseCaseJob.Background<S, EF, EB> -> withContext(backgroundCoroutineContext) {
                        job.process(backgroundEnvironment)
                    }
                }
                println("End step : $step (${Thread.currentThread()})")

                currentStep = nextStep
                currentJob = null
            }
        }
    }

}