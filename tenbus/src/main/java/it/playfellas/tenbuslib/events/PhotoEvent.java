package it.playfellas.tenbuslib.events;


/**
 * Created by Stefano Cappa on 03/08/15.
 */
public class PhotoEvent extends NetEvent {
    private byte[] photoByteArray;

    public PhotoEvent(byte[] photoByteArray) {
        this.photoByteArray = photoByteArray;
    }

    public byte[] getPhotoByteArray() {
        return photoByteArray;
    }
}
