package ch.logixisland.anuto.util.iterator;

class MappingIterator<F, T> extends StreamIterator<T> {

    private final Function<? super F, ? extends T> mMapper;
    private final StreamIterator<F> mOriginal;

    MappingIterator(StreamIterator<F> original, Function<? super F, ? extends T> mapper) {
        mOriginal = original;
        mMapper = mapper;
    }

    @Override
    public void close() {
        mOriginal.close();
    }

    @Override
    public boolean hasNext() {
        return mOriginal.hasNext();
    }

    @Override
    public T next() {
        return mMapper.apply(mOriginal.next());
    }
}
