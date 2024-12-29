package org.duckdns.sunga.rw5noti;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.auth.AuthApi;
import com.xiaomi.xms.wearable.message.MessageApi;
import com.xiaomi.xms.wearable.node.Node;
import com.xiaomi.xms.wearable.node.NodeApi;
import com.xiaomi.xms.wearable.tasks.OnFailureListener;
import com.xiaomi.xms.wearable.tasks.OnSuccessListener;
import com.xiaomi.xms.wearable.auth.Permission;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ApiUtil {

    private Context cx;
    private NodeApi nodeApi;
    private AuthApi authApi;
    private MessageApi messageApi;
    private String nodeId;
    private boolean hasPermissioin;
    private boolean isInstalled;

    public ApiUtil(Context context) {
        this.cx = context;
        this.nodeApi = Wearable.getNodeApi(context);
        this.authApi = Wearable.getAuthApi(context);
        this.messageApi = Wearable.getMessageApi(context);
        this.init();
    }

    public void init() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        this.nodeApi.getConnectedNodes().addOnSuccessListener(new OnSuccessListener<List<Node>>() {
            @Override
            public void onSuccess(List<Node> nodes) {
                if (nodes == null || nodes.isEmpty()) {
                    ApiUtil.this.requestPermission();
                    return;
                }
                ApiUtil.this.nodeId = ((Node)nodes.get(0)).id;

                ApiUtil.this.checkPermission();
                ApiUtil.this.isBandAppInstalled();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("MyError:: ", "fail to get listener");
            }
        });
    }

    public void checkPermission() {
        Permission[] permissions = new Permission[] {Permission.DEVICE_MANAGER,Permission.NOTIFY};

        this.authApi.checkPermissions(this.nodeId, permissions)
            .addOnSuccessListener(new OnSuccessListener<boolean[]>() {
                @Override
                public void onSuccess(boolean[] booleans) {
                    ApiUtil.this.hasPermissioin = booleans[0];
                    if(!booleans[0]) {
                        ApiUtil.this.requestPermission();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("MyError:: ", "fail to check permission");
                    ApiUtil.this.hasPermissioin = false;
                }
            });
    }

    private void requestPermission() {
        Permission[] permissions = new Permission[] {Permission.DEVICE_MANAGER,Permission.NOTIFY};
        this.authApi.requestPermission(this.nodeId, permissions)
            .addOnSuccessListener(new OnSuccessListener<Permission[]>() {
                @Override
                public void onSuccess(Permission[] permissions) {
                    ApiUtil.this.hasPermissioin = true;
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("MyError:: ", "fail to get permission");
                    ApiUtil.this.hasPermissioin = false;
                }
            });
    }

    public void isBandAppInstalled() {

        this.nodeApi.isWearAppInstalled(this.nodeId)
            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    ApiUtil.this.isInstalled = result;
                    if(!result) {
                        Log.d("MyError:: ", "app is not installed");
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull  Exception e) {
                    Log.d("MyError:: ", "fail to get installed info");
                }
            });
    }

    public void sendMessage(byte[] data) {
        if(!this.hasPermissioin || !this.isInstalled) {
            Log.d("MyError:: ", "no permission OR not installed");
            return;
        }

        this.nodeApi.launchWearApp(this.nodeId,"/index")
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void var1) {

                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception var1) {
                    Log.d("MyError:: ", "launch app failed");
                }
            });

        this.messageApi.sendMessage(this.nodeId, data)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void var1) {
                    Log.d("MyErrorNOT:: ", "send data success");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("MyErrorNOT:: ", "send data fail");
                }
            });
    }
}
