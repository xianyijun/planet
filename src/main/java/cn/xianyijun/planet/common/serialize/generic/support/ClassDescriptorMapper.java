package cn.xianyijun.planet.common.serialize.generic.support;

/**
 * The interface Class descriptor mapper.
 */
public interface ClassDescriptorMapper {
    /**
     * Gets descriptor.
     *
     * @param index the index
     * @return the descriptor
     */
    String getDescriptor(int index);

    /**
     * Gets descriptor index.
     *
     * @param desc the desc
     * @return the descriptor index
     */
    int getDescriptorIndex(String desc);
}
