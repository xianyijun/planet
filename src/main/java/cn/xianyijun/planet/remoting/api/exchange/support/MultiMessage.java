package cn.xianyijun.planet.remoting.api.exchange.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The type Multi message.
 */
public class MultiMessage implements Iterable {
    private final List messages = new ArrayList();

    private MultiMessage() {
    }

    /**
     * Create from collection multi message.
     *
     * @param collection the collection
     * @return the multi message
     */
    public static MultiMessage createFromCollection(Collection collection) {
        MultiMessage result = new MultiMessage();
        result.addMessages(collection);
        return result;
    }

    /**
     * Create from array multi message.
     *
     * @param args the args
     * @return the multi message
     */
    public static MultiMessage createFromArray(Object... args) {
        return createFromCollection(Arrays.asList(args));
    }

    /**
     * Create multi message.
     *
     * @return the multi message
     */
    public static MultiMessage create() {
        return new MultiMessage();
    }

    /**
     * Add message.
     *
     * @param msg the msg
     */
    public void addMessage(Object msg) {
        messages.add(msg);
    }

    /**
     * Add messages.
     *
     * @param collection the collection
     */
    public void addMessages(Collection collection) {
        messages.addAll(collection);
    }

    /**
     * Gets messages.
     *
     * @return the messages
     */
    public Collection getMessages() {
        return Collections.unmodifiableCollection(messages);
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return messages.size();
    }

    /**
     * Get object.
     *
     * @param index the index
     * @return the object
     */
    public Object get(int index) {
        return messages.get(index);
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Remove messages collection.
     *
     * @return the collection
     */
    public Collection removeMessages() {
        Collection result = Collections.unmodifiableCollection(messages);
        messages.clear();
        return result;
    }

    @Override
    public Iterator iterator() {
        return messages.iterator();
    }

}
