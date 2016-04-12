package it.playfellas.tenbuslib.events;

public class StringNetEvent extends NetEvent {
    private String body;

    public StringNetEvent(String body) {
        this.body = this.deviceName + " -> " + body;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return body;
    }
}
