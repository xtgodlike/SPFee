package com.qy.sp.fee.dto;

public class TLocationKey {
    private Integer hostId;

    private String segment;

    public Integer getHostId() {
        return hostId;
    }

    public void setHostId(Integer hostId) {
        this.hostId = hostId;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment == null ? null : segment.trim();
    }
}