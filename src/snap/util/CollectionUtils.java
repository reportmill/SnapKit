package snap.util;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Utility methods for use with Java.util.Collection.
 */
public class CollectionUtils {

    /**
     * Returns whether given collection has match for given predicate.
     */
    public static <T> boolean hasMatch(Collection<T> aList, Predicate<? super T> aPred)
    {
        return findMatch(aList, aPred) != null;
    }

    /**
     * Returns the first item in collection that matches given predicate (or null).
     */
    public static <T> T findMatch(Collection<T> aList, Predicate<? super T> aPred)
    {
        for (T item : aList)
            if (aPred.test(item))
                return item;
        return null;
    }
}
