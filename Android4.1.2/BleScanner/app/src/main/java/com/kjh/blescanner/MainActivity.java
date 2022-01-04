// 작성자: 김진희
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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS = 1;

    private ActivityResultLauncher<Intent> resultLauncher;
    private Intent intent;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceViewAdapter deviceViewAdapter;

    private Button btnScan;


    // 앱 시작 시 동작
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 블루투스 권한 요청 수락 및 거절 시 동작 설정
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_OK) {
                        Log.d("bluetooth 권한 요청", "수락");
                        
                        // 메인 액티비티 화면을 보여주고, ble 장치를 자동으로 스캔함
                        showContents();
                    } else {
                        // 블루투스 기능을 켜지 않았을 때
                        Log.d("bluetooth 권한 요청", "거절");
                        Toast.makeText(this, "블루투스 권한을 허용해야 사용가능합니다.", Toast.LENGTH_LONG).show();
                        System.exit(0);
                    }
                });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // 블루투스가 활성화되어 있는지 검사
        if (bluetoothAdapter.isEnabled()) {
            // 블루투스가 켜져 있을 때
            // 블루투스 관련 실행 진행
            Log.d("bluetooth 초기 상태", "활성");
            // 메인 액티비티 화면을 보여주고, ble 장치를 자동으로 스캔함
            showContents();
        } else {
            Log.d("bluetooth 초기 상태", "비활성");
            // 블루투스 활성화 하도록 권한 요청함
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // deprecated 되었으므로 다른 방식으로 대체
            //startActivityForResult(intent, 1);
            resultLauncher.launch(intent);
        }
    }


    // 메인 액티비티 화면을 보여주고, ble 장치를 자동으로 스캔함
    void showContents() {
        setContentView(R.layout.activity_main);

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = findViewById(R.id.bluetoothDevicesView) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(this)) ;

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        deviceViewAdapter = new BluetoothDeviceViewAdapter(this) ;
        recyclerView.setAdapter(deviceViewAdapter);

        // BLE 사용을 위한 위치 권한 요청
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS);
        // 여기서 블루투스 어댑터를 다시 얻어와야 사용 가능
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 디바이스 발견 시 동작을 정의할 브로드캐스트 리시버를 등록함
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bleBroadcastReceiver, filter);

        // BLE 장치를 스캔하고, 스캔 중에는 버튼을 비활성화함 (스캔 시간은 3초)
        startBleScan(3);

        // 스캔 버튼 기능 정의
        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(listener -> {
            Log.d(TAG, "btnScan 클릭");
            // 스캔 버튼 클릭 시 목록을 비우고, 다시 스캔함
            deviceViewAdapter.clear();
            deviceViewAdapter.notifyDataSetChanged();
            startBleScan(3);
        });
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        
        // 장치 발견 시 장치 추가
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

    // BLE 장치를 스캔하고, 스캔 중에는 버튼을 비활성화함
    // scanCallback은 이 클래스의 최하단에 정의함
    private void startBleScan(int second) {
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
        }, second * 1000);
    }

    private void stopScan() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        btnScan.setEnabled(true);
    }


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

}