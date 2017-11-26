package directget.get;

import static org.junit.Assert.*;

import org.junit.Test;

import directget.get.exceptions.AppScopeAlreadyInitializedException;
import lombok.val;

public class ConfigurationTest {

    @Test
    public void testConfiguration() throws AppScopeAlreadyInitializedException {
        val ref1 = Ref.of(String.class).defaultedTo("Ref1");
        val ref2 = Ref.of(String.class).defaultedTo("Ref2");
        
        assertEquals("Ref1", ref1.get());
        assertEquals("Ref2", ref2.get());
        
        val configuration = new Configuration(
                ref1.getProvider().butWith("Str1"),
                ref2
        );
        
        assertEquals("Str1", configuration.getProvider(ref1).get());
        assertEquals("Ref2", configuration.getProvider(ref2).get());
    }
    
    @Test
    public void testConfiguration_withDuplicateRef() throws AppScopeAlreadyInitializedException {
        val ref1 = Ref.of(String.class).defaultedTo("Ref1");
        
        assertEquals("Ref1", ref1.get());
        
        val configuration = new Configuration(
                ref1.getProvider().butWith("Str1"),
                ref1
        );
        
        assertEquals("Str1", configuration.getProvider(ref1).get());
    }
    
    @Test
    public void testConfiguration_combine() throws AppScopeAlreadyInitializedException {
        val ref1 = Ref.of(String.class).defaultedTo("Ref1");
        val ref2 = Ref.of(String.class).defaultedTo("Ref2");
        val ref3 = Ref.of(String.class).defaultedTo("Ref3");
        
        assertEquals("Ref1", ref1.get());
        assertEquals("Ref2", ref2.get());
        assertEquals("Ref3", ref3.get());
        
        val configuration1 = new Configuration(
                ref1.getProvider().butWith("Str1"),
                ref2
        );
        val configuration2 = new Configuration(
                ref3
        );
        val configuration = Configuration.combineOf(configuration1, configuration2);
        
        assertEquals("Str1", configuration.getProvider(ref1).get());
        assertEquals("Ref2", configuration.getProvider(ref2).get());
        assertEquals("Ref3", configuration.getProvider(ref3).get());
    }
    
    @Test
    public void testConfiguration_combine_withDuplicateRefs() throws AppScopeAlreadyInitializedException {
        val ref1 = Ref.of(String.class).defaultedTo("Ref1");
        val ref2 = Ref.of(String.class).defaultedTo("Ref2");
        val ref3 = Ref.of(String.class).defaultedTo("Ref3");
        
        assertEquals("Ref1", ref1.get());
        assertEquals("Ref2", ref2.get());
        assertEquals("Ref3", ref3.get());
        
        val configuration1 = new Configuration(
                ref1.getProvider().butWith("Str1"),
                ref2
        );
        val configuration2 = new Configuration(
                ref1,
                ref3
        );
        val configuration = Configuration.combineOf(configuration1, configuration2);
        
        assertEquals("Str1", configuration.getProvider(ref1).get());
        assertEquals("Ref2", configuration.getProvider(ref2).get());
        assertEquals("Ref3", configuration.getProvider(ref3).get());
    }
    
    @Test
    public void testConfiguration_combine_preferenceSame() throws AppScopeAlreadyInitializedException {
        val ref = Ref.of(String.class).defaultedTo("Ref");
        
        val configuration1 = new Configuration(
                ref.getProvider().butWith("Str1")
        );
        val configuration2 = new Configuration(
                ref.getProvider().butWith("Str2")
        );
        
        assertEquals("Str1", Configuration.combineOf(configuration1, configuration2).getProvider(ref).get());
        assertEquals("Str2", Configuration.combineOf(configuration2, configuration1).getProvider(ref).get());
    }
    
    @Test
    public void testConfiguration_combine_preferenceDiffer() throws AppScopeAlreadyInitializedException {
        val ref = Ref.of(String.class).defaultedTo("Ref");
        
        val configuration1 = new Configuration(
                ref.getProvider().butWith("Str1")
        );
        val configuration2 = new Configuration(
                ref.getProvider().butDictate().butWith("Str2")
        );
        
        assertEquals("Str2", Configuration.combineOf(configuration1, configuration2).getProvider(ref).get());
        assertEquals("Str2", Configuration.combineOf(configuration2, configuration1).getProvider(ref).get());
    }
    
}
