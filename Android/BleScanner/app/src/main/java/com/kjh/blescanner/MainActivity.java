package com.kjh.blescanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.kjh.blescanner.domain.BluetoothDeviceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

// 블루투스 권한 요청 거부 시 처리 방법

// 1. 거부하면 종료
// 2. 종료하지 말고, 권한을 켜야 한다는 안내 텍스트 보여주기

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS = 1;

    private ActivityResultLauncher<Intent> resultLauncher;
    private Intent intent;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceViewAdapter deviceViewAdapter;

    private Button btnScan;


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("onScanFailed", errorCode+"");
        }

        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (deviceViewAdapter.addDevice(result.getDevice())) {
                        deviceViewAdapter.notifyDataSetChanged();
                        Log.d("processResult", result+"");
                    }
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK) {
                        Log.d("bluetooth 권한 요청", "수락");
                        showContents();
                    } else {
                        // 블루투스 기능을 켜지 않았을 때
                        Log.d("bluetooth 권한 요청", "거절");
                        Toast.makeText(this, "블루투스 권한을 허용해야 사용가능합니다.", Toast.LENGTH_LONG).show();
                        System.exit(0);
                    }
                });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            // 블루투스가 켜져 있을 때
            // 블루투스 관련 실행 진행
            Log.d("bluetooth 초기 상태", "활성");
            showContents();
        } else {
            Log.d("bluetooth 초기 상태", "비활성");
            // 블루투스 활성화 하도록 권한 요청함
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // deprecated 되었으므로 다른 방식으로 대체
            //startActivityForResult(intent, 1);
            resultLauncher.launch(intent);
        }

        //setContentView(R.layout.activity_main);
    }


    void showContents() {
        setContentView(R.layout.activity_main);

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = findViewById(R.id.bluetoothDevicesView) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(this)) ;

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        deviceViewAdapter = new BluetoothDeviceViewAdapter() ;
        recyclerView.setAdapter(deviceViewAdapter);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS);
        // 여기서 블루투스 어댑터를 다시 얻어와야 사용 가능
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        bluetoothAdapter.startLeScan(leScanCallback);
//        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        startScan();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(listener -> {

            Log.d(TAG, "btnScan 클릭");
            deviceViewAdapter.clear();
            deviceViewAdapter.notifyDataSetChanged();
            startScan();
//            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
//            bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        });
    }


    private void startScan() {
//        bluetoothAdapter.startLeScan(leScanCallback);
        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        if (btnScan != null)
            btnScan.setEnabled(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    stopScan();
                });
            }
        }, 3000);
    }

    private void stopScan() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        btnScan.setEnabled(true);
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("BroadcastReceiver", "onReceive");

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

//                Log.d("deviceName", deviceName);
//                Log.d("deviceHardwareAddress", deviceHardwareAddress);
                deviceViewAdapter.addDevice(device);
                deviceViewAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}