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
package directget.get;

/**
 * This class offer a way to make it easy to debug lambda by adding name to them.
 * 
 * @author nawaman
 **/
// https://stackoverflow.com/questions/42876840/namingtostring-lambda-expressions-for-debugging-purpose/42876841#42876841
// https://stackoverflow.com/questions/23704355/creating-string-representation-of-lambda-expression/23705160#23705160
public interface Named {
    
    /** The ready to use object. */
    public static Named.User instance = new User() {
    };
    
    /** Returns the name. */
    public String getName();
    
    /** Add name to the given predicate. */
    public static <T> Predicate<T> predicate(String name, java.util.function.Predicate<T> check) {
        return instance.predicate(name, check);
    }
    
    /** Add name to the given predicate. */
    public static <T> Predicate<T> Predicate(String name, java.util.function.Predicate<T> check) {
        return instance.predicate(name, check);
    }
    
    /** Add name to the given supplier. */
    public static <T> Supplier<T> supplier(String name, java.util.function.Supplier<T> supplier) {
        return instance.supplier(name, supplier);
    }
    
    /** Add name to the given supplier. */
    public static <T> Supplier<T> Supplier(String name, java.util.function.Supplier<T> supplier) {
        return instance.supplier(name, supplier);
    }
    
    /** Add name to the given runnable. */
    public static Runnable runnable(String name, java.lang.Runnable runnable) {
        return instance.runnable(name, runnable);
    }
    
    /** Add name to the given runnable. */
    public static java.lang.Runnable Runnable(String name, java.lang.Runnable runnable) {
        return instance.runnable(name, runnable);
    }
    
    /** Add name to the given consumer. */
    public static <T> Consumer<T> consumer(String name, java.util.function.Consumer<T> consumer) {
        return instance.consumer(name, consumer);
    }
    
    /** Add name to the given consumer. */
    public static <T> Consumer<T> Consumer(String name, java.util.function.Consumer<T> consumer) {
        return instance.consumer(name, consumer);
    }
    
    /** Named predicate. **/
    public static class Predicate<T> implements Named, java.util.function.Predicate<T> {
        private final String name;
        private final java.util.function.Predicate<T> check;
        
        /** Constructors. */
        public Predicate(String name, java.util.function.Predicate<T> check) {
            this.name = name;
            this.check = check;
        }
        
        public String getName() {
            return this.name;
        }
        
        public boolean test(T t) {
            return check.test(t);
        }
        
        public String toString() {
            return "Predicate(" + name + ")";
        }
    }
    
    /** Named runnable. **/
    public static class Runnable implements Named, java.lang.Runnable {
        private final String name;
        private final java.lang.Runnable runnable;
        
        /** Constructors. */
        public Runnable(String name, java.lang.Runnable runnable) {
            this.name = name;
            this.runnable = runnable;
        }
        
        public void run() {
            runnable.run();
        }
        
        public String getName() {
            return name;
        }
        
        public String toString() {
            return "Runnable(" + name + ")";
        }
    }
    
    /** Named supplier */
    public static class Supplier<T> implements Named, java.util.function.Supplier<T> {
        private final String name;
        private final java.util.function.Supplier<T> supplier;
        
        /** Constructors. */
        public Supplier(String name, java.util.function.Supplier<T> supplier) {
            this.name = name;
            this.supplier = supplier;
        }
        
        public String getName() {
            return this.name;
        }
        
        public T get() {
            return supplier.get();
        }
        
        public String toString() {
            return "Supplier(" + name + ")";
        }
    }
    
    /** Supplier for a value. */
    public static class ValueSupplier<T> extends Supplier<T> {
        
        /** The name of the supplier. */
        public static final String NAME = "FromValue";
        /** The template for the name. */
        public static final String NAME_TEMPLATE = NAME + "(%s)";
        
        /** Constructor */
        public ValueSupplier(T value) {
            super(String.format(NAME, String.valueOf(value)), () -> value);
        }
        
    }
    
    /** Supplier for a value of a ref. */
    public static class RefSupplier<T> extends Supplier<T> {
        
        /** The name of the supplier. */
        public static final String NAME = "FromRef";
        /** The template for the name. */
        public static final String NAME_TEMPLATE = NAME + "(%s)";
        
        /** Constructor */
        public RefSupplier(Ref<T> ref) {
            super(String.format(NAME_TEMPLATE, ref.toString()), () -> Get.a(ref));
        }
        
    }
    
    /** Named consumer. **/
    public static class Consumer<T> implements Named, java.util.function.Consumer<T> {
        private final String name;
        private final java.util.function.Consumer<T> consumer;
        
        /** Constructors. */
        public Consumer(String name, java.util.function.Consumer<T> consumer) {
            this.name     = name;
            this.consumer = consumer;
        }
        
        public void accept(T value) {
        	consumer.accept(value);
        }
        
        public String getName() {
            return name;
        }
        
        public String toString() {
            return "Consumer(" + name + ")";
        }
    }
    
    /**
     * This interface make it possible to the user of the class to use these
     * method without static import.
     **/
    public static interface User {
        
        /** Add name to the given predicate. */
        public default <T> Predicate<T> predicate(String name, java.util.function.Predicate<T> check) {
            return new Predicate<T>(name, check);
        }
        
        /** Add name to the given supplier. */
        public default Runnable runnable(String name, java.lang.Runnable runnable) {
            return new Runnable(name, runnable);
        }
        
        /** Add name to the given runnable. */
        public default <T> Supplier<T> supplier(String name, java.util.function.Supplier<T> supplier) {
            return new Supplier<T>(name, supplier);
        }
        
        /** Add name to the given consumer. */
        public default <T> Consumer<T> consumer(String name, java.util.function.Consumer<T> consumer) {
            return new Consumer<T>(name, consumer);
        }
        
        /** Add name to the given predicate. */
        public default <T> Predicate<T> Predicate(String name, Predicate<T> check) {
            return instance.predicate(name, check);
        }
        
        /** Add name to the given supplier. */
        public default <T> Supplier<T> Supplier(String name, Supplier<T> supplier) {
            return instance.supplier(name, supplier);
        }
        
        /** Add name to the given runnable. */
        public default Runnable Runnable(String name, Runnable runnable) {
            return instance.runnable(name, runnable);
        }
        
        /** Add name to the given consumer. */
        public default <T> Consumer<T> Consumer(String name, java.util.function.Consumer<T> consumer) {
            return new Consumer<T>(name, consumer);
        }
        
    }
    
}
