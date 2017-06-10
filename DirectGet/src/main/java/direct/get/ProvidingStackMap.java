package direct.get;

import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Supplier;

import lombok.val;
import lombok.experimental.ExtensionMethod;

@SuppressWarnings({ "rawtypes", "unchecked"})
@ExtensionMethod({ Extensions.class })
class ProvidingStackMap extends TreeMap<Ref, Stack<Providing>> {
	
	private static final long serialVersionUID = -8113998773064688984L;

	private Supplier<Stack<Providing>> ensureValue(Ref ref) {
		return (Supplier<Stack<Providing>>)()->{
			this.put((Ref)ref, new Stack<Providing>());
			return super.get(ref);
		};
	}
	
	@Override
	public Stack<Providing> get(Object ref) {
		val stack = super.get(ref)._or(ensureValue((Ref)ref));
		return stack;
	}
	
	public <T> Providing<T> peek(Ref<T> ref) {
		if (!containsKey(ref)) {
			return null;
		}
		Stack<Providing> stack = get(ref);
		if (stack.isEmpty()) {
			return null;
		}
		return stack.peek();
	}
	
	public String toXRayString() {
		val isEmpty = this.isEmpty();
		if (isEmpty) {
			return "{\n}";
		}
		
		val pairs = this._toPairStrings()._toIndentLines();
		val xRay  = String.format("{\n\t%s\n}", pairs);
		return xRay; 
	}
	
}