// IBinder.aidl
package rodic.aleksa.miberchatapplication;

// Declare any non-default types here with import statements
import rodic.aleksa.miberchatapplication.ICallback;

interface IBinderNewMessage {
    void setCallback(in ICallback callback);
}
