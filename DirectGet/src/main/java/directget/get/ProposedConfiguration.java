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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import directget.get.supportive.Provider;
import directget.get.supportive.Utilities;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This class allow configuration to be prepared before App.scope is initialized.
 * 
 * @author NawaMan
 */
@SuppressWarnings("rawtypes")
@ExtensionMethod({ Utilities.class })
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
     * Add the provider to the proposed configuration.
     * 
     * @param provider the provider.
     * @return the proposed configuration.
     */
    public <T> ProposedConfigurationWithLastProvider add(Provider<T> provider) {
        if (provider.isNotNull()) {
            val ref = provider.getRef();
            if (providers.containsKey(ref)) {
                val thisPreferability = providers.get(ref).getPreferability();
                val thatPreferability = provider.getPreferability();
                if (thisPreferability.compareTo(thatPreferability) < 0)
                    providers.put(ref, provider);
            } else {
                providers.put(ref, provider);
            }
        }
        
        return new ProposedConfigurationWithLastProvider(provider, App.isInitialized());
    }
    
    //== Sub class ==
    
    /**
     * @author manusitn
     *
     */
    public static class ProposedConfigurationWithLastProvider extends ProposedConfiguration {
        
        private final Provider lastProvider;
        
        private final boolean isLate;
        
        private ProposedConfigurationWithLastProvider(Provider provider, boolean isLate) {
            this.lastProvider = provider;
            this.isLate       = isLate;
        }
        
        /** Add onAccepted listener. **/
        public ProposedConfigurationWithLastProvider onAccepted(BiConsumer<Provider, Status> onAccepted) {
            if (lastProvider.isNotNull() && onAccepted.isNotNull()) {
                onAccepteds.putIfAbsent(lastProvider, new HashSet<>());
                onAccepteds.get(lastProvider).add(onAccepted);
            }
            
            return this;
        }

        /** Add onReject listener. **/
        public ProposedConfigurationWithLastProvider onRejected(BiConsumer<Provider, Status> onRejected) {
            if (lastProvider.isNotNull() && onRejected.isNotNull()) {
                if (isLate) {
                    notifyReject(lastProvider, onRejected);
                } else {
                    onRejecteds.putIfAbsent(lastProvider, new HashSet<>());
                    onRejecteds.get(lastProvider).add(onRejected);
                }
            }
            
            return this;
        }
        
    }

    Configuration getConfiguration() {
        return new Configuration(providers.values().stream());
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
                    try {
                        listener.accept(provider, status);
                    } catch (Exception e) {
                        // TODO Think about this.
                    }
                });
            }
        };
    }

    private static void notifyReject(Provider provider, BiConsumer<Provider, Status> onRejected) {
        if (provider.isNull())
            return;
        if (onRejected.isNull())
            return;
        onRejected.accept(provider, Status.REJECTED);
    }
    
}
