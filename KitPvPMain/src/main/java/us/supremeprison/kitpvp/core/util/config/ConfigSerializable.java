package us.supremeprison.kitpvp.core.util.config;

/**
 * @author Connor Hollasch
 * @since 3/29/2015
 */
public interface ConfigSerializable<T> {

    public T load(String in);

    public String save(T t);

    public Class<? extends T> getWrappedType();
}
