package directget.get.supportive.retain;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import directget.get.Ref;

public 
interface WithRetainer<T, WR extends WithRetainer<T, WR>> {

    public Retainer<T> getRetainer();

    WR __but(Supplier<T> newSupplier);

    
    /** @return the new providing similar to this one except that it retains globally. **/
    public default WR globally() {
        return __but(getRetainer().butGlobally());
    }

    /** @return the new providing similar to this one except that it retains locally. **/
    public default WR locally() {
        return __but(getRetainer().butLocally());
    }

    /** @return the new providing similar to this one except that it always retains its value. **/
    public default WR forAlways() {
        return __but(getRetainer().butAlways());
    }

    /** @return the new providing similar to this one except that it never retains its value. **/
    public default WR forNever() {
        return __but(getRetainer().butNever());
    }

    /** @return the new providing similar to this one except that it retains its value with in current thread. **/
    public default WR forCurrentThread() {
        return __but(getRetainer().forCurrentThread());
    }

    /** @return the new providing similar to this one except that it retains its value follow the give reference value ('same' rule). **/
    public default <R> WR forSame(Ref<R> ref) {
        return __but(getRetainer().forSame(ref));
    }

    /** @return the new providing similar to this one except that it retains its value follow the give reference value ('equivalent' rule). **/
    public default <R> WR forEquivalent(Ref<R> ref) {
        return __but(getRetainer().forEquivalent(ref));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period (in millisecond). **/
    public default <R> WR forTime(long time) {
        return __but(getRetainer().forTime(null, time));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period. **/
    public default <R> WR forTime(long time, TimeUnit unit) {
        return __but(getRetainer().forTime(null, time, unit));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period (in millisecond). **/
    public default <R> WR butForTime(Long startMilliseconds, long time) {
        return __but(getRetainer().forTime(startMilliseconds, time));
    }

    /** @return the new providing similar to this one except that it retains its value for a given time period. **/
    public default <R> WR forTime(Long startMilliseconds, long time, TimeUnit unit) {
        return __but(getRetainer().forTime(startMilliseconds, time, unit));
    }
    
}