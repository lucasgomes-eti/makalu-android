package eti.lucasgomes.adress

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk

@Suppress("UNCHECKED_CAST")
fun <T> successfulTask(result: T?): Task<T> {
    val task = mockk<Task<T>>()
    every { task.addOnSuccessListener(any<OnSuccessListener<in T>>()) } answers {
        firstArg<OnSuccessListener<T>>().onSuccess(result as T)
        task
    }
    every { task.addOnFailureListener(any<OnFailureListener>()) } returns task
    every { task.addOnCompleteListener(any<OnCompleteListener<T>>()) } answers {
        firstArg<OnCompleteListener<T>>().onComplete(task)
        task
    }
    return task
}

fun <T> failedTask(exception: Exception): Task<T> {
    val task = mockk<Task<T>>()
    every { task.addOnSuccessListener(any<OnSuccessListener<in T>>()) } returns task
    every { task.addOnFailureListener(any<OnFailureListener>()) } answers {
        firstArg<OnFailureListener>().onFailure(exception)
        task
    }
    every { task.addOnCompleteListener(any<OnCompleteListener<T>>()) } answers {
        firstArg<OnCompleteListener<T>>().onComplete(task)
        task
    }
    return task
}

fun <T> pendingTask(): Task<T> {
    val task = mockk<Task<T>>()
    every { task.addOnSuccessListener(any<OnSuccessListener<in T>>()) } returns task
    every { task.addOnFailureListener(any<OnFailureListener>()) } returns task
    every { task.addOnCompleteListener(any<OnCompleteListener<T>>()) } returns task
    return task
}
