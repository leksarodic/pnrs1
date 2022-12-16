package rodic.aleksa.miberchatapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class BindService extends Service {
    private Binder binder = null;

    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null){
            binder = new Binder();
        }
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        binder.stop();
        return super.onUnbind(intent);
    }
}
