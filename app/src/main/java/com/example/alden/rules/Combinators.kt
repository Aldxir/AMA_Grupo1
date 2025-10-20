package com.example.alden.rules

typealias Predicate<T> = (T) -> Boolean

infix fun <T> Predicate<T>.and(other: Predicate<T>): Predicate<T> = { t -> this(t) && other(t) }
infix fun <T> Predicate<T>.or(other: Predicate<T>): Predicate<T> = { t -> this(t) || other(t) }
fun <T> not(p: Predicate<T>): Predicate<T> = { t -> !p(t) }