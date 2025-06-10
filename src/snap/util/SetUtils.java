package snap.util;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility methods for use with Java.util.Set.
 */
public class SetUtils extends CollectionUtils {

    /**
     * Returns a filtered array for given original and Predicate.
     */
    public static <T> Set<T> filter(Collection<T> aList, Predicate<? super T> pred)
    {
        return aList.stream().filter(pred).collect(Collectors.toSet());
    }

    /**
     * Returns list of given class for given original.
     */
    public static <T,R> Set<R> filterByClass(Collection<T> aList, Class<R> aClass)
    {
        return (Set<R>) filter(aList, obj -> aClass.isInstance(obj));
    }
}
