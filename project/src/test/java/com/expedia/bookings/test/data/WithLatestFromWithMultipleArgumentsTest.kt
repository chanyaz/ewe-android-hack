package com.expedia.bookings.test.data

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import rx.observers.TestSubscriber
import com.expedia.bookings.utils.withLatestFrom
import org.junit.Before
import rx.subjects.PublishSubject


@RunWith(RobolectricRunner::class)
class WithLatestFromWithMultipleArgumentsTest {
    val output = TestSubscriber.create<String>()
    val output1 = TestSubscriber.create<Int>()
    val output2 = TestSubscriber.create<Int>()
    val output3 = TestSubscriber.create<Int>()

    //main observable on which emission depends
    val input = PublishSubject.create<Unit>()

    //Observable from which latest values will be taken
    val input1 = PublishSubject.create<Int>()
    val input2 = PublishSubject.create<Int>()
    val input3 = PublishSubject.create<Int>()

    @Before
    fun before() {
        input1.subscribe(output1)
        input2.subscribe(output2)
        input3.subscribe(output3)

        input.withLatestFrom(input1, input2, input3, {
            Unit, a1, a2, a3 ->
            object {
                val a1 = a1
                val a2 = a2
                val a3 = a3
            }
        }).subscribe {
            output.onNext("First Observable Emission: " + it.a1 + "  Second Observable Emission: " + it.a2 + "  Third Observable Emission: " + it.a3)
        }
    }

    @Test
    fun testWithLatestFromWithMultipleArgumentsOperator() {
        //No emission from the operator till each input Observable/Subject has emitted atleast one value
        input.onNext(Unit)
        output.assertValueCount(0)

        input1.onNext(1)
        output.assertValueCount(0)

        input2.onNext(2)
        output.assertValueCount(0)

        input3.onNext(3)
        output.assertValueCount(0)

        //Emission from the operator when each Observable/Subject has emiited atleast one value and there is an emission in the main Observable/Subject
        input.onNext(Unit)
        output.assertValueCount(1)
        output.assertValue("First Observable Emission: " + output1.onNextEvents[0]
                + "  Second Observable Emission: " + output2.onNextEvents[0]
                + "  Third Observable Emission: " + output3.onNextEvents[0])

        //Emission in one of the latest Observable/Subject will not trigger any emission from the operator
        input1.onNext(7)
        output.assertValueCount(1)
        output.assertValue("First Observable Emission: " + output1.onNextEvents[0]
                + "  Second Observable Emission: " + output2.onNextEvents[0]
                + "  Third Observable Emission: " + output3.onNextEvents[0])

        //Emission from the main operator will trigger an emission from the operator with the latest values
        input.onNext(Unit)
        output.assertValueCount(2)
        output.assertValues("First Observable Emission: " + output1.onNextEvents[0]
                + "  Second Observable Emission: " + output2.onNextEvents[0]
                + "  Third Observable Emission: " + output3.onNextEvents[0],
                "First Observable Emission: " + output1.onNextEvents[1]
                        + "  Second Observable Emission: " + output2.onNextEvents[0]
                        + "  Third Observable Emission: " + output3.onNextEvents[0])
    }
}