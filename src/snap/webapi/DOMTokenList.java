package snap.webapi;
import java.util.stream.Stream;

/**
 * This class is a wrapper for Web API DOMTokenList (https://developer.mozilla.org/en-US/docs/Web/API/DOMTokenList).
 */
public class DOMTokenList extends JSProxy {

    /**
     * Constructor.
     */
    public DOMTokenList(Object jsObj)
    {
        super(jsObj);
    }

    /**
     * Return canvas X.
     */
    public int length()  { return getMemberInt("length"); }

    public String item(int index)  { return (String) call("item", index); }

    public boolean contains(String token)  { return (Boolean) call("contains", token); }

    public void add(String token)  { call("add", token); }

    public void add(String token1, String token2) { call("add", token1, token2); }

    public void add(String ... tokens)  { Stream.of(tokens).forEach(this::add); }

    public void remove(String token)  { call("remove", token); }

    public void remove(String token1, String token2)  { call("remove", token1, token2); }

    public void remove(String... tokens)  { Stream.of(tokens).forEach(this::remove); }

    public boolean toggle(String token)  { return (Boolean) call("toggle", token); }
}
