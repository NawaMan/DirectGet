//  ========================================================================
//  Copyright (c) 2017 Nawapunth Manusitthipol.
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

import static directget.get.supportive.Utilities.isLocalCall;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import directget.get.supportive.Provider;
import directget.get.supportive.Utilities;
import dssb.callerid.impl.CallerId;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This class allow configuration to be prepared before App.scope is initialized.
 * 
 * @author NawaMan
 */
@SuppressWarnings("rawtypes")
@ExtensionMethod({ Utilities.class, Nulls.class })
public class ProposedConfiguration {
    
    /** The instance of the proposed configuration. */
    public static final ProposedConfiguration instance = new ProposedConfiguration();
    
    /** Status of the proposal.*/
    public static enum Status {
        /** Proposed is accepted */
        ACCEPTED,
        /** Proposed is rejected */
        REJECTED;
        
        /** @return {@code true} if the status is accepted. */
        public boolean isAccepted() {
            return this == ACCEPTED;
        }
        /** @return {@code true} if the status is rejected. */
        public boolean isRejected() {
            return this == REJECTED;
        }
    }
    
    private final static Map<Ref, Provider> providers = new ConcurrentHashMap<>();

    private final static Map<Provider, Set<BiConsumer<Provider,Status>>> onAccepteds = new ConcurrentHashMap<>();
    private final static Map<Provider, Set<BiConsumer<Provider,Status>>> onRejecteds = new ConcurrentHashMap<>();
    
    private final static List<Provider> DEFAULT_PROVIDERS = Arrays.asList(
            Ref.of(byte.class)   .butDefaultedTo((byte)0),
            Ref.of(short.class)  .butDefaultedTo((short)0),
            Ref.of(int.class)    .butDefaultedTo(0),
            Ref.of(long.class)   .butDefaultedTo(0L),
            Ref.of(float.class)  .butDefaultedTo((float)0.0),
            Ref.of(double.class) .butDefaultedTo(0.0),
            Ref.of(char.class)   .butDefaultedTo(' '),
            Ref.of(boolean.class).butDefaultedTo(false),
            
            Ref.of(Byte.class)     .butDefaultedTo((byte)0),
            Ref.of(Short.class)    .butDefaultedTo((short)0),
            Ref.of(Integer.class)  .butDefaultedTo(0),
            Ref.of(Long.class)     .butDefaultedTo(0L),
            Ref.of(Float.class)    .butDefaultedTo((float)0.0),
            Ref.of(Double.class)   .butDefaultedTo(0.0),
            Ref.of(Character.class).butDefaultedTo(' '),
            Ref.of(Boolean.class)  .butDefaultedTo(false),
            
            Ref.of(String.class)      .butDefaultedTo(""),
            Ref.of(CharSequence.class).butDefaultedTo(""),
            Ref.of(BigInteger.class)  .butDefaultedTo(BigInteger.ZERO),
            Ref.of(BigDecimal.class)  .butDefaultedTo(BigDecimal.ZERO)
    );
    
    private ProposedConfiguration() {}
    
    // -- For testing only --

    /** Reset proposed configuration. */
    void reset() {
        if (!isLocalCall())
            return;
        
        providers.clear();
        onAccepteds.clear();
        onRejecteds.clear();
    }
    
    
    /** @return {@code true} if the App is not yet initialized. */
    public boolean isOpen() {
        return !App.isInitialized();
    }

    /** @return {@code true} if the App is not yet initialized. */
    public boolean isClosed() {
        return App.isInitialized();
    }
    
    /**
     * Propose application mode.
     * 
     * @param mode  the propose application mode.
     * @return the proposed configuration.
     */
    public ProposedConfigurationWithLastProvider appMode(AppMode mode) {
        return CallerId.instance.trace(caller->{
            return this.add(App.mode.butDictatedTo(mode));
        });
    }
    
    // TODO - Share the same code with the one in Configuration and print it out to log. 
    /**
     * Add the provider to the proposed configuration.
     * 
     * @param provider the provider.
     * @return the proposed configuration.
     */
    public <T> ProposedConfigurationWithLastProvider add(Provider<T> provider) {
        if (provider.isNotNull())
            Configuration.addToMap(providers, provider);
        
        return new ProposedConfigurationWithLastProvider(provider, App.isInitialized());
    }
    
    //== Sub class ==
    
    /**
     * Instances of this class hold the last provider so listener can be added to it.
     */
    public static class ProposedConfigurationWithLastProvider extends ProposedConfiguration {
        
        private final Provider lastProvider;
        
        private final boolean isLate;
        
        private ProposedConfigurationWithLastProvider(Provider provider, boolean isLate) {
            this.lastProvider = provider;
            this.isLate       = isLate;
        }
        
        /** 
         * Add onAccepted listener. 
         * 
         * @param onAccepted  the event to be called when accepted.
         * @return this propose configuration.
         **/
        public ProposedConfigurationWithLastProvider onAccepted(Runnable onAccepted) {
            return onAccepted((provider, status)->{
                onAccepted.run(); 
            });
        }
        
