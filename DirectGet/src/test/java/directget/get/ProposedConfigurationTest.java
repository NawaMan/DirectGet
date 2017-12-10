package directget.get;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Ignore;
import org.junit.Test;

import directget.get.exceptions.AppScopeAlreadyInitializedException;
import directget.get.supportive.RefTo;
import lombok.val;

public class ProposedConfigurationTest {
    
    static final RefTo<String> text = Ref.toValue("Hello world!");
    
    @Test
    public void testAddingProviderToProposedConfigurationEndedUpBeingUsedAfterInitializing() {
        App.reset();
        try {
            assertFalse(App.isInitialized());
            
            ProposedConfiguration.instance
            .add(text.butDefaultedTo("Hi World!!!"));
            
            App.initialize();
            
            assertEquals("Hi World!!!", Get.the(text));
        } finally {
            App.reset();
        }
    }

    @Test
    public void testPreferability() {
        App.reset();
        try {
            assertFalse(App.isInitialized());
            
            ProposedConfiguration.instance
            .add(text.butDictatedTo("Hey World!!!"))
            .add(text.butDefaultedTo("Hi World!!!"));
            
            App.initialize();
            
            assertEquals("Hey World!!!", Get.the(text));
        } finally {
            App.reset();
        }
    }
    
    @Test
    public void testProposedConfigurationIsIgnoredWhenAnotherConfigIsUsed()
            throws AppScopeAlreadyInitializedException {
        App.reset();
        try {
            assertFalse(App.isInitialized());
            
            ProposedConfiguration.instance
            .add(text.butDefaultedTo("Hi World!!!"));
            
            App.initialize(new Configuration(text.butDefaultedTo("Hey World!!!")));
            
            assertEquals("Hey World!!!", Get.the(text));
        } finally {
            App.reset();
        }
    }
    
    @Test
    public void testOnAccepted() {
        App.reset();
        try {
            assertFalse(App.isInitialized());
            
            val isAccepted = new AtomicBoolean();
            val isRejected = new AtomicBoolean();
            
            ProposedConfiguration.instance
            .add(text.butDefaultedTo("Hi World!!!"))
                .onAccepted((p,s)-> 
                    isAccepted.set(true) )
                .onRejected((p,s)-> 
                    isRejected.set(true) );
            
            App.initialize();
            
            assertTrue( isAccepted.get());
            assertFalse(isRejected.get());
        } finally {
            App.reset();
        }
    }
    
    @Test
    public void testOnRejected() throws AppScopeAlreadyInitializedException {
        App.reset();
        try {
            assertFalse(App.isInitialized());
            
            val isAccepted = new AtomicBoolean();
            val isRejected = new AtomicBoolean();
            
            ProposedConfiguration.instance
            .add(text.butDefaultedTo("Hi World!!!"))
                .onAccepted((p,s)-> isAccepted.set(true) )
                .onRejected((p,s)-> isRejected.set(true) );
    
            App.initialize(new Configuration(text.butDefaultedTo("Hey World!!!")));
            
            assertFalse(isAccepted.get());
            assertTrue( isRejected.get());
        } finally {
            App.reset();
        }
    }
    
}
