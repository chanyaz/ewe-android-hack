package com.expedia.bookings.unit;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.expedia.bookings.utils.FragmentModificationSafeLock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FragmentModificationSafeLockTests {
	@Test
	public void testLockSafeWorks() {
		FragmentModificationSafeLock lock = new FragmentModificationSafeLock();
		assertFalse("expected not to be safe", lock.isSafe());

		lock.setSafe(true);
		assertTrue("expected to be safe", lock.isSafe());

		lock.setSafe(true);
		assertTrue("expected to be safe", lock.isSafe());

		lock.setSafe(false);
		assertFalse("expected not to be safe", lock.isSafe());
	}

	@Test
	public void testLockBlocksRunnable() {
		final CountDownLatch runnableRan = new CountDownLatch(1);

		FragmentModificationSafeLock lock = new FragmentModificationSafeLock();

		lock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				runnableRan.countDown();
			}
		});

		assertTrue("expected the runnable to not be run", runnableRan.getCount() > 0);
	}

	@Test
	public void testOpenLockLetsRunnableRun() {
		final CountDownLatch runnableRan = new CountDownLatch(1);

		FragmentModificationSafeLock lock = new FragmentModificationSafeLock();

		lock.setSafe(true);
		lock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				runnableRan.countDown();
			}
		});

		assertTrue("expected the runnable to be finished", runnableRan.getCount() == 0);
	}

	@Test
	public void testOpeningLockLetsRunnableRun() {
		final CountDownLatch runnableRan = new CountDownLatch(1);

		FragmentModificationSafeLock lock = new FragmentModificationSafeLock();

		lock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				runnableRan.countDown();
			}
		});

		assertTrue("expected the runnable to not be run", runnableRan.getCount() > 0);

		lock.setSafe(true);

		assertTrue("expected the runnable to be finished", runnableRan.getCount() == 0);
	}

	@Test
	public void testOpeningAndClosingLockBlocksRunnable() {
		final CountDownLatch runnableRan = new CountDownLatch(1);

		FragmentModificationSafeLock lock = new FragmentModificationSafeLock();

		lock.setSafe(true);
		lock.setSafe(false);

		lock.runWhenSafe(new Runnable() {
			@Override
			public void run() {
				runnableRan.countDown();
			}
		});

		assertTrue("expected the runnable to not be run", runnableRan.getCount() > 0);
	}
}
