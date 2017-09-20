package org.processmining.streamsocialnetworks.models;

public class SSNLink<T> {

	private int count = 0;

	private final T from;

	private final T to;

	private double value = 0d;

	public SSNLink(T from, T to) {
		this(from, to, 0, 0d);
	}

	public SSNLink(T from, T to, double value) {
		this(from, to, 0, value);
	}

	public SSNLink(T from, T to, int count, double value) {
		this.from = from;
		this.to = to;
		this.value = value;
		this.count = count;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SSNLink) {
			SSNLink<?> c = (SSNLink<?>) o;
			boolean r = c.getFrom().equals(getFrom());
			r &= c.getTo().equals(getTo());
			return r && c.getValue() == getValue();
		}
		return false;
	}

	public int getCount() {
		return count;
	}

	public T getFrom() {
		return from;
	}

	public T getTo() {
		return to;
	}

	public double getValue() {
		return value;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
