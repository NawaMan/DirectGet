package directget.get;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ProvidingWithRetain<T> extends Providing<T> {


    public ProvidingWithRetain(Ref<T> ref, Preferability preferability, Supplier<? extends T> supplier) {
        super(ref, preferability, supplier);
    }


    ProvidingWithRetain<T> but(Supplier<T> newRetainer) {
        if (newRetainer == supplier) {
            return this;
        }
        return new ProvidingWithRetain<>(ref, preferability, newRetainer);
    }
    
    /** @return the new providing similar to this one except that it retains globally. **/
    public ProvidingWithRetain<T> globally() {
        return but(getRetainer().butGlobally());
    }

    /** @return the new providing similar to this one except that it retains locally. **/
    public ProvidingWithRetain<T> locally() {
        return but(getRetainer().butLocally());
    }

    /** @return the new providing similar to this one except that it always retains its value. **/
    public ProvidingWithRetain<T> forAlways() {
        return but(getRetainer().butAlways());
    }

    /** @return the new providing similar to this one except that it never retains its value. **/
    public ProvidingWithRetain<T> forNever() {
        return but(getRetainer().butNever());
    }

    /** @return the new providing similar to this one except that it retains its value with in current thread. **/
    public ProvidingWithRetain<T> forCurrentThread() {
        return but(getRetainer().forCurrentThread());
    }

    /** @return the new providing similar to this one except that it retains its value follow the give reference value ('same' rule). **/
    public <R> ProvidingWithRetain<T> forSame(Ref<R> ref) {
        return but(getRetainer().forSame(ref));
    }

    /** @return the new providing similar to this one except that it retains its value follow the give reference value ('equivalent' rule). **/
    public <R> ProvidingWithRetain<T> forEquivalent(Ref<R> ref) {
        return but(getRetainer().forEquivalent(ref));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period (in millisecond). **/
    public <R> ProvidingWithRetain<T> forTime(long time) {
        return but(getRetainer().forTime(null, time));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period. **/
    public <R> ProvidingWithRetain<T> forTime(long time, TimeUnit unit) {
        return but(getRetainer().forTime(null, time, unit));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period (in millisecond). **/
    public <R> ProvidingWithRetain<T> butForTime(Long startMilliseconds, long time) {
        return but(getRetainer().forTime(startMilliseconds, time));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period. **/
    public <R> ProvidingWithRetain<T> forTime(Long startMilliseconds, long time, TimeUnit unit) {
        return but(getRetainer().forTime(startMilliseconds, time, unit));
    }
    
}