        /** 
         * Add onAccepted listener. 
         * 
         * @param onAccepted  the event to be called when accepted.
         * @return this propose configuration.
         **/
        public ProposedConfigurationWithLastProvider onAccepted(Consumer<Provider> onAccepted) {
            return onAccepted((provider, status)->{
                onAccepted.accept(provider); 
            });
        }
        
        /** 
         * Add onAccepted listener. 
         * 
         * @param onAccepted  the event to be called when accepted.
         * @return this propose configuration.
         **/
        public ProposedConfigurationWithLastProvider onAccepted(BiConsumer<Provider, Status> onAccepted) {
            if (lastProvider.isNotNull() && onAccepted.isNotNull()) {
                onAccepteds.putIfAbsent(lastProvider, new HashSet<>());
                onAccepteds.get(lastProvider).add(onAccepted);
            }
            
            return this;
        }

        /**
         * Add onReject listener. 
         * 
         * @param onRejected  the event to be called when rejected.
         * @return this propose configuration.
         **/
        public ProposedConfigurationWithLastProvider onRejected(Runnable onRejected) {
            return onRejected((provider, status)->{
                onRejected.run(); 
            });
        }

        /**
         * Add onReject listener. 
         * 
         * @param onRejected  the event to be called when rejected.
         * @return this propose configuration.
         **/
        public ProposedConfigurationWithLastProvider onRejected(Consumer<Provider> onRejected) {
            return onRejected((provider, status)->{
                onRejected.accept(provider); 
            });
        }

        /**
         * Add onReject listener. 
         * 
         * @param onRejected  the event to be called when rejected.
         * @return this propose configuration.
         **/
        public ProposedConfigurationWithLastProvider onRejected(BiConsumer<Provider, Status> onRejected) {
            if (lastProvider.isNotNull() && onRejected.isNotNull()) {
                if (isLate) {
                    notifyEvent(lastProvider, onRejected, Status.REJECTED);
                } else {
                    onRejecteds.putIfAbsent(lastProvider, new HashSet<>());
                    onRejecteds.get(lastProvider).add(onRejected);
                }
            }
            
            return this;
        }
        
        /**
         * Cause a system halt if the previous proposal is not accepted.
         * 
         * @return propose configuration.
         */
        public ProposedConfigurationWithLastProvider orSystemHalt() {
           return orSystemHalt(-1); 
        }
        
        /**
         * Cause a system halt if the previous proposal is not accepted.
         * 
         * @param  message  the exit message - use default if null given.
         * @return propose configuration.
         */
        public ProposedConfigurationWithLastProvider orSystemHalt(String message) {
           return orSystemHalt(message, -1); 
        }
        
        /**
         * Cause a system halt if the previous proposal is not accepted.
         * 
         * @param exitStatus  the exit status.
         * @return propose configuration.
         */
        public ProposedConfigurationWithLastProvider orSystemHalt(int exitStatus) {
           return orSystemHalt(null, exitStatus); 
        }
        
        /**
         * Cause a system halt if the previous proposal is not accepted.
         * 
         * @param message     the exit message - use default if null given.
         * @param exitStatus  the exit status.
         * @return propose configuration.
         */
        public ProposedConfigurationWithLastProvider orSystemHalt(String message, int exitStatus) {
            return onRejected(provider->{
                System.err.println(message.or("Application is already initialized!"));
                System.err.println("Provider: " + provider);
                System.exit(exitStatus);
            });
        }
        
    }

    Configuration getConfiguration() {
        // TODO - Get SPI and ask for all proposal.
        // See https://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html#Service_Provider
        
        Stream<Provider> theProviders = providers.values().stream();
        Stream<Provider> theDefauls  = DEFAULT_PROVIDERS.stream();
        return new Configuration(Stream.concat(theProviders, theDefauls));
    }

    /**
     * 
     */
    public void onInitialized() {
        onAccepteds.entrySet().forEach(notifyEvent(Status.ACCEPTED));
        onRejecteds.entrySet().forEach(notifyEvent(Status.REJECTED));
    }

    @SuppressWarnings("unchecked")
    private Consumer<? super Entry<Provider, Set<BiConsumer<Provider, Status>>>> notifyEvent(Status status) {
        return entry->{
            val provider = entry.getKey();
            val isInused = (boolean)(provider.isNotNull() && (provider == Get.getProvider(provider.getRef())));
            if (isInused != status.isAccepted())
                return;
            
            val listeners = entry.getValue();
            if (listeners.isNotNull()) {
                listeners.stream()
                .filter(Objects::nonNull)
                .forEach(listener->{
                    notifyEvent(provider, listener, status);
                });
            }
        };
    }

    private static void notifyEvent(Provider provider, BiConsumer<Provider, Status> listener, Status status) {
        if (provider.isNull())
            return;
        if (listener.isNull())
            return;
        try {
            listener.accept(provider, status);
        } catch (Exception e) {
            // TODO - May do more.
            e.printStackTrace();
        }
    }
    
}
