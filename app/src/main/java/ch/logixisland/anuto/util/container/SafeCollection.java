package ch.logixisland.anuto.util.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.logixisland.anuto.util.iterator.LazyIterator;
import ch.logixisland.anuto.util.iterator.StreamIterable;
import ch.logixisland.anuto.util.iterator.StreamIterator;

public class SafeCollection<T> implements Collection<T>, StreamIterable<T> {

    private class SafeIterator extends LazyIterator<T> {
        private int mNextIndex = 0;

        private SafeIterator() {
            mIterators.add(this);
        }

        @Override
        public T fetchNext() {
            T ret = null;

            if (mNextIndex < mItems.size()) {
                ret = mItems.get(mNextIndex++);
            } else {
                close();
            }

            return ret;
        }

        @Override
        public void close() {
            mIterators.remove(this);
        }

        @Override
        public void remove() {
            SafeCollection.this.remove(mNextIndex - 1);
        }
    }


    private final List<T> mItems;
    private final Collection<SafeIterator> mIterators = new ArrayList<>();


    public SafeCollection() {
        mItems = new ArrayList<>();
    }


    @Override
    public boolean add(T object) {
        return mItems.add(object);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return mItems.addAll(collection);
    }

    @Override
    public void clear() {
        mItems.clear();

        for (SafeIterator it : mIterators) {
            it.mNextIndex = 0;
        }
    }

    @Override
    public boolean contains(Object object) {
        return mItems.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return mItems.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public StreamIterator<T> iterator() {
        return new SafeIterator();
    }

    @Override
    public boolean remove(Object object) {
        //noinspection SuspiciousMethodCalls
        int index = mItems.indexOf(object);

        if (index >= 0) {
            remove(index);
            return true;
        }

        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean ret = false;

        for (Object item : collection) {
            if (remove(item)) {
                ret = true;
            }
        }

        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        boolean ret = false;

        for (T item : this) {
            if (!collection.contains(item)) {
                remove(item);
                ret = true;
            }
        }

        return ret;
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public Object[] toArray() {
        return mItems.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] array) {
        return mItems.toArray(array);
    }

    private T remove(int index) {
        T ret = mItems.remove(index);

        for (SafeIterator it : mIterators) {
            if (it.mNextIndex > index) {
                it.mNextIndex--;
            }
        }

        return ret;
    }

}
