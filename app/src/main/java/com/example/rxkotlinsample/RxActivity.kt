package com.example.rxkotlinsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_rx.*
import java.util.concurrent.TimeUnit

class RxActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx)
        setupClickListeners()
    }

    //Set click listeners
    private fun setupClickListeners() {
        btnCreate.setOnClickListener {
            onHandleCreateObservable()
        }
        btnJust.setOnClickListener {
            onHandleJust()
        }
        btnRange.setOnClickListener {
            onHandleRange()
        }
        btnRepeat.setOnClickListener {
            onHandleRepeat()
        }
        btnInterval.setOnClickListener {
            onHandleInterval()
        }
        btnTimer.setOnClickListener {
            onHandleTimer()
        }
        btnFromArray.setOnClickListener {
            onHandleFromArray()
        }
        btnFromIterable.setOnClickListener {
            onHandleFromIterable()
        }
        btnFlowable.setOnClickListener {
            onHandleFlowable()
        }
    }

    //Create an Observable
    private fun getObservableFromList(myList: List<String>): Observable<String> =
        Observable.create<String> { emitter ->
            myList.forEach { item ->
                if (item.isBlank()) {
                    emitter.onError(Exception("Error: item cannot be blank"))
                }
                emitter.onNext(item)
            }
            emitter.onComplete()
        }

    // Create
    private fun onHandleCreateObservable() {
        val list = listOf("Apple", "", "Mango")
        tvOperationType.text = getString(R.string.create)
        tvInput.text = list.toString()
        val disposable = getObservableFromList(list)
            .subscribe({ value -> tvOutput.text = value },
                { error -> tvOutput.text = "${error.message}" })
        compositeDisposable.add(disposable)
    }

    //Just
    private fun onHandleJust() {
        val jusString = "RxJust"
        tvOperationType.text = getString(R.string.just)
        tvInput.text = jusString
        val disposable = Observable.just(jusString)
            .subscribe(
                { value -> tvOutput.text = value.toString() }, // onNext
                { error -> tvOutput.text = error?.message },    // onError
                { tvOutput.text = getString(R.string.completed) } // onComplete
            )
        compositeDisposable.add(disposable)
    }

    //Range
    private fun onHandleRange() {
        tvOperationType.text = getString(R.string.range)
        tvInput.text = "From 10 to 15 sec"
        val builder = StringBuilder()
        val disposable = Observable.rangeLong(
            10,     // Start
            5      // Count
        ).subscribe({ value ->
            builder.append(" $value")
        },
            { e -> tvOutput.text = "Range error: ${e.message}" },
            { tvOutput.text = "Range complete: $builder" })
        compositeDisposable.add(disposable)
    }

    //Repeat
    private fun onHandleRepeat() {
        tvOperationType.text = getString(R.string.repeat)
        val array = arrayOf("Apple,Mango")
        val output = StringBuilder()
        tvInput.text = array.joinToString()
        val disposable = Observable.fromArray(*array)
            .repeat(2)
            .subscribe(
                { value ->
                    output.append(" $value")
                }, // onNext
                { error -> tvOutput.text = error?.message },    // onError
                { tvOutput.text = "${getString(R.string.completed)} $output" } // onComplete
            )
        compositeDisposable.add(disposable)
    }

    //Interval
    private fun onHandleInterval() {
        tvOperationType.text = getString(R.string.interval)
        tvInput.text = "5 sec"
        val disposable = Observable.intervalRange(
            1,     // Start
            5,      // Count
            0,      // Initial Delay
            1,      // Period
            TimeUnit.SECONDS
        ).subscribe(
            { value -> tvOutput.text = value.toString() },
            { e -> tvOutput.text = "Interval: error ${e.message}" },
            { tvOutput.text = "Interval complete" })
        compositeDisposable.add(disposable)
    }

    //Timer
    private fun onHandleTimer() {
        tvOperationType.text = getString(R.string.timer)
        tvInput.text = "2 sec delay"
        val disposable = Observable.timer(
            2L,// Delay
            TimeUnit.SECONDS
        ).subscribe(
            { value -> tvOutput.text = value.toString() },
            { e -> tvOutput.text = "Timer: error ${e.message}" },
            { tvOutput.text = "Timer complete" })
        compositeDisposable.add(disposable)
    }

    //From Array
    private fun onHandleFromArray() {
        val array = arrayOf("Apple", "Orange", "Banana")
        tvOperationType.text = getString(R.string.fromarray)
        tvInput.text = array.joinToString()
        Observable.fromArray(*array)
            .subscribe({ value -> tvOutput.text = value },
                { e -> tvOutput.text = "From Array: error ${e.message}" },
                { tvOutput.text = "From Array complete" })
    }

    //From Iterable
    private fun onHandleFromIterable() {
        val list = listOf("Apple", "Orange", "Banana")
        tvOperationType.text = getString(R.string.fromiterable)
        tvInput.text = list.toString()
        Observable.fromIterable(list)
            .subscribe(
                { tvOutput.text = it }, // onNext
                { e -> tvOutput.text = "Error: ${e.message}" }, // onError
                { tvOutput.text = getString(R.string.completed) } // onComplete
            )
    }

    //From Callable
    private fun onHandleFromCallable() {

    }

    //From Future
    private fun onHandleFromFuture() {

    }

    //From Publisher
    private fun onHandleFromPublisher() {

    }

    //Flowable
    private fun onHandleFlowable() {
        val count = 1000000
        tvOperationType.text = getString(R.string.flowable)
        tvInput.text = "$count items"
        var output = 0
        val flowable = PublishSubject.create<Int>()
        flowable.toFlowable(BackpressureStrategy.DROP)
            .observeOn(Schedulers.computation())
            .subscribe(
                { value ->
                    output = value
                },
                { t ->
                    println("Flowable error: ${t.message}")
                },
                {
                    println("Flowable complete:")
                }
            ).addTo(compositeDisposable)
        for (i in 0..count) {
            flowable.onNext(i)
        }
        tvOutput.text = "Flowable complete: $output"
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}