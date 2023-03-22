package telran.mediation;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlkQueue<T> implements IBlkQueue<T> {

	Lock locker = new ReentrantLock();
	Condition producerWaitingCondition = locker.newCondition();
	Condition consumerWaitingCondition = locker.newCondition();

	private final LinkedList<T> queue = new LinkedList<>();
	private final int maxSize;

	public BlkQueue(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public void push(T message) {

		locker.lock();
		try {
			while (queue.size() == maxSize) {
				try {
					producerWaitingCondition.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			queue.add(message);
			consumerWaitingCondition.signal();
		} finally {
			locker.unlock();
		}
	}

	@Override
	public T pop() {
		locker.lock();
		try {
			while (queue.isEmpty()) {
				try {
					consumerWaitingCondition.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			T message = queue.removeFirst();
			producerWaitingCondition.signal();
			return message;
		} finally {
			locker.unlock();
		}
	}
}