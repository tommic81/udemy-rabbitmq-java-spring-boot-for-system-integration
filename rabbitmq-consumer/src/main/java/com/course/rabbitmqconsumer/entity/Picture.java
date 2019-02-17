package com.course.rabbitmqconsumer.entity;

public class Picture {

    private String name;
    private String source;
    private String type;
    private long size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Picture{" +
                "name='" + name + '\'' +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                '}';
    }
}
