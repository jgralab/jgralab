package de.uni_koblenz.jgralab.algolib.buffers;

public abstract class DynamicArrayBuffer<T> implements Buffer<T> {

	private static final long MAX = Integer.MAX_VALUE;
	private static final long MIN = 1000000l;

	protected Object[] data;
	protected int initialSize;
	protected int filled;

	public DynamicArrayBuffer(int initialSize) {
		super();
		this.initialSize = initialSize;
		data = new Object[initialSize];
		filled = 0;
	}

	private void expand() {
		// System.out.println("Expanding");
		Object[] newData = new Object[expandSize(data.length)];
		System.arraycopy(data, 0, newData, 0, data.length);
		// for (int i = 0; i < data.length; i++) {
		// newData[i] = data[i];
		// }
		data = newData;
	}

	private int expandSize(int oldSize) {
		long length = oldSize;
		if (length < MIN) {
			length *= 2;
		} else if (length < 3 * MIN) {
			length *= 1.5;
		} else if (length < 6 * MIN) {
			length *= 1.25;
		}

		if (length >= MAX) {
			return Integer.MAX_VALUE;
		}
		return (int) length;
	}

	@Override
	public boolean isEmpty() {
		return filled == 0;
	}

	@Override
	public void put(T element) {
		if (filled == data.length) {
			expand();
		}
		data[filled++] = element;
	}

}