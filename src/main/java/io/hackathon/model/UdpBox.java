package io.hackathon.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 10.11.2018
 */
public class UdpBox {

    private String msg;
    private LocalDateTime timestamp;

    public UdpBox(String msg) {
        this.msg = msg;
        this.timestamp = LocalDateTime.now();
    }

    public String getMsg() {
        return msg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UdpBox udpBox = (UdpBox) o;
        return Objects.equals(msg, udpBox.msg) &&
                Objects.equals(timestamp, udpBox.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(msg, timestamp);
    }
}
