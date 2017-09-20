package org.processmining.streamsocialnetworks.models;

import org.processmining.framework.util.Pair;

public class TripleImpl<T, S, U> {

	private final T first;

	private final S second;

	private final U third;

	public TripleImpl(T first, S second, U third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}

	public U getThird() {
		return third;
	}

	public Pair<T, S> getPairOfFirstAndSecond() {
		return new Pair<T, S>(first, second);
	}

	public Pair<T, U> getPairOfFirstAndThird() {
		return new Pair<T, U>(first, third);
	}

	public Pair<S, U> getPairOfSecondAndThird() {
		return new Pair<S, U>(second, third);
	}
}
