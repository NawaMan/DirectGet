//  ========================================================================
//  Copyright (c) 2017 The Direct Solution Software Builder.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
package direct.get;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.val;

/**
 * 
 * 
 * @author nawaman
 */
public class Retain {
	
	static Ref<Thread> currentThread = Ref.of(Thread.class, Preferability.Normal, ()->Thread.currentThread());

	
	static Ref<Long> currentTimeMillis = Ref.of(Long.class, Preferability.Normal, ()->Long.valueOf(System.currentTimeMillis()));
	
	
	public static <V> RetainerBuilder<V> valueFrom(Supplier<V> supplier) {
		return new RetainerBuilder<>(supplier);
	}
	
	public static <V> RetainerBuilder<V> Retain(Supplier<V> supplier) {
		return new RetainerBuilder<>(supplier);
	}
	
	
	public static interface Retainer<V> extends Supplier<V>  {
		
	}
	
	public static class GlobalRetainer<V> implements Retainer<V> {

		private final Supplier<V> supplier;
		
		private final Predicate<V> shouldRetain;
		
		private final AtomicReference<Optional<V>> cache = new AtomicReference<Optional<V>>(null);
		
		public GlobalRetainer(Supplier<V> supplier, Predicate<V> shouldRetain) {
			this.supplier     = supplier;
			this.shouldRetain = shouldRetain;
		}

		@Override
		public V get() {
			if (cache.compareAndSet(null, Optional.empty())) {
				// First time
				val newValue = supplier.get();
				cache.set(Optional.ofNullable(newValue));
				return newValue;
			}

			val oldValue = cache.get().orElse(null);
			if (shouldRetain.test(oldValue)) {
				return oldValue;
			}
			
			val newValue = supplier.get();
			cache.set(Optional.ofNullable(newValue));
			return newValue;
		}
		
	}
	
	public static class LocalRetainer<V> implements Retainer<V> {

		private final Supplier<V> supplier;
		
		private final Predicate<V> shouldRetain;
		
		private final ThreadLocal<Optional<V>> cache = ThreadLocal.withInitial(()->null);
		
		public LocalRetainer(Supplier<V> supplier, Predicate<V> shouldRetain) {
			this.supplier     = supplier;
			this.shouldRetain = shouldRetain;
		}

		@Override
		public V get() {
			if (cache.get() == null) {
				// First time
				val newValue = supplier.get();
				cache.set(Optional.ofNullable(newValue));
				return newValue;
			}

			val oldValue = cache.get().orElse(null);
			if (shouldRetain.test(oldValue)) {
				return oldValue;
			}
			
			val newValue = supplier.get();
			cache.set(Optional.ofNullable(newValue));
			return newValue;
		}
		
	}
	
	public static class RetainerBuilder<V> {
		
		private static Predicate<?> ALWAYS = value->true;
		
		private static Predicate<?> NEVER = value->false;
		
		private boolean isGlobal = false;
		
		private final Supplier<V> supplier;
		
		public RetainerBuilder(Supplier<V> supplier) {
			this.supplier = supplier;
		}
		
		public RetainerBuilder<V> globally() {
			isGlobal = true;
			return this;
		}
		
		public RetainerBuilder<V> locally() {
			isGlobal = false;
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public Supplier<V> always() {
			return isGlobal
					? new GlobalRetainer<V>(supplier, (Predicate<V>)ALWAYS)
					: new LocalRetainer<V>(supplier,  (Predicate<V>)ALWAYS);
		}

		@SuppressWarnings("unchecked")
		public Supplier<V> never() {
			return isGlobal
					? new GlobalRetainer<V>(supplier, (Predicate<V>)NEVER)
					: new LocalRetainer<V>(supplier,  (Predicate<V>)NEVER);
		}
		
		public Supplier<V> forCurrentThread() {
			return locally().always();
		}
		
		public <T> Supplier<V> forSame(Ref<T> ref) {
			val refValue = new AtomicReference<T>(Get.a(ref));
			Predicate<V> shouldRetain = value->{
				val newValue = Get.a(ref);
				val isSame = newValue == refValue.get();
				if (!isSame) {
					refValue.set(newValue);
				}
				return isSame;
			};
			return isGlobal
					? new GlobalRetainer<V>(supplier, shouldRetain)
					: new LocalRetainer<V>(supplier,  shouldRetain);
		}
		
		public <T> Supplier<V> forEquivalent(Ref<T> ref) {
			val refValue = new AtomicReference<T>(Get.a(ref));
			Predicate<V> shouldRetain = value->{
				val newValue = Get.a(ref);
				val isEquivalent = Objects.equals(newValue, refValue.get());
				if (!isEquivalent) {
					refValue.set(newValue);
				}
				return isEquivalent;
			};
			return isGlobal
					? new GlobalRetainer<V>(supplier, shouldRetain)
					: new LocalRetainer<V>(supplier,  shouldRetain); 
		}
		
		public <T> Supplier<V> forTime(long time, TimeUnit unit) {
			val expiredValue = new AtomicLong(Get.a(currentTimeMillis) + unit.toMillis(time));
			Predicate<V> shouldRetain = value->{
				val currentTime = Get.a(currentTimeMillis);
				val hasExpires = currentTime >= expiredValue.get();
				if (hasExpires) {
					expiredValue.set(Get.a(currentTimeMillis) + unit.toMillis(time));
				}
				return !hasExpires;
			};
			return isGlobal
					? new GlobalRetainer<V>(supplier, shouldRetain)
					: new LocalRetainer<V>(supplier,  shouldRetain); 
		}
		
	}
	
	
}
